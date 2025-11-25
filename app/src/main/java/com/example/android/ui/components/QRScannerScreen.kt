package com.example.android.ui.components

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Environment
import android.webkit.*
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.example.android.repository.AlbumRepository
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class Album(
    val id: String,
    val name: String
)

/** ---------- ViewModel ---------- **/

class QRScannerViewModel(
    private val repository: AlbumRepository
) : ViewModel() {

    var scannedUrl by mutableStateOf<String?>(null)
        private set

    var pendingDownloadUrl by mutableStateOf<String?>(null)
        private set

    var albumList by mutableStateOf<List<Album>>(emptyList())
        private set

    fun setScannedUrl(url: String) {
        scannedUrl = url
    }

    fun setPendingDownloadUrl(url: String?) {
        pendingDownloadUrl = url
    }

    fun loadAlbums(onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = repository.getAlbums()
                withContext(Dispatchers.Main) {
                    albumList = list.map { Album(it.id, it.name) }
                }
            } catch (e: Exception) {
                onError(e.message ?: "앨범 불러오기 실패")
            }
        }
    }

    fun uploadFile(context: Context, file: File, albumId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uploaded = repository.uploadPhotoFile(file)
                if (albumId != null) repository.addPhotoToAlbum(uploaded.photoId.toLong(), albumId)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "업로드 완료!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

/** ---------- DownloadReceiver ---------- **/

class DownloadReceiver(
    private val onDownloaded: (Uri) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != -1L) {
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val uri = dm.getUriForDownloadedFile(id)
                if (uri != null) onDownloaded(uri)
            }
        }
    }
}

/** ---------- QRScannerScreen ---------- **/

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerScreen(
    viewModel: QRScannerViewModel,
    modifier: Modifier = Modifier.fillMaxSize(),
    onResult: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // DownloadReceiver 등록
    val receiver = remember {
        DownloadReceiver { uri ->
            val file = uriToFile(context, uri)
            viewModel.uploadFile(context, file, AlbumSelection.albumId)
        }
    }

    DisposableEffect(Unit) {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // 앨범 선택 다이얼로그
    var isAlbumDialogOpen by remember { mutableStateOf(false) }

    if (isAlbumDialogOpen && viewModel.pendingDownloadUrl != null) {
        AlertDialog(
            onDismissRequest = { isAlbumDialogOpen = false },
            title = { Text("앨범 선택") },
            text = {
                Column {
                    if (viewModel.albumList.isEmpty()) {
                        Text("앨범 목록을 불러오는 중...")
                    } else {
                        viewModel.albumList.forEach { album ->
                            TextButton(onClick = {
                                isAlbumDialogOpen = false
                                startDownloadWithAlbum(
                                    context,
                                    viewModel.pendingDownloadUrl!!,
                                    album.id
                                )
                            }) { Text(album.name) }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { isAlbumDialogOpen = false }) { Text("취소") }
            }
        )
    }

    // WebView 다운로드 팝업
    viewModel.pendingDownloadUrl?.let { url ->
        AlertDialog(
            onDismissRequest = { viewModel.setPendingDownloadUrl(null) },
            title = { Text("사진 다운로드") },
            text = { Text("이 사진을 어떤 앨범에 저장할까요?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.loadAlbums { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    isAlbumDialogOpen = true
                }) { Text("앨범 선택") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setPendingDownloadUrl(null) }) { Text("취소") }
            }
        )
    }

    // WebView 화면
    viewModel.scannedUrl?.let { url ->
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?, request: WebResourceRequest?
                        ): Boolean {
                            val link = request?.url.toString()
                            if (link.matches(Regex(".*\\.(png|jpg|jpeg|gif|webp|zip|pdf)(\\?.*)?$"))) {
                                viewModel.setPendingDownloadUrl(link)
                                return true
                            }
                            return false
                        }
                    }
                    loadUrl(url)
                }
            }
        )
        return
    }

    // QR 스캐너 화면
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val scanner = BarcodeScanning.getClient()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                    val bitmap = imageProxy.toBitmap()
                    val inputImage = InputImage.fromBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)

                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            barcodes.firstOrNull()?.rawValue?.let { value ->
                                viewModel.setScannedUrl(value)
                                onResult(value)
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}


/** ---------------- 다운로드 시작 ---------------- **/

fun startDownloadWithAlbum(context: Context, url: String, albumId: String) {
    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request = DownloadManager.Request(Uri.parse(url))
        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        .setTitle("파일 다운로드")
        .setDescription(url.substringAfterLast("/"))
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substringAfterLast("/"))
    dm.enqueue(request)
}

/** ---------------- Uri → File 변환 ---------------- **/

fun uriToFile(context: Context, uri: Uri): File {
    val input = context.contentResolver.openInputStream(uri)!!
    val file = File(context.cacheDir, "downloaded_${System.currentTimeMillis()}.jpg")
    val output = FileOutputStream(file)
    input.copyTo(output)
    input.close()
    output.close()
    return file
}

/** ---------- 앨범 선택 상태 저장용 ---------- **/

object AlbumSelection {
    var albumId: Long? = null
}

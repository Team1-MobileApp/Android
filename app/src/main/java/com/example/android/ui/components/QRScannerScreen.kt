package com.example.android.ui.components

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
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
    var pendingDownloadUrl by mutableStateOf<String?>(null)
    var albumList by mutableStateOf<List<Album>>(emptyList())

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

    fun uploadFile(context: Context, file: File, albumId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uploaded = repository.uploadPhotoFile(file)
                repository.addPhotoToAlbum(uploaded.photoId, albumId)

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
    modifier: Modifier = Modifier.fillMaxSize(),
    onResult: (String) -> Unit = {}
) {

    // DownloadReceiver 등록
    val receiver = remember {
        DownloadReceiver { uri ->
            val file = uriToFile(context, uri)
            viewModel.uploadFile(
                context,
                file,
                AlbumSelection.albumId!!.toString()
            )
        }
    }

    DisposableEffect(Unit) {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // 앨범 선택 다이얼로그
    var isAlbumDialogOpen by remember { mutableStateOf(false) }

    // 다운로드 URL 저장
    var pendingDownloadUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // 다운로드 팝업
    if (pendingDownloadUrl != null) {
        AlertDialog(
            onDismissRequest = { viewModel.pendingDownloadUrl = null },
            title = { Text("사진 다운로드") },
            text = { Text("이 사진을 어떤 앨범에 저장할까요?") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDownloadUrl?.let { url ->
                        // 실제 다운로드 실행
                        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val request = DownloadManager.Request(Uri.parse(url))
                            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            .setTitle("파일 다운로드")
                            .setDescription(url.substringAfterLast("/"))
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substringAfterLast("/"))

                        dm.enqueue(request)
                    }
                    pendingDownloadUrl = null
                }) { Text("다운로드") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.pendingDownloadUrl = null }) { Text("취소") }
            }
        )
    }

    // QR 스캔 완료 → WebView 로드
    if (scannedUrl != null) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    // JS 인터페이스 추가
                    addJavascriptInterface(
                        WebAppInterface(context) { url ->
                            pendingDownloadUrl = url
                        },
                        "Android"
                    )

                    // URL 가로채기 및 다운로드 감지
                    webViewClient = object : WebViewClient() {

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val link = request?.url.toString()
                            if (link.matches(Regex(".*\\.(png|jpg|jpeg|gif|webp|zip|pdf)(\\?.*)?$"))) {
                                viewModel.pendingDownloadUrl = link
                                return true
                            }
                            return false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)

                            // JS 주입: 버튼 클릭 가로채기
                            val jsScript = """
                                (function() {
                                    try {
                                        const keywords = ['사진 다운로드','사진','Download Photo','Download','download'];
                                        document.querySelectorAll('button, a, [role="button"]').forEach(function(el){
                                            const text = (el.innerText || el.textContent || '').trim();
                                            if(keywords.some(k => text.includes(k))) {
                                                el.addEventListener('click', function(ev){
                                                    ev.preventDefault();
                                                    ev.stopPropagation();
                                                    try {
                                                        const img = el.querySelector('img');
                                                        if(img && img.src){ Android.downloadPhoto(img.src); return; }
                                                        let anchor = el.closest('a');
                                                        if(anchor && anchor.href){ Android.downloadPhoto(anchor.href); return; }
                                                        const imgs = Array.from(document.querySelectorAll('img')).sort((a,b)=> (b.naturalWidth*b.naturalHeight) - (a.naturalWidth*a.naturalHeight));
                                                        if(imgs.length>0 && imgs[0].src){ Android.downloadPhoto(imgs[0].src); return; }
                                                        Android.downloadPhoto(window.location.href);
                                                    } catch(e){ Android.downloadPhoto(window.location.href); }
                                                }, { once: true });
                                            }
                                        });
                                    } catch(e){}
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(jsScript, null)
                        }
                    }

                    loadUrl(scannedUrl!!)
                }
            }
        )
        return
    }

    // ===== 기본 QR 스캐너 화면 =====
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
            val scanner = BarcodeScanning.getClient()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply { setSurfaceProvider(previewView.surfaceProvider) }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                    val bitmap = imageProxy.toBitmap()
                    val inputImage = InputImage.fromBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)

                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            barcodes.firstOrNull()?.rawValue?.let { value ->
                                viewModel.scannedUrl = value
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

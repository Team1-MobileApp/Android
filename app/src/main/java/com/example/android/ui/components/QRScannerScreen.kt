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

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    onResult: (String) -> Unit = {}
) {

    var scannedUrl by remember { mutableStateOf<String?>(null) }

    // 다운로드 URL 저장
    var pendingDownloadUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // 다운로드 팝업
    if (pendingDownloadUrl != null) {
        AlertDialog(
            onDismissRequest = { pendingDownloadUrl = null },
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
                TextButton(onClick = { pendingDownloadUrl = null }) { Text("취소") }
            },
            title = { Text("파일 다운로드") },
            text = { Text("이 파일을 다운로드하시겠습니까?") }
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
                            val url = request?.url.toString()
                            if (url.matches(Regex(".*\\.(png|jpg|jpeg|gif|webp|zip|pdf)(\\?.*)?$", RegexOption.IGNORE_CASE))) {
                                pendingDownloadUrl = url
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
                                scannedUrl = value
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

package com.example.android.ui.components

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(
    private val context: Context,
    private val onDownloadRequest: (String) -> Unit
) {

    @JavascriptInterface
    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun requestDownload(url: String) {
        onDownloadRequest(url)
    }
}
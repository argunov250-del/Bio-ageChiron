package com.bioage.alpha

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/** Lightweight offline Three.js background. No camera gestures or touch controls. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CaucasusThreeBackground() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(AndroidColor.rgb(99, 135, 155))
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                settings.domStorageEnabled = false
                settings.setSupportZoom(false)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                isClickable = false
                setOnTouchListener { _, _ -> true }
                webChromeClient = WebChromeClient()
                loadUrl("file:///android_asset/three_caucasus/index.html")
            }
        },
        onRelease = { view -> view.stopLoading(); view.destroy() }
    )
}

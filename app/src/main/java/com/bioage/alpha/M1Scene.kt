package com.bioage.alpha

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Locale

/** Holds the single WebView so tabs can steer the scene without recreating it. */
class M1SceneController {
    var web: WebView? = null
    var loaded = false
    private var mode = "home"
    private var progress = 0f
    private var sent = ""

    fun update(newMode: String, newProgress: Float) {
        mode = newMode; progress = newProgress
        push()
    }

    fun push() {
        val view = web ?: return
        if (!loaded) return
        val call = String.format(Locale.US, "window.bioage&&window.bioage.set('%s',%.4f)", mode, progress)
        if (call == sent) return
        sent = call
        view.evaluateJavascript(call, null)
    }

    /** Reports what actually draws the mountains: "webgl", "2d:<reason>" or a not-loaded note. */
    fun status(result: (String) -> Unit) {
        val view = web
        if (view == null) { result("WebView недоступен, показан встроенный фон"); return }
        if (!loaded) { result("страница сцены ещё не загружена"); return }
        view.evaluateJavascript("window.bioage?window.bioage.status:'no-script'") { raw -> result((raw ?: "?").trim('"')) }
    }
}

/**
 * Offline Three.js mountains. Created once and kept across tabs; no gestures, one automatic camera.
 * The page falls back to a 2D canvas when WebGL is missing; if WebView itself cannot start,
 * the Compose backdrop below stays visible.
 */
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun M1MountainScene(controller: M1SceneController, mode: String, progress: Float) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            try {
                val web = WebView(context)
                web.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                web.settings.javaScriptEnabled = true
                web.settings.allowFileAccess = true
                web.settings.domStorageEnabled = false
                web.settings.setSupportZoom(false)
                web.isVerticalScrollBarEnabled = false
                web.isHorizontalScrollBarEnabled = false
                web.overScrollMode = View.OVER_SCROLL_NEVER
                web.isClickable = false
                web.isFocusable = false
                web.setOnTouchListener { _, _ -> true }
                web.webChromeClient = WebChromeClient()
                web.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        controller.loaded = true
                        controller.push()
                    }
                }
                controller.web = web
                web.loadUrl("file:///android_asset/three_caucasus/index.html")
                web as View
            } catch (error: Throwable) {
                View(context)
            }
        },
        update = { controller.update(mode, progress) },
        onRelease = { view ->
            if (view is WebView) { view.stopLoading(); view.destroy() }
            controller.web = null
            controller.loaded = false
        }
    )
}

/** Static dusk mountains under the WebView: visible while the page loads or if WebView is missing. */
@Composable
fun M1FallbackBackdrop() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(
            0f to Color(0xFF06131F), .24f to Color(0xFF1B3B5C), .36f to Color(0xFF6F7C9C), .44f to Color(0xFF12283B), 1f to Color(0xFF061521)))
        val far = Path().apply {
            moveTo(0f, h * .42f); lineTo(w * .12f, h * .36f); lineTo(w * .24f, h * .40f); lineTo(w * .40f, h * .33f)
            lineTo(w * .62f, h * .39f); lineTo(w * .82f, h * .34f); lineTo(w, h * .40f); lineTo(w, h * .5f); lineTo(0f, h * .5f); close()
        }
        drawPath(far, Color(0xFF51668A))
        val massif = Path().apply {
            moveTo(w * .02f, h * .46f); lineTo(w * .22f, h * .33f); lineTo(w * .36f, h * .20f); lineTo(w * .44f, h * .18f)
            lineTo(w * .52f, h * .24f); lineTo(w * .60f, h * .20f); lineTo(w * .68f, h * .22f); lineTo(w * .82f, h * .35f)
            lineTo(w * .98f, h * .46f); close()
        }
        drawPath(massif, Brush.verticalGradient(listOf(Color(0xFFF4F8FB), Color(0xFFB9C8DA), Color(0xFF2C3D52)), startY = h * .18f, endY = h * .46f))
        val front = Path().apply {
            moveTo(0f, h * .47f); lineTo(w * .18f, h * .43f); lineTo(w * .40f, h * .46f); lineTo(w * .66f, h * .42f)
            lineTo(w, h * .46f); lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(front, Color(0xFF0B1B2A))
    }
}

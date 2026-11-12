package com.bioage.alpha

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

/** Always available low-cost backdrop if the WebView/Three renderer is unavailable. */
@Composable
fun AlpineFallback9() {
    Canvas(Modifier.fillMaxSize()) {
        val w=size.width; val h=size.height
        drawRect(Brush.verticalGradient(listOf(Color(0xFF18394F),Color(0xFF648BA0),Color(0xFF9AB3BE),Color(0xFF183549))))
        val peaks=listOf(.0f to .70f,.12f to .56f,.22f to .62f,.36f to .38f,.48f to .61f,.64f to .32f,.77f to .56f,.91f to .48f,1f to .64f)
        val back=Path().apply { moveTo(0f,h); peaks.forEach { (x,y)->lineTo(x*w,y*h) };lineTo(w,h);close() }
        drawPath(back,Color(0xFFCADDE0))
        val front=Path().apply {moveTo(0f,h);lineTo(0f,h*.75f);lineTo(w*.2f,h*.69f);lineTo(w*.42f,h*.78f);lineTo(w*.68f,h*.67f);lineTo(w,h*.73f);lineTo(w,h);close()}
        drawPath(front,Color(0xFF506E7E))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent,Color(0xAA0A2333)),startY=h*.48f,endY=h))
    }
}

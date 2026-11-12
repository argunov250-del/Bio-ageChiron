package com.bioage.alpha

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlin.math.cos
import kotlin.math.sin

/** Alpha 0.17: real GLB terrain, baked rock/snow lighting, distant ridges and clouds.
 * Alpha 4.00: camera re-framed for the larger twin-peak model (26 x 18, peaks ~9 high):
 * it orbits a point between the two summits so both stay in view above the home card. */
@Composable
fun Caucasus3DScene(cameraAngle: Float) {
    val engine = rememberEngine()
    val loader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) { position = Position(x = 0.4f, y = 10.5f, z = 32.8f) }
    val nodes = rememberNodes { add(ModelNode(modelInstance = loader.createModelInstance("models/elbrus_terrain.glb"))) }
    val smoothAngle by animateFloatAsState(targetValue = cameraAngle, animationSpec = tween(220), label = "camera-pan")
    LaunchedEffect(smoothAngle) {
        val radians = Math.toRadians(smoothAngle.toDouble() * 0.55)
        val radius = 35.0
        val cx = 0.4; val cz = -2.2  // orbit centre between the two summits
        cameraNode.position = Position(x = (cx + sin(radians)*radius).toFloat(), y = 10.5f, z = (cz + cos(radians)*radius).toFloat())
        cameraNode.lookAt(Position(x = cx.toFloat(), y = -2.5f, z = cz.toFloat()))
    }
    Scene(modifier = Modifier.fillMaxSize(), engine = engine, modelLoader = loader, cameraNode = cameraNode, childNodes = nodes)
}

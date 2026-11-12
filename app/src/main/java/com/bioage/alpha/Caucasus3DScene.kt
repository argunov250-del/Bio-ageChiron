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

/** Alpha 0.17: real GLB terrain, baked rock/snow lighting, distant ridges and clouds. */
@Composable
fun Caucasus3DScene(cameraAngle: Float) {
    val engine = rememberEngine()
    val loader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) { position = Position(x = 0f, y = 4.4f, z = 16.5f) }
    val nodes = rememberNodes { add(ModelNode(modelInstance = loader.createModelInstance("models/elbrus_terrain.glb"))) }
    val smoothAngle by animateFloatAsState(targetValue = cameraAngle, animationSpec = tween(220), label = "camera-pan")
    LaunchedEffect(smoothAngle) {
        val radians = Math.toRadians(smoothAngle.toDouble() * 0.55)
        val radius = 16.5
        cameraNode.position = Position(x = (sin(radians)*radius).toFloat(), y = 4.4f, z = (cos(radians)*radius).toFloat())
        cameraNode.lookAt(Position(x = 0f, y = 0.55f, z = -1.2f))
    }
    Scene(modifier = Modifier.fillMaxSize(), engine = engine, modelLoader = loader, cameraNode = cameraNode, childNodes = nodes)
}

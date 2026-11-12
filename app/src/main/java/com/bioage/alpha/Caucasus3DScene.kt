package com.bioage.alpha

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

/** Alpha 0.4: actual GLB mesh with two summits and per-vertex snow/rock colors.
 * Uses the same SceneView API as Alpha 0.3 to minimize integration risk.
 */
@Composable
fun Caucasus3DScene(cameraAngle: Float) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 4.4f, z = 16.5f)
    }
    val childNodes = rememberNodes {
        add(ModelNode(modelInstance = modelLoader.createModelInstance("models/elbrus_terrain.glb")))
    }
    LaunchedEffect(cameraAngle) {
        val radians = Math.toRadians(cameraAngle.toDouble() * 0.55)
        val radius = 16.5
        cameraNode.position = Position(
            x = (sin(radians) * radius).toFloat(),
            y = 4.4f,
            z = (cos(radians) * radius).toFloat()
        )
        cameraNode.lookAt(Position(x = 0f, y = 0.55f, z = -1.2f))
    }
    Scene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        cameraNode = cameraNode,
        childNodes = childNodes
    )
}

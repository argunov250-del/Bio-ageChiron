package com.bioage.alpha

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

/** Actual GLB triangle mesh rendered with Google Filament through SceneView. */
@Composable
fun Caucasus3DScene(cameraAngle: Float) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 1.5f, z = 7.5f)
    }
    val childNodes = rememberNodes {
        add(ModelNode(modelInstance = modelLoader.createModelInstance("models/elbrus_terrain.glb")))
    }
    LaunchedEffect(cameraAngle) {
        cameraNode.position = Position(x = cameraAngle / 22f, y = 1.5f, z = 7.5f)
        cameraNode.lookAt(Position(x = 0f, y = 0.5f, z = 0f))
    }
    Scene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        cameraNode = cameraNode,
        childNodes = childNodes
    )
}

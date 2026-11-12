package com.bioage.alpha

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.filament.Skybox
import io.github.sceneview.SceneView
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlin.math.cos
import kotlin.math.sin

/** Alpha 6.00: slow, self-running panorama. No user camera controls.
 * Narrow +-7 degree orbit retains the twin peaks in the 4.00 framing. */
@Composable
fun Caucasus3DScene() {
    val engine = rememberEngine()
    val loader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) { position = Position(x = 0.4f, y = 10.5f, z = 32.8f) }
    // SceneView's default skybox is solid black. Use the light lower-sky colour instead, so any
    // part of the frame not covered by the GLB backdrop (e.g. below the terrain's front edge)
    // is never black. Created once (remember) - the scene recomposes every animation frame.
    val environmentLoader = rememberEnvironmentLoader(engine)
    val skyEnvironment = remember(environmentLoader) {
        val base = SceneView.createEnvironment(environmentLoader)
        base.skybox?.let { engine.destroySkybox(it) }
        base.copy(skybox = Skybox.Builder().color(0.69f, 0.80f, 0.84f, 1.0f).build(engine))
    }
    DisposableEffect(skyEnvironment) { onDispose { environmentLoader.destroyEnvironment(skyEnvironment) } }
    val nodes = rememberNodes { add(ModelNode(modelInstance = loader.createModelInstance("models/elbrus_terrain.glb"))) }
    val transition = rememberInfiniteTransition(label = "alpine-panorama")
    val angle by transition.animateFloat(
        initialValue = -7f, targetValue = 7f,
        animationSpec = infiniteRepeatable(animation = tween(28000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "slow-camera-orbit"
    )
    LaunchedEffect(angle) {
        val radians = Math.toRadians(angle.toDouble())
        val radius = 35.0
        val cx = 0.4; val cz = -2.2
        cameraNode.position = Position(
            x = (cx + sin(radians) * radius).toFloat(),
            y = 10.5f,
            z = (cz + cos(radians) * radius).toFloat()
        )
        cameraNode.lookAt(Position(x = cx.toFloat(), y = -2.5f, z = cz.toFloat()))
    }
    Scene(modifier = Modifier.fillMaxSize(), engine = engine, modelLoader = loader, environmentLoader = environmentLoader,
        environment = skyEnvironment, cameraNode = cameraNode, childNodes = nodes)
}

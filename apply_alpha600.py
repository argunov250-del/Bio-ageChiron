from pathlib import Path
import re
root=Path.cwd(); base=root/'app/src/main/java/com/bioage/alpha'
main=base/'MainActivity.kt'; features=base/'Alpha500Features.kt'; scene=base/'Caucasus3DScene.kt'; gradle=root/'app/build.gradle.kts'
for p in [main,features,scene,gradle]:
    if not p.exists(): raise SystemExit(f'Missing {p}; run from repository root')
texts={p:p.read_text(encoding='utf-8') for p in [main,features,scene,gradle]}
def replace_once(s,a,b,label):
    if s.count(a)!=1: raise SystemExit(f'Anchor mismatch ({label}): {s.count(a)} matches. No files changed.')
    return s.replace(a,b)
m=texts[main]
m=replace_once(m,'    var camera by remember { mutableFloatStateOf(0f) }\n','', 'camera state')
m=replace_once(m,'Caucasus3DScene(camera)','Caucasus3DScene()', 'scene call')
m=replace_once(m,'Home500(prefs, steps, sleep, done, done + historyRefresh, camera, { camera = it }) { chatOpen = true }','Home600(prefs, steps, sleep, done, done + historyRefresh) { chatOpen = true }','home call')
m=replace_once(m,'ALPHA 5.00','ALPHA 6.00','header')
m=replace_once(m,'Bio-Age Alpha 5.00','Bio-Age Alpha 6.00','profile')
f=texts[features]
# Keep existing Home500 function for backwards compatibility; new Home600 has no camera controls.
new='''\n/** Alpha 6.00: the landscape moves automatically. No camera buttons or gestures. */\n@Composable\nfun Home600(prefs: SharedPreferences, steps: String, sleep: String, done: Int, refresh: Int, onChat: () -> Unit) {\n    Hero300(steps, sleep, done)\n    Spacer(Modifier.height(12.dp))\n    AlpineRoute400(prefs, refresh)\n    Spacer(Modifier.height(10.dp))\n    GlassPanel300(Modifier.fillMaxWidth()) {\n        Text("ХИРОН · СЕГОДНЯ", color = ice500, fontSize = 12.sp, fontWeight = FontWeight.Bold)\n        val hours = sleep.replace(',', '.').toDoubleOrNull()\n        val message = when {\n            hours != null && hours < 6.0 -> "Сон был коротким. Выбирай комфортный темп и время для отдыха."\n            done == 3 -> "Все три привычки отмечены. Восстановление — тоже часть прогресса."\n            else -> "Записывай сон и движение, чтобы замечать изменения со временем."\n        }\n        Text(message, color = ink500, fontSize = 13.sp, lineHeight = 19.sp)\n        OutlinedButton(onClick = onChat, modifier = Modifier.fillMaxWidth()) { Text("Диалог с Хироном") }\n    }\n}\n'''
if 'fun Home600(' in f: raise SystemExit('Home600 already exists')
f+='\n'+new
# Camera uses Compose infinite transition; preserve 4.00 framing and GLB, no manual camera control.
s='''package com.bioage.alpha\n\nimport androidx.compose.animation.core.*\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.runtime.*\nimport androidx.compose.ui.Modifier\nimport io.github.sceneview.Scene\nimport io.github.sceneview.math.Position\nimport io.github.sceneview.node.ModelNode\nimport io.github.sceneview.rememberCameraNode\nimport io.github.sceneview.rememberEngine\nimport io.github.sceneview.rememberModelLoader\nimport io.github.sceneview.rememberNodes\nimport kotlin.math.cos\nimport kotlin.math.sin\n\n/** Alpha 6.00: slow, self-running panorama. No user camera controls.\n * Narrow +-7 degree orbit retains the twin peaks in the 4.00 framing. */\n@Composable\nfun Caucasus3DScene() {\n    val engine = rememberEngine()\n    val loader = rememberModelLoader(engine)\n    val cameraNode = rememberCameraNode(engine) { position = Position(x = 0.4f, y = 10.5f, z = 32.8f) }\n    val nodes = rememberNodes { add(ModelNode(modelInstance = loader.createModelInstance("models/elbrus_terrain.glb"))) }\n    val transition = rememberInfiniteTransition(label = "alpine-panorama")\n    val angle by transition.animateFloat(\n        initialValue = -7f, targetValue = 7f,\n        animationSpec = infiniteRepeatable(animation = tween(28000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),\n        label = "slow-camera-orbit"\n    )\n    LaunchedEffect(angle) {\n        val radians = Math.toRadians(angle.toDouble())\n        val radius = 35.0\n        val cx = 0.4; val cz = -2.2\n        cameraNode.position = Position(\n            x = (cx + sin(radians) * radius).toFloat(),\n            y = 10.5f,\n            z = (cz + cos(radians) * radius).toFloat()\n        )\n        cameraNode.lookAt(Position(x = cx.toFloat(), y = -2.5f, z = cz.toFloat()))\n    }\n    Scene(modifier = Modifier.fillMaxSize(), engine = engine, modelLoader = loader, cameraNode = cameraNode, childNodes = nodes)\n}\n'''
g=texts[gradle]
g=re.sub(r'versionCode\s*=\s*500\b','versionCode = 600',g)
g=g.replace('versionName = "5.00.0-alpha"','versionName = "6.00.0-alpha"')
if 'versionCode = 600' not in g or 'versionName = "6.00.0-alpha"' not in g: raise SystemExit('Version anchor missing; no files changed')
for p,content in [(main,m),(features,f),(scene,s),(gradle,g)]: p.write_text(content,encoding='utf-8')
print('Alpha 6.00 patch applied. Review diff and build APK.')

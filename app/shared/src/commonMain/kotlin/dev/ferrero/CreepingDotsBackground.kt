package dev.ferrero

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@Composable
fun CreepingDotsBackground() {
    val isDark = MaterialTheme.colorScheme.background == Brand.Charcoal
    val dotColor = if (isDark) Color(0xFFFFE082) else Color(0xFFFBC02D)
    
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val dotsState = remember { DotSystem() }

    val tick = remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        // Wait for first valid size
        while (canvasSize == IntSize.Zero) {
            delay(64)
        }
        dotsState.initialize(canvasSize)

        var lastTime = withFrameMillis { it }
        while (true) {
            // Use withFrameMillis to synchronize with display refresh
            val currentTime = withFrameMillis { it }
            val deltaTime = (currentTime - lastTime) / 1000f
            lastTime = currentTime

            // Update dot positions and states
            dotsState.update(canvasSize, deltaTime)
            tick.value = currentTime
            
            // Reduce framerate to roughly 30fps to improve performance
            delay(64)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize().onSizeChanged { 
        if (it != IntSize.Zero) {
            if (canvasSize != IntSize.Zero && it != canvasSize) {
                dotsState.onSizeChanged(canvasSize, it)
            }
            canvasSize = it
        }
    }) {
        // Trigger redraw on tick
        tick.value

        dotsState.dots.forEach { dot ->
            if (dot.history.size < 2) return@forEach

            // Pulse/glow for non-dancing dots (seeking state)
            if (dot.state == DotState.MOVING) {
                val timeSec = tick.value / 1000f
                val period = 2.0f // 4x slower interval than original 0.5s
                val cycleTime = (timeSec + dot.glowOffset) % period
                
                // 3 parts off (1.5s), 1 part on (0.5s)
                if (cycleTime > 1.5f) {
                    val pulseProgress = (cycleTime - 1.5f) / 0.5f
                    val pulse = sin(pulseProgress * PI.toFloat())
                    drawCircle(
                        color = Color.White.copy(alpha = pulse * 0.1f), // Pure white, much fainter
                        radius = 8f + pulse * 12f,
                        center = dot.history[0]
                    )
                }
            }

            val snakePath = Path()
            val trailSegments = 10
            val trailPaths = List(trailSegments) { Path() }
            
            var totalDist = 0f
            val points = dot.history
            snakePath.moveTo(points[0].x, points[0].y)
            
            for (i in 1 until points.size) {
                val p1 = points[i-1]
                val p2 = points[i]
                val d = (p2 - p1).getDistance()
                if (d <= 0f) continue
                
                val startDist = totalDist
                totalDist += d
                
                // Snake part (0-6px)
                if (startDist < 6f) {
                    val s = max(startDist, 0f)
                    val e = min(totalDist, 6f)
                    if (s < e) {
                        val ratioE = (e - startDist) / d
                        val pE = p1 + (p2 - p1) * ratioE
                        snakePath.lineTo(pE.x, pE.y)
                    }
                }
                
                // Trail part (6-26px)
                if (totalDist > 6f && startDist < 26f) {
                    for (j in 0 until trailSegments) {
                        val segStartDist = 6f + j * 2f
                        val segEndDist = 6f + (j + 1) * 2f
                        
                        val s = max(max(startDist, 6f), segStartDist)
                        val e = min(min(totalDist, 26f), segEndDist)
                        
                        if (s < e) {
                            val ratioS = (s - startDist) / d
                            val ratioE = (e - startDist) / d
                            val pS = p1 + (p2 - p1) * ratioS
                            val pE = p1 + (p2 - p1) * ratioE
                            
                            trailPaths[j].moveTo(pS.x, pS.y)
                            trailPaths[j].lineTo(pE.x, pE.y)
                        }
                    }
                }
                if (totalDist >= 26f) break
            }
            
            // Draw trail segments with fading alpha
            for (j in 0 until trailSegments) {
                val alpha = 0.3f * (1f - (j + 0.5f) / trailSegments)
                drawPath(
                    path = trailPaths[j],
                    color = dotColor.copy(alpha = alpha),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
            }
            
            // Draw snake
            drawPath(
                path = snakePath,
                color = dotColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }
    }
}

class DotSystem {
    val dots = mutableListOf<Dot>()
    private var initialized = false

    fun initialize(size: IntSize) {
        if (initialized) return
        repeat(20) {
            dots.add(
                Dot(
                    Random.nextFloat() * size.width,
                    Random.nextFloat() * size.height,
                    Random.nextFloat() * 2 * PI.toFloat()
                )
            )
        }
        initialized = true
    }

    fun onSizeChanged(oldSize: IntSize, newSize: IntSize) {
        if (!initialized || oldSize == IntSize.Zero || newSize == IntSize.Zero) return
        
        val scaleX = newSize.width.toFloat() / oldSize.width
        val scaleY = newSize.height.toFloat() / oldSize.height
        
        dots.forEach { dot ->
            dot.x *= scaleX
            dot.y *= scaleY
            // Scale history as well
            for (i in dot.history.indices) {
                val offset = dot.history[i]
                dot.history[i] = Offset(offset.x * scaleX, offset.y * scaleY)
            }
        }
    }

    fun update(size: IntSize, deltaTime: Float) {
        val speed = 5f
        val interactionRange = 100f
        
        val interactionRangeSq = interactionRange * interactionRange
        
        // Check for interactions
        for (i in dots.indices) {
            for (j in i + 1 until dots.size) {
                val d1 = dots[i]
                val d2 = dots[j]
                if (d1.state == DotState.MOVING && d2.state == DotState.MOVING) {
                    val dx = d1.x - d2.x
                    val dy = d1.y - d2.y
                    val distSq = dx * dx + dy * dy
                    if (distSq < interactionRangeSq) {
                        val axis = atan2(d2.y - d1.y, d2.x - d1.x)
                        val seed = Random.nextFloat() * 100f
                        val speedMult = 1f + (Random.nextFloat() - 0.5f) * 0.4f
                        d1.startGreeting(d2, axis, false, seed, speedMult)
                        d2.startGreeting(d1, axis, true, seed, speedMult)
                    }
                }
            }
        }

        dots.forEach { it.update(size, deltaTime, speed) }
    }
}

enum class DotState { MOVING, GREETING, DANCING }

class Dot(var x: Float, var y: Float, var angle: Float) {
    var state = DotState.MOVING
    var timer = 0f
    val history = mutableListOf<Offset>()
    var targetAngle = angle
    var angleChangeTimer = 0f
    val glowOffset = Random.nextFloat() * 2f
    
    var danceAxis = 0f
    var isReflected = false
    var danceSeed = 0f
    var danceSpeedMultiplier = 1f
    
    init {
        history.add(Offset(x, y))
    }

    fun startGreeting(other: Dot, axis: Float, reflected: Boolean, seed: Float, speedMult: Float) {
        state = DotState.GREETING
        timer = 0.75f // Half of 1.5s
        danceAxis = axis
        isReflected = reflected
        danceSeed = seed
        danceSpeedMultiplier = speedMult
    }

    fun update(size: IntSize, deltaTime: Float, speed: Float) {
        when (state) {
            DotState.MOVING -> {
                // Meandering
                angleChangeTimer -= deltaTime
                if (angleChangeTimer <= 0) {
                    targetAngle = angle + (Random.nextFloat() - 0.5f) * PI.toFloat() / 2
                    angleChangeTimer = Random.nextFloat() * 2 + 1
                }
                
                // Smoothly turn towards target angle
                val angleDiff = (targetAngle - angle)
                angle += angleDiff * deltaTime
                
                x += cos(angle) * speed * deltaTime
                y += sin(angle) * speed * deltaTime

                // Bounce off edges
                if (x < 0 || x > size.width) {
                    angle = PI.toFloat() - angle
                    targetAngle = angle
                    x = x.coerceIn(0f, size.width.toFloat())
                }
                if (y < 0 || y > size.height) {
                    angle = -angle
                    targetAngle = angle
                    y = y.coerceIn(0f, size.height.toFloat())
                }

                history.add(0, Offset(x, y))
            }
            DotState.GREETING -> {
                timer -= deltaTime
                if (timer <= 0) {
                    state = DotState.DANCING
                    timer = 1.5f // Half of 3s
                }
                history.add(0, Offset(x, y))
            }
            DotState.DANCING -> {
                timer -= deltaTime
                if (timer <= 0) {
                    state = DotState.MOVING
                    // Change angle after dancing
                    angle = Random.nextFloat() * 2 * PI.toFloat()
                    targetAngle = angle
                }
                
                // Dance: small wiggle or circular motion
                val danceSpeed = 30f // Slower velocity
                val phase = timer * (5f * danceSpeedMultiplier) + danceSeed // Slower angular velocity + randomness
                
                var dx = cos(phase) * danceSpeed * deltaTime
                var dy = sin(phase) * danceSpeed * deltaTime
                
                if (isReflected) {
                    // Reflect over the perpendicular bisector of the axis connecting them
                    val alpha = danceAxis + PI.toFloat() / 2f
                    val cos2a = cos(2 * alpha)
                    val sin2a = sin(2 * alpha)
                    val rx = dx * cos2a + dy * sin2a
                    val ry = dx * sin2a - dy * cos2a
                    dx = rx
                    dy = ry
                }
                
                x += dx
                y += dy
                history.add(0, Offset(x, y))
            }
        }

        // Limit history size to roughly 26px total length
        // Since we move at 5px/s, and we want 26px, we need history for ~5.2s.
        // At 60fps, that's ~312 points.
        // But it's better to limit by cumulative distance.
        var totalDist = 0f
        var lastValidIndex = history.size - 1
        for (i in 1 until history.size) {
            totalDist += (history[i] - history[i-1]).getDistance()
            if (totalDist > 30f) { // a bit more than 26 to be safe
                lastValidIndex = i
                break
            }
        }
        if (lastValidIndex < history.size - 1) {
            // Remove points beyond what we need
            while (history.size > lastValidIndex + 1) {
                history.removeAt(history.size - 1)
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 28
) {
    val transition = rememberInfiniteTransition(label = "audio_bars_infinite")

    val anim1 by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim1"
    )

    val anim2 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(620, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim2"
    )

    val anim3 by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim3"
    )

    val anim4 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim4"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val totalWidth = size.width
        val totalHeight = size.height
        val barSpacing = 4f
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = ((totalWidth - totalSpacing) / barCount).coerceAtLeast(3f)

        val brush = Brush.verticalGradient(
            colors = listOf(primaryColor, secondaryColor),
            startY = 0f,
            endY = totalHeight
        )

        for (i in 0 until barCount) {
            val factor = when (i % 4) {
                0 -> anim1
                1 -> anim2
                2 -> anim3
                else -> anim4
            }

            // Wave curve shaping: center bars are naturally taller
            val normalizedDistFromCenter = kotlin.math.abs(i - (barCount / 2f)) / (barCount / 2f)
            val envelope = (1.0f - normalizedDistFromCenter * 0.4f).coerceIn(0.2f, 1.0f)

            val currentRatio = if (isPlaying) {
                (factor * envelope).coerceIn(0.08f, 1.0f)
            } else {
                0.08f
            }

            val barHeight = (totalHeight * currentRatio).coerceAtLeast(4f)
            val x = i * (barWidth + barSpacing)
            val y = totalHeight - barHeight

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}

package com.hackathon.dinemate

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DineMateColors {
    val DineRed = Color(0xFFFF6B6B)
    val MateBlue = Color(0xFF4ECDC4)
    val IconGradientStart = Color(0xFFFF6B6B)
    val IconGradientEnd = Color(0xFFFF8E53)
    val TextDark = Color(0xFF2C3E50)
    val White = Color.White
}

@Composable
fun DineMateIconOnly(
    modifier: Modifier = Modifier,
    size: LogoSize = LogoSize.Medium,
    showAnimation: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_animation")

    val shineProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine"
    )

    val logoModifier = modifier
        .let { mod ->
            onClick?.let { mod.clickable { it() } } ?: mod
        }

    Box(
        modifier = logoModifier
            .size(size.iconSize)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(size.iconRadius),
                spotColor = DineMateColors.IconGradientStart.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(size.iconRadius))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        DineMateColors.IconGradientStart,
                        DineMateColors.IconGradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showAnimation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            start = Offset(
                                x = (shineProgress * size.iconSize.value * 2) - size.iconSize.value,
                                y = 0f
                            ),
                            end = Offset(
                                x = (shineProgress * size.iconSize.value * 2),
                                y = size.iconSize.value
                            )
                        )
                    )
            )
        }

        Text(
            text = "🍽️",
            fontSize = size.emojiSize,
            textAlign = TextAlign.Center
        )
    }
}

enum class LogoSize(
    val iconSize: androidx.compose.ui.unit.Dp,
    val iconRadius: androidx.compose.ui.unit.Dp,
    val textSize: androidx.compose.ui.unit.TextUnit,
    val emojiSize: androidx.compose.ui.unit.TextUnit,
    val happyEmojiSize: androidx.compose.ui.unit.TextUnit,
    val spacing: androidx.compose.ui.unit.Dp
) {
    Small(
        iconSize = 40.dp,
        iconRadius = 10.dp,
        textSize = 18.sp,
        emojiSize = 20.sp,
        happyEmojiSize = 14.sp,
        spacing = 8.dp
    ),
    Medium(
        iconSize = 60.dp,
        iconRadius = 15.dp,
        textSize = 28.sp,
        emojiSize = 28.sp,
        happyEmojiSize = 18.sp,
        spacing = 12.dp
    ),
    Large(
        iconSize = 80.dp,
        iconRadius = 20.dp,
        textSize = 36.sp,
        emojiSize = 36.sp,
        happyEmojiSize = 24.sp,
        spacing = 16.dp
    )
}

@Preview(showBackground = true)
@Composable
fun DineMateLogoPreview() {
    DineMateIconOnly(
        size = LogoSize.Medium,
        showAnimation = true
    )
}
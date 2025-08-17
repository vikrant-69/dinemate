package com.hackathon.dinemate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun GoogleSignInScreen() {
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatAnimation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "float1"
    )

    val floatAnimation2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "float2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        // Floating decoration circles
        FloatingCircle(
            color = Color(0xFFFF6B6B).copy(alpha = 0.1f),
            size = 40.dp,
            offset = Offset(0.1f, 0.2f),
            animationOffset = floatAnimation1
        )

        FloatingCircle(
            color = Color(0xFF4ECDC4).copy(alpha = 0.1f),
            size = 60.dp,
            offset = Offset(0.85f, 0.7f),
            animationOffset = floatAnimation2
        )

        FloatingCircle(
            color = Color(0xFF4285F4).copy(alpha = 0.1f),
            size = 30.dp,
            offset = Offset(0.8f, 0.4f),
            animationOffset = floatAnimation1 * 0.5f
        )

        // Main content
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                // Logo with shimmer effect
                DineMateLogo()

                Spacer(modifier = Modifier.height(24.dp))

                // App name
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFFFF6B6B))) {
                            append("Dine")
                        }
                        withStyle(SpanStyle(color = Color(0xFF4ECDC4))) {
                            append("Mate")
                        }
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tagline
                Text(
                    text = "Your trusted dining companion",
                    color = Color(0xFF6B7280),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Welcome text
                Text(
                    text = "Welcome!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sign in to discover amazing restaurants tailored just for you",
                    color = Color(0xFF6B7280),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Google Sign-In Button
                GoogleSignInButton(
                    isLoading = isLoading,
                    isSuccess = isSuccess,
                    onClick = {
                        if (!isLoading) {
                            isLoading = true
                            // Simulate sign-in process
                            LaunchedEffect(isLoading) {
                                delay(2000)
                                isSuccess = true
                                delay(1500)
                                // Reset state (in real app, navigate to main screen)
                                isLoading = false
                                isSuccess = false
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFE5E7EB)
                    )
                    Text(
                        text = "Secure & Fast",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFE5E7EB)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Terms text
                Text(
                    text = "By continuing, you agree to our Terms of Service and Privacy Policy",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun DineMateLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAnimation by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ), label = "shimmer"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF6B6B),
                        Color(0xFFFF8E53)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🍽️",
            fontSize = 36.sp
        )

        // Shimmer overlay
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
                        start = androidx.compose.ui.geometry.Offset(
                            shimmerAnimation * 200,
                            shimmerAnimation * 200
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            (shimmerAnimation + 0.5f) * 200,
                            (shimmerAnimation + 0.5f) * 200
                        )
                    )
                )
        )
    }
}

@Composable
fun GoogleSignInButton(
    isLoading: Boolean,
    isSuccess: Boolean,
    onClick: @Composable () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSuccess -> Color(0xFF4285F4)
            else -> Color.White
        },
        animationSpec = tween(300), label = "bg_color"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSuccess -> Color.White
            else -> Color(0xFF374151)
        },
        animationSpec = tween(300), label = "text_color"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSuccess -> Color(0xFF4285F4)
            else -> Color(0xFFE5E7EB)
        },
        animationSpec = tween(300), label = "border_color"
    )

    Button(
        onClick = { onClick },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSuccess) 8.dp else 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF4285F4)
                )
            } else {

                // Google Icon (you would use actual Google logo here)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = when {
                    isLoading -> "Signing in..."
                    isSuccess -> "Success! Redirecting..."
                    else -> "Continue with Google"
                },
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FloatingCircle(
    color: Color,
    size: Dp,
    offset: Offset,
    animationOffset: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopStart)
            .offset(
                x = (offset.x * 400).dp,
                y = (offset.y * 800 + animationOffset).dp
            )
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    color = color,
                    shape = CircleShape
                )
        )
    }
}

data class Offset(val x: Float, val y: Float)

@Preview(showBackground = true)
@Composable
fun GoogleSignInScreenPreview() {
    MaterialTheme {
        GoogleSignInScreen()
    }
}
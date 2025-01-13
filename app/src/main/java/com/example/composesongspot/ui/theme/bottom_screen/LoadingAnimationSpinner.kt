package com.example.composesongspot.ui.theme.bottom_screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.composesongspot.R
import com.example.composesongspot.ui.theme.Typography

@Composable
fun LoadingAnimationSpinner() {
        Box(modifier = Modifier.fillMaxSize())
        {
            val infiniteTransition = rememberInfiniteTransition(label = "Loading Animation")
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ), label = ""
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.recorded),
                        style = Typography.bodyMedium.copy(
                            Color.Black,
                            shadow = Shadow(
                                color = Color.White,
                                offset = Offset(1f, 1f),
                                blurRadius = 5f
                            )
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Canvas(modifier = Modifier
                        .align(Alignment.Center)
                        .size(150.dp),
                        onDraw = {
                            drawCircle(
                                color = Color.White,
                                style = Stroke(width = 5f)
                            )
                        }
                    )
                    Canvas(modifier = Modifier
                        .align(Alignment.Center)
                        .size(150.dp),
                        onDraw = {
                            drawArc(
                                color = Color.DarkGray,
                                style = Stroke(
                                    width = 5f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                ),
                                startAngle = angle,
                                sweepAngle = 360 / 3f,
                                useCenter = false
                            )
                        }
                    )
                }
            }
        }
    }
package com.clusterview.demo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun VisualizationMapView(
    clusters: List<Cluster>,
    onBack: () -> Unit,
    onClusterClick: (Cluster) -> Unit
) {
    // State for the entire view (Zoom/Pan)
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Map cluster IDs to positions so we can drag them
    val nodePositions = remember {
        mutableStateMapOf<Int, Offset>().apply {
            clusters.forEachIndexed { index, cluster ->
                // Initial spiral-like placement so they aren't all in a line
                this[cluster.id] = Offset(400f + (index * 150f), 300f + (if(index % 2 == 0) 100f else -100f))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(VelvetTheme.DeepOcean)) {
        // --- 1. THE TRANSFORMABLE LAYER (Canvas + Nodes) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale *= zoom
                        offset += pan
                    }
                }
                .graphicsLayer(
                    scaleX = scale, scaleY = scale,
                    translationX = offset.x, translationY = offset.y
                )
        ) {
            // --- DRAW LINES (EDGES) ---
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centers = nodePositions.values.toList()
                for (i in centers.indices) {
                    for (j in i + 1 until centers.size) {
                        // Draw a subtle glowing line between all nodes
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(VelvetTheme.SunsetCoral.copy(alpha = 0.3f), VelvetTheme.SlateBlue.copy(alpha = 0.3f))
                            ),
                            start = centers[i] + Offset(50f, 50f), // Adjust for node center
                            end = centers[j] + Offset(50f, 50f),
                            strokeWidth = 2f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // --- DRAW NODES ---
            clusters.forEach { cluster ->
                val pos = nodePositions[cluster.id] ?: Offset.Zero

                Box(
                    modifier = Modifier
                        .offset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }
                        .size(120.dp)
                        .graphicsLayer {
                            shadowElevation = 20f
                            shape = CircleShape
                            clip = true
                        }
                        .background(
                            Brush.radialGradient(
                                colors = listOf(VelvetTheme.SlateBlue, VelvetTheme.DeepOcean)
                            )
                        )
                        .pointerInput(cluster.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                nodePositions[cluster.id] = (nodePositions[cluster.id] ?: Offset.Zero) + dragAmount
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = cluster.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${cluster.fileCount} nodes",
                            color = VelvetTheme.SunsetCoral,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // --- 2. UI OVERLAY ---
        TopAppBar(
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(backgroundColor = VelvetTheme.CrimsonRed),
                shape = CircleShape
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("EXIT TO CORE", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
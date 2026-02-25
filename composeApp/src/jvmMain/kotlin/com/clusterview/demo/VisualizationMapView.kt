package com.clusterview.demo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    clusters: List<Cluster>, // This list must come from your DB
    onBack: () -> Unit,
    onClusterClick: (Cluster) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Map to track positions of REAL cluster IDs
    val nodePositions = remember(clusters) {
        mutableStateMapOf<Int, Offset>().apply {
            clusters.forEachIndexed { index, cluster ->
                // Spread nodes in a circle based on how many clusters exist
                val angle = index * (2 * Math.PI / clusters.size)
                val radius = 250f
                this[cluster.id] = Offset(
                    x = 500f + (radius * Math.cos(angle)).toFloat(),
                    y = 350f + (radius * Math.sin(angle)).toFloat()
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(VelvetTheme.DeepOcean)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale *= zoom
                        offset += pan
                    }
                }
                .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
        ) {
            // --- DYNAMIC LINES ---
            Canvas(modifier = Modifier.fillMaxSize()) {
                val clusterIds = clusters.map { it.id }
                for (i in clusterIds.indices) {
                    for (j in i + 1 until clusterIds.size) {
                        val p1 = nodePositions[clusterIds[i]] ?: Offset.Zero
                        val p2 = nodePositions[clusterIds[j]] ?: Offset.Zero
                        drawLine(
                            color = VelvetTheme.SunsetCoral.copy(alpha = 0.2f),
                            start = p1 + Offset(60.dp.toPx(), 60.dp.toPx()),
                            end = p2 + Offset(60.dp.toPx(), 60.dp.toPx()),
                            strokeWidth = 2f
                        )
                    }
                }
            }

            // --- DYNAMIC NODES (Uses cluster.name directly) ---
            clusters.forEach { cluster ->
                val pos = nodePositions[cluster.id] ?: Offset.Zero
                NodeElement(
                    cluster = cluster,
                    position = pos,
                    onPositionChange = { newPos -> nodePositions[cluster.id] = newPos },
                    onClick = { onClusterClick(cluster) }
                )
            }
        }

        // Back Button
        Button(
            onClick = onBack,
            modifier = Modifier.padding(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = VelvetTheme.SlateBlue)
        ) {
            Text("← BACK TO DASHBOARD", color = Color.White)
        }
    }
}

@Composable
fun NodeElement(
    cluster: Cluster,
    position: Offset,
    onPositionChange: (Offset) -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
            .size(120.dp)
            .pointerInput(cluster.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onPositionChange(position + dragAmount)
                }
            }
            .clickable { onClick() }
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(VelvetTheme.SlateBlue, VelvetTheme.MidnightNavy)
                ),
                shape = CircleShape
            )
            .border(2.dp, VelvetTheme.SunsetCoral.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = cluster.name.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = "${cluster.fileCount} FILES",
                color = VelvetTheme.SunsetCoral,
                fontSize = 10.sp
            )
        }
    }
}
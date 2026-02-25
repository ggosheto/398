package com.clusterview.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun VisualizationMapView(
    clusters: List<Cluster>,
    onClusterClick: (Cluster) -> Unit,
    onBack: () -> Unit
) {
    // Map View State
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Store node positions in a map so they can be dragged individually
    val nodePositions = remember {
        mutableStateMapOf<Int, Offset>().apply {
            clusters.forEachIndexed { index, cluster ->
                this[cluster.id] = Offset(200f + (index * 250f), 300f)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        // The Zoomable/Draggable Canvas
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
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            clusters.forEach { cluster ->
                val pos = nodePositions[cluster.id] ?: Offset.Zero

                // Individual Node
                Box(
                    modifier = Modifier
                        .offset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }
                        .size(120.dp)
                        .background(Color.DarkGray, CircleShape)
                        .clickable { onClusterClick(cluster) }
                        .pointerInput(cluster.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                nodePositions[cluster.id] = (nodePositions[cluster.id] ?: Offset.Zero) + dragAmount
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(cluster.name, color = Color.White, style = MaterialTheme.typography.labelLarge)
                        Text("${cluster.fileCount} files", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Overlay UI
        Button(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Text("← Back to Dashboard")
        }
    }
}
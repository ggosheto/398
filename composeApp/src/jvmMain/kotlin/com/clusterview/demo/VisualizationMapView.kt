package com.clusterview.demo

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var hoveredClusterId by remember { mutableStateOf<Int?>(null) }
    var selectedClusterId by remember { mutableStateOf<Int?>(null) }
    var visibleClusters by remember { mutableStateOf(clusters.map { it.id }.toSet()) }

    // Map to track positions of REAL cluster IDs
    val nodePositions = remember(clusters) {
        mutableStateMapOf<Int, Offset>().apply {
            clusters.forEachIndexed { index, cluster ->
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
        Row(modifier = Modifier.fillMaxSize()) {
            // Main Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(VelvetTheme.DeepOcean)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.5f, 3f)
                                offset += pan
                            }
                        }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.type == PointerEventType.Scroll) {
                                        val delta = event.changes.first().scrollDelta.y
                                        val zoomFactor = if (delta < 0) 1.1f else 0.9f
                                        scale = (scale * zoomFactor).coerceIn(0.5f, 3f)
                                    }
                                }
                            }
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                ) {
                    // Grid Background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridSize = 50f
                        for (x in 0..size.width.toInt() step 50) {
                            drawLine(
                                color = VelvetTheme.SlateBlue.copy(alpha = 0.1f),
                                start = Offset(x.toFloat(), 0f),
                                end = Offset(x.toFloat(), size.height),
                                strokeWidth = 1f
                            )
                        }
                        for (y in 0..size.height.toInt() step 50) {
                            drawLine(
                                color = VelvetTheme.SlateBlue.copy(alpha = 0.1f),
                                start = Offset(0f, y.toFloat()),
                                end = Offset(size.width, y.toFloat()),
                                strokeWidth = 1f
                            )
                        }
                    }

                    // Dynamic Edges (Lines between clusters)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val visibleClusterList = clusters.filter { it.id in visibleClusters }
                        for (i in visibleClusterList.indices) {
                            for (j in i + 1 until visibleClusterList.size) {
                                val cluster1 = visibleClusterList[i]
                                val cluster2 = visibleClusterList[j]
                                val p1 = nodePositions[cluster1.id] ?: Offset.Zero
                                val p2 = nodePositions[cluster2.id] ?: Offset.Zero

                                val edgeColor = when {
                                    hoveredClusterId in listOf(cluster1.id, cluster2.id) ->
                                        VelvetTheme.SunsetCoral.copy(alpha = 0.5f)
                                    else -> VelvetTheme.SunsetCoral.copy(alpha = 0.2f)
                                }

                                drawLine(
                                    color = edgeColor,
                                    start = p1 + Offset(60.dp.toPx(), 60.dp.toPx()),
                                    end = p2 + Offset(60.dp.toPx(), 60.dp.toPx()),
                                    strokeWidth = 2f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    // Dynamic Nodes (The Cluster Bubbles)
                    clusters.filter { it.id in visibleClusters }.forEach { cluster ->
                        val pos = nodePositions[cluster.id] ?: Offset.Zero
                        NodeElement(
                            cluster = cluster,
                            position = pos,
                            isSelected = selectedClusterId == cluster.id,
                            isHovered = hoveredClusterId == cluster.id,
                            onPositionChange = { newPos -> nodePositions[cluster.id] = newPos },
                            onHover = { hoveredClusterId = it },
                            onClick = {
                                selectedClusterId = cluster.id
                                onClusterClick(cluster) // This takes you to the Dashboard folder
                            }
                        )
                    }
                }

                TopToolbar(
                    scale = scale,
                    onResetView = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    onBack = onBack
                )
            }
        }

        Legend(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
    }
}

@Composable
fun NodeElement(
    cluster: Cluster,
    position: Offset,
    isSelected: Boolean = false,
    isHovered: Boolean = false,
    onPositionChange: (Offset) -> Unit,
    onHover: (Int?) -> Unit = {},
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
            .size(120.dp)
            // CLICKABLE added to handle navigation
            .clickable { onClick() }
            .pointerInput(cluster.id) {
                detectDragGestures(
                    onDragStart = { onHover(cluster.id) },
                    onDragEnd = { onHover(null) }
                ) { change, dragAmount ->
                    change.consume()
                    onPositionChange(position + dragAmount)
                }
            }
            .pointerInput(cluster.id) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when {
                            event.changes.any { it.pressed } -> onHover(cluster.id)
                            else -> onHover(null)
                        }
                    }
                }
            }
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isSelected || isHovered) VelvetTheme.SunsetCoral else VelvetTheme.SlateBlue,
                        VelvetTheme.MidnightNavy
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 3.dp else 2.dp,
                color = if (isSelected) VelvetTheme.SunsetCoral else VelvetTheme.SunsetCoral.copy(alpha = if (isHovered) 0.8f else 0.5f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = cluster.name.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = "${cluster.fileCount} FILES",
                color = if (isSelected) Color.White else VelvetTheme.SunsetCoral,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun TopToolbar(
    scale: Float,
    onResetView: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VelvetTheme.MidnightNavy.copy(alpha = 0.8f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.height(40.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = VelvetTheme.SlateBlue)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("BACK", color = Color.White, fontSize = 12.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Zoom: %.1f×".format(scale), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = onResetView,
                modifier = Modifier.height(40.dp).width(100.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = VelvetTheme.SlateBlue)
            ) {
                Icon(Icons.Default.Refresh, "Reset", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", color = Color.White, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun Legend(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .background(VelvetTheme.MidnightNavy.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        color = VelvetTheme.MidnightNavy,
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Column {
            Text("CONTROLS", color = VelvetTheme.SunsetCoral, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            LegendItem("Drag", "Pan view")
            LegendItem("Scroll", "Zoom")
            LegendItem("Click Node", "Open Folder")
            LegendItem("Drag Node", "Move")
        }
    }
}

@Composable
fun LegendItem(action: String, description: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(action, color = VelvetTheme.SunsetCoral, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
        Text("→ $description", color = Color.White, fontSize = 9.sp)
    }
}
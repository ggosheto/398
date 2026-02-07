package com.clusterview.demo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.UIManager

// --- 1. DATA MODELS ---
data class Cluster(
    val id: Int,
    val name: String,
    val path: String,
    val fileCount: Int,
    val lastModified: String
)

data class FileMetadata(
    val name: String,
    val extension: String,
    val sizeString: String
)

// --- 2. THEME SYSTEM ---
object VelvetTheme {
    val MidnightNavy = Color(0xFF181A2F)
    val DeepOcean = Color(0xFF242E49)
    val SlateBlue = Color(0xFF37415C)
    val SunsetCoral = Color(0xFFFDA481)
    val CrimsonRed = Color(0xFFB4182D)
    val DeepMaroon = Color(0xFF54162B)
    val SoftSand = Color(0xFFDFB6B2)

    val CoreGradient = Brush.verticalGradient(
        colors = listOf(MidnightNavy, DeepOcean)
    )
}

// --- 3. MAIN APP VIEW ---
@Composable
fun HomeView(user: User?, onLogoutSuccess: () -> Unit) {
    val clusters = remember { mutableStateListOf<Cluster>().apply { addAll(loadClusters()) } }
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }
    var clusterToDelete by remember { mutableStateOf<Cluster?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(VelvetTheme.CoreGradient)) {
        if (selectedCluster != null) {
            ClusterDetailView(
                cluster = selectedCluster!!,
                onBack = { selectedCluster = null },
                onRefresh = {
                    val current = selectedCluster!!
                    val folder = File(current.path)
                    if (folder.exists()) {
                        val newCount = folder.listFiles()?.filter { it.isFile }?.size ?: 0
                        val newTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy (HH:mm)"))
                        val index = clusters.indexOfFirst { it.id == current.id }
                        if (index != -1) {
                            val updated = current.copy(fileCount = newCount, lastModified = newTime)
                            clusters[index] = updated
                            selectedCluster = updated
                            saveClusters(clusters)
                        }
                    }
                }
            )
        } else {
            Scaffold(
                backgroundColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { Text("CORE CONTROL", color = VelvetTheme.SunsetCoral, fontWeight = FontWeight.ExtraBold) },
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp,
                        actions = {
                            IconButton(onClick = onLogoutSuccess) {
                                Icon(Icons.Default.Logout, "Logout", tint = VelvetTheme.CrimsonRed)
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            val folder = pickFolderNatively()
                            folder?.let {
                                if (clusters.none { c -> c.path == it.absolutePath }) {
                                    val count = it.listFiles()?.filter { f -> f.isFile }?.size ?: 0
                                    val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                    val newCluster = Cluster(it.name.hashCode(), it.name, it.absolutePath, count, time)
                                    clusters.add(newCluster)
                                    saveClusters(clusters)
                                }
                            }
                        },
                        backgroundColor = VelvetTheme.CrimsonRed,
                        contentColor = VelvetTheme.SunsetCoral,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp)) {
                    Text("SYSTEM CLUSTERS", style = MaterialTheme.typography.overline, color = VelvetTheme.SlateBlue, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))

                    if (clusters.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("NO DATA NODES DETECTED", color = VelvetTheme.SlateBlue)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 240.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(clusters) { cluster ->
                                ModernClusterCard(
                                    cluster = cluster,
                                    onDelete = { clusterToDelete = cluster },
                                    onClick = { selectedCluster = cluster }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DELETE CONFIRMATION DIALOG ---
    if (clusterToDelete != null) {
        AlertDialog(
            onDismissRequest = { clusterToDelete = null },
            backgroundColor = VelvetTheme.DeepOcean,
            shape = RoundedCornerShape(16.dp),
            title = { Text("UNLINK CLUSTER?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to remove '${clusterToDelete?.name}' from the dashboard? (Physical files will remain safe).",
                    color = VelvetTheme.SoftSand
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    clusters.remove(clusterToDelete)
                    saveClusters(clusters)
                    clusterToDelete = null
                }) {
                    Text("REMOVE", color = VelvetTheme.CrimsonRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { clusterToDelete = null }) {
                    Text("CANCEL", color = VelvetTheme.SunsetCoral)
                }
            }
        )
    }
}

// --- 4. UI COMPONENTS ---

@Composable
fun ModernClusterCard(cluster: Cluster, onDelete: () -> Unit, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.2f)) {
        Card(
            shape = RoundedCornerShape(20.dp),
            backgroundColor = VelvetTheme.DeepOcean,
            border = BorderStroke(2.dp, VelvetTheme.SlateBlue),
            elevation = 8.dp,
            modifier = Modifier.fillMaxSize().clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // HEADER ROW: Contains the Storage Icon (Left) and Deletion Box (Right)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // LEFT SIDE: Storage Icon
                    Surface(
                        color = VelvetTheme.DeepMaroon,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Storage,
                            null,
                            tint = VelvetTheme.SunsetCoral,
                            modifier = Modifier.padding(8.dp).size(24.dp)
                        )
                    }

                    // RIGHT SIDE: The "X" Button (Perfectly Mirrored)
                    Surface(
                        color = VelvetTheme.DeepMaroon,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .size(40.dp) // Total size: 24dp icon + 8dp padding on both sides
                            .clickable { onDelete() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Close,
                                null,
                                tint = VelvetTheme.SunsetCoral,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // BOTTOM CONTENT: Name and Objects Count
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            cluster.name,
                            color = Color.White,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${cluster.fileCount} Objects",
                            color = VelvetTheme.SunsetCoral,
                            style = MaterialTheme.typography.caption
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            null,
                            tint = VelvetTheme.SlateBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Modified: ${cluster.lastModified}",
                            color = VelvetTheme.SlateBlue,
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClusterDetailView(cluster: Cluster, onRefresh: () -> Unit, onBack: () -> Unit) {
    val filesInFolder = remember(cluster.path, cluster.lastModified) {
        File(cluster.path).listFiles()?.filter { it.isFile }?.map { file ->
            FileMetadata(file.nameWithoutExtension, file.extension.uppercase(), formatReadableSize(file.length()))
        } ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize().background(VelvetTheme.MidnightNavy)) {
        Row(Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.background(VelvetTheme.DeepMaroon, RoundedCornerShape(8.dp))) {
                    Icon(Icons.Default.ArrowBack, null, tint = VelvetTheme.SunsetCoral)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(cluster.name, style = MaterialTheme.typography.h5, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(cluster.path, style = MaterialTheme.typography.caption, color = VelvetTheme.SlateBlue)
                }
            }
            IconButton(onClick = onRefresh, modifier = Modifier.background(VelvetTheme.SlateBlue, RoundedCornerShape(8.dp))) {
                Icon(Icons.Default.Refresh, null, tint = VelvetTheme.SunsetCoral)
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = VelvetTheme.DeepOcean.copy(alpha = 0.5f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            border = BorderStroke(1.dp, VelvetTheme.SlateBlue)
        ) {
            LazyColumn(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text("MANIFEST", style = MaterialTheme.typography.overline, color = VelvetTheme.CrimsonRed) }
                items(filesInFolder) { file ->
                    Row(Modifier.fillMaxWidth().background(VelvetTheme.DeepOcean, RoundedCornerShape(12.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = VelvetTheme.MidnightNavy, shape = RoundedCornerShape(6.dp)) {
                            Text(file.extension.take(3), modifier = Modifier.padding(horizontal = 6.dp), color = VelvetTheme.SunsetCoral, style = MaterialTheme.typography.caption)
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(file.name, color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Text(file.sizeString, color = VelvetTheme.SunsetCoral, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- 5. PERSISTENCE & UTILS ---
fun saveClusters(clusters: List<Cluster>) {
    val file = File("clusters.txt")
    val data = clusters.joinToString("\n") { "${it.id}|${it.name}|${it.path}|${it.fileCount}|${it.lastModified}" }
    file.writeText(data)
}

fun loadClusters(): List<Cluster> {
    val file = File("clusters.txt")
    if (!file.exists()) return emptyList()
    return file.readLines().mapNotNull { line ->
        val parts = line.split("|")
        when (parts.size) {
            5 -> Cluster(parts[0].toInt(), parts[1], parts[2], parts[3].toInt(), parts[4])
            4 -> Cluster(parts[0].toInt(), parts[1], parts[2], parts[3].toInt(), "Initial Import")
            else -> null
        }
    }
}

fun pickFolderNatively(): File? {
    try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) } catch (e: Exception) {}
    val chooser = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY }
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
}

fun formatReadableSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}
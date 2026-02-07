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
import javax.swing.JFileChooser
import javax.swing.UIManager

// --- 1. DATA MODELS ---
data class Cluster(
    val id: Int,
    val name: String,
    val path: String,
    val fileCount: Int
)

data class FileMetadata(
    val name: String,
    val extension: String,
    val sizeString: String
)

// --- 2. UPDATED THEME (Using your specific Hex codes) ---
object VelvetTheme {
    val MidnightNavy = Color(0xFF181A2F)  // #181A2F
    val DeepOcean = Color(0xFF242E49)     // #242E49
    val SlateBlue = Color(0xFF37415C)     // #37415C
    val SunsetCoral = Color(0xFFFDA481)   // #FDA481
    val CrimsonRed = Color(0xFFB4182D)    // #B4182D
    val DeepMaroon = Color(0xFF54162B)    // #54162B

    val CoreGradient = Brush.verticalGradient(
        colors = listOf(MidnightNavy, DeepOcean)
    )
}

// --- 3. MAIN APP LOGIC ---
@Composable
fun HomeView(user: User?, onLogoutSuccess: () -> Unit) {
    val clusters = remember { mutableStateListOf<Cluster>() }
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(VelvetTheme.CoreGradient)) {
        if (selectedCluster != null) {
            ClusterDetailView(cluster = selectedCluster!!) {
                selectedCluster = null
            }
        } else {
            Scaffold(
                backgroundColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Text("DASHBOARD CONTROL",
                                color = VelvetTheme.SunsetCoral,
                                fontWeight = FontWeight.ExtraBold
                            )
                        },
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
                                    clusters.add(Cluster(it.name.hashCode(), it.name, it.absolutePath, count))
                                }
                            }
                        },
                        backgroundColor = VelvetTheme.CrimsonRed,
                        contentColor = VelvetTheme.SunsetCoral,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add")
                    }
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp)) {
                    Text("MANAGED CLUSTERS",
                        style = MaterialTheme.typography.overline,
                        color = VelvetTheme.SlateBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(24.dp))

                    if (clusters.isEmpty()) {
                        EmptyStateHint()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 240.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(clusters) { cluster ->
                                ModernClusterCard(cluster) { selectedCluster = cluster }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 4. REFINED UI COMPONENTS ---

@Composable
fun EmptyStateHint() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.FolderOpen, null, tint = VelvetTheme.SlateBlue, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("NO CLUSTERS INITIALIZED", color = VelvetTheme.SlateBlue)
        }
    }
}

@Composable
fun ModernClusterCard(cluster: Cluster, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        backgroundColor = VelvetTheme.DeepOcean,
        border = BorderStroke(2.dp, VelvetTheme.SlateBlue),
        elevation = 8.dp,
        modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).clickable { onClick() }
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Surface(color = VelvetTheme.DeepMaroon, shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Storage, null, tint = VelvetTheme.SunsetCoral, modifier = Modifier.padding(8.dp))
                }
                Text("${cluster.fileCount} Objects", color = VelvetTheme.SunsetCoral, style = MaterialTheme.typography.caption)
            }
            Column {
                Text(cluster.name, color = Color.White, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                Text("System Cluster", color = VelvetTheme.SlateBlue, style = MaterialTheme.typography.caption)
            }
        }
    }
}

@Composable
fun ClusterDetailView(cluster: Cluster, onBack: () -> Unit) {
    val filesInFolder = remember(cluster.path) {
        File(cluster.path).listFiles()?.filter { it.isFile }?.map { file ->
            FileMetadata(file.nameWithoutExtension, file.extension.uppercase(), formatReadableSize(file.length()))
        } ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize().background(VelvetTheme.MidnightNavy)) {
        // Detail Header
        Row(Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(VelvetTheme.DeepMaroon, RoundedCornerShape(8.dp))) {
                Icon(Icons.Default.ArrowBackIosNew, "Back", tint = VelvetTheme.SunsetCoral, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(cluster.name, style = MaterialTheme.typography.h5, color = Color.White, fontWeight = FontWeight.Bold)
                Text(cluster.path, style = MaterialTheme.typography.caption, color = VelvetTheme.SlateBlue)
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = VelvetTheme.DeepOcean.copy(alpha = 0.5f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            border = BorderStroke(1.dp, VelvetTheme.SlateBlue)
        ) {
            LazyColumn(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("CLUSTER MANIFEST", style = MaterialTheme.typography.overline, color = VelvetTheme.CrimsonRed)
                    Spacer(Modifier.height(16.dp))
                }
                items(filesInFolder) { file ->
                    ModernFileRow(file)
                }
            }
        }
    }
}

@Composable
fun ModernFileRow(file: FileMetadata) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VelvetTheme.DeepOcean, RoundedCornerShape(12.dp))
            .border(1.dp, VelvetTheme.SlateBlue, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = VelvetTheme.MidnightNavy, shape = RoundedCornerShape(6.dp)) {
            Text(file.extension.take(3),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                color = VelvetTheme.SunsetCoral,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(file.name, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text("File Type: ${file.extension}", color = VelvetTheme.SlateBlue, style = MaterialTheme.typography.caption)
        }
        Text(file.sizeString, color = VelvetTheme.SunsetCoral, fontWeight = FontWeight.Bold)
    }
}

// --- 5. UTILITIES ---
fun pickFolderNatively(): File? {
    try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) } catch (e: Exception) {}
    val chooser = JFileChooser()
    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
}

fun formatReadableSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}
package com.clusterview.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File // CRITICAL: This fixes 'isDirectory' and 'listFiles'
import javax.swing.JFileChooser
import javax.swing.UIManager

// 1. DATA MODELS
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

@Composable
fun HomeView(user: User?, onLogoutSuccess: () -> Unit) {
    val clusters = remember { mutableStateListOf<Cluster>() }
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // 2. DIRECTORY PICKER LAUNCHER
    // 1. Inside HomeView, replace the launcher block with just this:
// (We don't need the launcher variable anymore)

    // 3. NAVIGATION LOGIC
    if (selectedCluster != null) {
        ClusterDetailView(cluster = selectedCluster!!) {
            selectedCluster = null
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Dashboard") })
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // We call a manual picker function instead of the library
                        val selected = openNativeFolderPicker()
                        selected?.let { folder ->
                            if (clusters.none { it.path == folder.absolutePath }) {
                                clusters.add(
                                    Cluster(
                                        id = folder.name.hashCode(),
                                        name = folder.name,
                                        path = folder.absolutePath,
                                        fileCount = folder.listFiles()?.filter { it.isFile }?.size ?: 0
                                    )
                                )
                            }
                        }
                    },
                    backgroundColor = Color.Cyan
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                Text("Your Clusters", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                if (clusters.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No clusters found. Click '+' to import a folder.", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 200.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(clusters) { cluster ->
                            ClusterCard(cluster) { selectedCluster = cluster }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure?") },
            confirmButton = {
                Button(onClick = { onLogoutSuccess() }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// 4. SUB-COMPONENTS

@Composable
fun ClusterCard(cluster: Cluster, onClick: () -> Unit) {
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFF1E1E1E),
        modifier = Modifier.size(200.dp).clickable { onClick() },
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(48.dp))
            Text(cluster.name, color = Color.White, fontWeight = FontWeight.Bold)
            Text("${cluster.fileCount} Files", color = Color.Gray, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun ClusterDetailView(cluster: Cluster, onBack: () -> Unit) {
    val filesInFolder = remember(cluster.path) {
        File(cluster.path).listFiles()?.filter { it.isFile }?.map { file ->
            FileMetadata(file.nameWithoutExtension, file.extension.uppercase(), formatBytes(file.length()))
        } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cluster.name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(filesInFolder) { file ->
                Card(backgroundColor = Color(0xFF2A2A2A), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(file.name, color = Color.White)
                        Text("${file.extension} | ${file.sizeString}", color = Color.Cyan)
                    }
                }
            }
        }
    }
}

// 5. HELPER FUNCTION
fun formatBytes(bytes: Long): String {
    // 1. If it's literally less than 1024, just stay in Bytes
    if (bytes < 1024) return "$bytes B"

    // 2. This math finds out how many times we can divide by 1024
    // 1 = KB, 2 = MB, 3 = GB, etc.
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()

    // 3. This picks the letter based on the 'exp' result
    val pre = "KMGTPE"[exp - 1]

    // 4. This does the final division and adds the letter (e.g., "1.5 MB")
    return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}

fun openNativeFolderPicker(): File? {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (e: Exception) { }

    val chooser = JFileChooser()
    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile
    } else null
}
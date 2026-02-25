package com.clusterview.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.UIManager
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign

/*data class Cluster(
    val id: Int,
    val name: String,
    val fileCount: Int,
    val path: String,
    val lastModified: String,
    val hasDuplicates: Boolean = false
)*/

data class FileMetadata(
    val name: String,
    val extension: String,
    val sizeString: String,
    val sizeBytes: Long = 0L
)

enum class SortOption { NAME, DATE, OBJECT_COUNT }

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

@Composable
fun HomeView(user: User?, onClusterClick: (Cluster) -> Unit, onOpenMap: () -> Unit, onLogoutSuccess: () -> Unit) {
    val clusters = remember { mutableStateListOf<Cluster>().apply { addAll(loadClusters()) } }
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }
    var clusterToDelete by remember { mutableStateOf<Cluster?>(null) }


    var searchTerm by remember { mutableStateOf("") }
    var currentSort by remember { mutableStateOf(SortOption.NAME) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val filteredClusters = clusters
        .filter { it.name.contains(searchTerm, ignoreCase = true) }
        .sortedWith(compareBy {
            when (currentSort) {
                SortOption.NAME -> it.name.lowercase()
                SortOption.DATE -> it.lastModified
                SortOption.OBJECT_COUNT -> -it.fileCount
            }
        })

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
                        val duplicatesFound = hasDuplicates(current.path)
                        val index = clusters.indexOfFirst { it.id == current.id }
                        if (index != -1) {
                            val updated = current.copy(
                                fileCount = newCount,
                                lastModified = newTime,
                                hasDuplicates = duplicatesFound
                            )
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

                            IconButton(onClick = onOpenMap) {
                                Icon(
                                    imageVector = Icons.Default.Share, // Or Icons.Default.Map if available
                                    contentDescription = "Topology Map",
                                    tint = VelvetTheme.SunsetCoral
                                )
                            }

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
                                    val newCluster = Cluster(it.name.hashCode(), it.name, count, it.absolutePath, time)
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

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            modifier = Modifier.weight(1f).border(1.dp, VelvetTheme.SlateBlue, RoundedCornerShape(12.dp)),
                            placeholder = { Text("Search clusters...", color = VelvetTheme.SlateBlue) },
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = VelvetTheme.DeepOcean.copy(alpha = 0.5f),
                                textColor = Color.White,
                                cursorColor = VelvetTheme.SunsetCoral,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = VelvetTheme.SunsetCoral) },
                            singleLine = true
                        )

                        Spacer(Modifier.width(12.dp))

                        Box {
                            IconButton(
                                onClick = { sortMenuExpanded = true },
                                modifier = Modifier
                                    .background(VelvetTheme.DeepOcean, RoundedCornerShape(12.dp))
                                    .border(1.dp, VelvetTheme.SlateBlue, RoundedCornerShape(12.dp))
                                    .size(56.dp)
                            ) {
                                Icon(Icons.Default.Sort, "Sort", tint = VelvetTheme.SunsetCoral)
                            }

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false },
                                modifier = Modifier.background(VelvetTheme.DeepOcean).border(1.dp, VelvetTheme.SlateBlue)
                            ) {
                                DropdownMenuItem(onClick = { currentSort = SortOption.NAME; sortMenuExpanded = false }) {
                                    Text("Sort by Name", color = Color.White)
                                }
                                DropdownMenuItem(onClick = { currentSort = SortOption.DATE; sortMenuExpanded = false }) {
                                    Text("Sort by Date", color = Color.White)
                                }
                                DropdownMenuItem(onClick = { currentSort = SortOption.OBJECT_COUNT; sortMenuExpanded = false }) {
                                    Text("Sort by Size", color = Color.White)
                                }
                            }
                        }
                    }

                    if (filteredClusters.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("NO MATCHING DATA NODES", color = VelvetTheme.SlateBlue)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 240.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredClusters) { cluster ->
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

    if (clusterToDelete != null) {
        AlertDialog(
            onDismissRequest = { clusterToDelete = null },
            backgroundColor = VelvetTheme.DeepOcean,
            shape = RoundedCornerShape(16.dp),
            title = { Text("UNLINK CLUSTER?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to remove '${clusterToDelete?.name}'? (Files remain on disk).",
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


@Composable
fun ModernClusterCard(cluster: Cluster, onDelete: () -> Unit, onClick: () -> Unit) {
    val heatColor = remember(cluster.fileCount) { getHeatmapColor(cluster.fileCount) }

    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.2f)) {
        Card(
            shape = RoundedCornerShape(20.dp),
            backgroundColor = VelvetTheme.DeepOcean,
            border = BorderStroke(2.dp, heatColor),
            elevation = 8.dp,
            modifier = Modifier.fillMaxSize().clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = VelvetTheme.DeepMaroon,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Storage,
                            null,
                            tint = heatColor,
                            modifier = Modifier.padding(8.dp).size(24.dp)
                        )
                    }

                    if (cluster.hasDuplicates) {
                        Surface(
                            color = VelvetTheme.CrimsonRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, VelvetTheme.CrimsonRed),
                            modifier = Modifier.clickable {
                                onClick()
                            }
                        ) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = VelvetTheme.CrimsonRed, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("DUPLICATES FOUND", color = VelvetTheme.CrimsonRed, style = MaterialTheme.typography.overline)
                            }
                        }
                    }

                    Surface(
                        color = VelvetTheme.DeepMaroon,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(40.dp).clickable { onDelete() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, null, tint = VelvetTheme.SunsetCoral, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(cluster.name, color = Color.White, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${cluster.fileCount} Objects", color = heatColor, style = MaterialTheme.typography.caption)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, null, tint = VelvetTheme.SlateBlue, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Modified: ${cluster.lastModified}", color = VelvetTheme.SlateBlue, style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
    }
}

enum class SortMethod { NAME, SIZE, TYPE }

@Composable
fun ClusterDetailView(cluster: Cluster, onRefresh: () -> Unit, onBack: () -> Unit) {
    // 1. DATA PROCESSING
    val filesInFolder = remember(cluster.path, cluster.lastModified) {
        File(cluster.path).listFiles()?.filter { it.isFile }?.map { file ->
            FileMetadata(
                name = file.nameWithoutExtension,
                extension = file.extension.uppercase(),
                sizeString = formatReadableSize(file.length()),
                sizeBytes = file.length() // Added for accurate sorting
            )
        } ?: emptyList()
    }

    val distribution = remember(cluster.path, cluster.lastModified) { getFileDistribution(cluster.path) }
    val totalSizeBytes = remember(cluster.path, cluster.lastModified) {
        File(cluster.path).listFiles()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L
    }
    val totalSizeString = formatReadableSize(totalSizeBytes)

    var searchQuery by remember { mutableStateOf("") }
    var currentSort by remember { mutableStateOf(SortMethod.NAME) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedFileForSidebar by remember { mutableStateOf<FileMetadata?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var prefixText by remember { mutableStateOf("") }

    val processedFiles = remember(searchQuery, currentSort, filesInFolder) {
        val filtered = filesInFolder.filter { it.name.contains(searchQuery, ignoreCase = true) }
        when (currentSort) {
            SortMethod.NAME -> filtered.sortedBy { it.name.lowercase() }
            SortMethod.SIZE -> filtered.sortedByDescending { it.sizeBytes }
            SortMethod.TYPE -> filtered.sortedBy { it.extension }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            backgroundColor = VelvetTheme.DeepOcean,
            shape = RoundedCornerShape(16.dp),
            title = { Text("BATCH RENAME", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                TextField(
                    value = prefixText,
                    onValueChange = { prefixText = it },
                    placeholder = { Text("Enter prefix...", color = VelvetTheme.SlateBlue) },
                    colors = TextFieldDefaults.textFieldColors(textColor = Color.White)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (prefixText.isNotEmpty()) {
                        batchRenameFiles(cluster.path, prefixText)
                        onRefresh()
                        showRenameDialog = false
                    }
                }) { Text("APPLY", color = VelvetTheme.SunsetCoral) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("CANCEL", color = Color.Gray) }
            }
        )
    }

    Row(modifier = Modifier.fillMaxSize().background(VelvetTheme.MidnightNavy)) {

        Column(modifier = Modifier.weight(1f)) {
            DetailHeader(cluster.name, cluster.path, onBack, onRefresh)

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = VelvetTheme.DeepOcean.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 32.dp),
                border = BorderStroke(1.dp, VelvetTheme.SlateBlue)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    item {
                        DistributionPieChart(distribution, totalSizeString)
                        Spacer(Modifier.height(24.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Search files...", color = VelvetTheme.SlateBlue) },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = VelvetTheme.SunsetCoral) },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = Color.White,
                                    focusedBorderColor = VelvetTheme.SunsetCoral,
                                    unfocusedBorderColor = VelvetTheme.SlateBlue
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(Modifier.width(12.dp))
                            Box {
                                IconButton(
                                    onClick = { showSortMenu = true },
                                    modifier = Modifier.size(52.dp).background(VelvetTheme.DeepMaroon, RoundedCornerShape(12.dp))
                                ) {
                                    Icon(Icons.Default.Sort, null, tint = VelvetTheme.SunsetCoral)
                                }
                                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }, modifier = Modifier.background(VelvetTheme.DeepOcean)) {
                                    DropdownMenuItem(onClick = { currentSort = SortMethod.NAME; showSortMenu = false }) { Text("Sort by Name", color = Color.White) }
                                    DropdownMenuItem(onClick = { currentSort = SortMethod.SIZE; showSortMenu = false }) { Text("Sort by Size", color = Color.White) }
                                    DropdownMenuItem(onClick = { currentSort = SortMethod.TYPE; showSortMenu = false }) { Text("Sort by Type", color = Color.White) }
                                }
                            }
                        }
                    }

                    items(processedFiles) { file ->
                        Box(modifier = Modifier.clickable { selectedFileForSidebar = file }) {
                            ModernFileRow(file)
                        }
                    }

                    if (processedFiles.isEmpty() && searchQuery.isNotEmpty()) {
                        item {
                            Text("No files match '$searchQuery'", color = VelvetTheme.SlateBlue, modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selectedFileForSidebar != null,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            selectedFileForSidebar?.let { file ->
                FileInspectorSidebar(
                    file = file,
                    clusterPath = cluster.path,
                    clusterTotalSize = totalSizeBytes,
                    onClose = { selectedFileForSidebar = null },
                    onRefresh = onRefresh
                )
            }
        }
    }
}

@Composable
fun DistributionPieChart(distribution: Map<String, Float>, totalSize: String) {
    val colors = mapOf(
        "Images" to VelvetTheme.SunsetCoral,
        "Docs" to VelvetTheme.SlateBlue,
        "Code" to VelvetTheme.CrimsonRed,
        "Other" to Color.Gray
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                distribution.forEach { (label, weight) ->
                    val sweepAngle = weight * 360f
                    if (sweepAngle > 0) {
                        drawArc(
                            color = colors[label] ?: Color.Gray,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false, // Changed to false for a "Ring" look
                            style = Stroke(width = 40f, cap = StrokeCap.Round) // Modern thick stroke
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = totalSize.split(" ")[0], // The Number
                    style = MaterialTheme.typography.h4,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = totalSize.split(" ").getOrElse(1) { "" }, // The Unit (MB/GB)
                    style = MaterialTheme.typography.caption,
                    color = VelvetTheme.SunsetCoral,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            distribution.keys.forEach { label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(colors[label] ?: Color.Gray, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(6.dp))
                    Text(label, color = VelvetTheme.SoftSand, style = MaterialTheme.typography.caption)
                }
            }
        }
    }
}

fun batchRenameFiles(path: String, prefix: String): Boolean {
    val folder = File(path)
    val files = folder.listFiles()?.filter { it.isFile } ?: return false
    var success = true

    files.forEach { file ->
        val newName = prefix + file.name
        val destination = File(path, newName)
        if (!file.renameTo(destination)) {
            success = false
        }
    }
    return success
}

@Composable
fun ModernFileRow(file: FileMetadata) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VelvetTheme.DeepOcean, RoundedCornerShape(12.dp))
            .border(1.dp, VelvetTheme.SlateBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = VelvetTheme.MidnightNavy,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = file.extension.take(3),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                color = VelvetTheme.SunsetCoral,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = file.name,
            color = Color.White,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = file.sizeString,
            color = VelvetTheme.SunsetCoral,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold
        )
    }
}
fun saveClusters(clusters: List<Cluster>) {
    val file = File("clusters.txt")
    val data = clusters.joinToString("\n") {
        "${it.id}|${it.name}|${it.path}|${it.fileCount}|${it.lastModified}|${it.hasDuplicates}"
    }
    file.writeText(data)
}

fun loadClusters(): List<Cluster> {
    val file = File("clusters.txt")
    if (!file.exists()) return emptyList()
    return file.readLines().mapNotNull { line ->
        val parts = line.split("|")
        if (parts.size == 6) {
            Cluster(
                parts[0].toInt(),
                parts[1],
                parts[3].toInt(),
                parts[2],
                parts[4],
                parts[5].toBoolean() // The 6th part
            )
        } else if (parts.size == 5) {
            Cluster(parts[0].toInt(), parts[1], parts[3].toInt(), parts[2], parts[4], false)
        } else null
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

fun getFileDistribution(path: String): Map<String, Float> {
    val files = File(path).listFiles()?.filter { it.isFile } ?: return emptyMap()
    if (files.isEmpty()) return emptyMap()

    // Expanded Categories to include your specific file types
    val categories = mapOf(
        "Images" to listOf("JPG", "JPEG", "PNG", "GIF", "SVG", "WEBP", "BMP"),
        "Docs"   to listOf("PDF", "PPT", "PPTX", "DOC", "DOCX", "TXT", "XLSX", "CSV"),
        "Code"   to listOf("KT", "KTS", "PY", "JS", "HTML", "CSS", "JAVA", "CPP", "JSON")
    )

    val counts = mutableMapOf<String, Int>()

    files.forEach { file ->
        val ext = file.extension.uppercase()

        val cat = categories.entries.find { it.value.contains(ext) }?.key ?: "Other"

        counts[cat] = counts.getOrDefault(cat, 0) + 1
    }

    return counts.mapValues { it.value.toFloat() / files.size }
}

fun getHeatmapColor(fileCount: Int): Color {
    val threshold = 10f
    val intensity = (fileCount.toFloat() / threshold).coerceIn(0f, 1f)

    return Color(
        red = VelvetTheme.SlateBlue.red + (VelvetTheme.CrimsonRed.red - VelvetTheme.SlateBlue.red) * intensity,
        green = VelvetTheme.SlateBlue.green + (VelvetTheme.CrimsonRed.green - VelvetTheme.SlateBlue.green) * intensity,
        blue = VelvetTheme.SlateBlue.blue + (VelvetTheme.CrimsonRed.blue - VelvetTheme.SlateBlue.blue) * intensity
    )
}

fun hasDuplicates(path: String): Boolean {
    val folder = File(path)
    val files = folder.listFiles()?.filter { it.isFile } ?: return false

    val seenFiles = files.groupBy { "${it.name}_${it.length()}" }
    return seenFiles.any { it.value.size > 1 }
}

fun removeDuplicates(path: String): Int {
    val folder = File(path)
    val files = folder.listFiles()?.filter { it.isFile } ?: return 0

    val seenFiles = mutableSetOf<String>()
    var deletedCount = 0

    files.forEach { file ->
        val fingerprint = "${file.name}_${file.length()}"
        if (seenFiles.contains(fingerprint)) {
            if (file.delete()) deletedCount++
        } else {
            seenFiles.add(fingerprint)
        }
    }
    return deletedCount
}

fun renameSingleFile(oldPath: String, newName: String, extension: String): Boolean {
    val file = File(oldPath)
    val parent = file.parentFile
    val destination = File(parent, "$newName.$extension")
    return file.renameTo(destination)
}

@Composable
fun FileInspectorSidebar(
    file: FileMetadata,
    clusterPath: String,
    clusterTotalSize: Long,
    onClose: () -> Unit,
    onRefresh: () -> Unit
) {
    val fullPath = "$clusterPath/${file.name}.${file.extension.lowercase()}"
    val fileObj = File(fullPath)
    val sizeRatio = if (clusterTotalSize > 0) fileObj.length().toFloat() / clusterTotalSize else 0f

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(VelvetTheme.DeepOcean)
            .drawBehind {
                drawLine(VelvetTheme.SlateBlue, Offset(0f, 0f), Offset(0f, size.height), 2.dp.toPx())
            }
            .padding(24.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("FILE CONFIG", style = MaterialTheme.typography.overline, color = VelvetTheme.CrimsonRed)
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, null, tint = VelvetTheme.SoftSand)
            }
        }

        Spacer(Modifier.height(40.dp))

        Text("WEIGHT IN CLUSTER", color = VelvetTheme.SoftSand, style = MaterialTheme.typography.overline)
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.size(140.dp).align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(VelvetTheme.MidnightNavy, -90f, 360f, false, style = Stroke(18f))
                drawArc(VelvetTheme.SunsetCoral, -90f, sizeRatio * 360f, false, style = Stroke(18f, cap = StrokeCap.Round))
            }
            Text("${(sizeRatio * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(40.dp))

        InspectorField("FILENAME", file.name)
        InspectorField("TYPE / FORMAT", file.extension)
        InspectorField("FILE SIZE", file.sizeString)

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { try { java.awt.Desktop.getDesktop().browse(fileObj.parentFile.toURI()) } catch(e: Exception) {} },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = VelvetTheme.DeepMaroon, contentColor = VelvetTheme.SunsetCoral),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, VelvetTheme.SunsetCoral.copy(alpha = 0.3f))
        ) {
            Icon(Icons.Default.Launch, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text("SHOW IN EXPLORER", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = {
                if (fileObj.exists()) {
                    fileObj.delete()
                    onRefresh()
                    onClose()
                }
            },
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Icon(Icons.Default.DeleteForever, null, tint = VelvetTheme.CrimsonRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("DELETE PERMANENTLY", color = VelvetTheme.CrimsonRed.copy(alpha = 0.8f), style = MaterialTheme.typography.caption, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InspectorField(label: String, value: String) {
    Column(Modifier.padding(vertical = 10.dp)) {
        Text(label, color = VelvetTheme.SoftSand, style = MaterialTheme.typography.caption, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, style = MaterialTheme.typography.body1)
    }
}

@Composable
fun DetailHeader(
    name: String,
    path: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(VelvetTheme.DeepMaroon, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = VelvetTheme.SunsetCoral)
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.h5,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = path,
                    style = MaterialTheme.typography.caption,
                    color = VelvetTheme.SlateBlue
                )
            }
        }

        IconButton(
            onClick = onRefresh,
            modifier = Modifier.background(VelvetTheme.SlateBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Default.Refresh, null, tint = VelvetTheme.SunsetCoral)
        }
    }
}
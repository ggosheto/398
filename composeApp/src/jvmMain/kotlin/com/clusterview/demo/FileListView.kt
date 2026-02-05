package com.clusterview.demo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clusterview.demo.FileScanner.revealFileInExplorer

// --- THEME PALETTES ---
val AccentColor = Color(0xFFD2B48C)
val DarkBg = Color(0xFF000814)
val LightBg = Color(0xFFF0F2F5)

enum class SortOrder { NAME, SIZE, TYPE }

/*@Composable
fun FileListView(
    clusterName: String,
    files: List<FileEntry>,
    onBack: () -> Unit,
    onSearch: (String) -> Unit
) {

    // Track the last 5 unique searches
    var searchHistory by remember { mutableStateOf(listOf<String>()) }
    var showHistory by remember { mutableStateOf(false) }

    // A helper function to save a search to history
    fun addToHistory(query: String) {
        if (query.isNotBlank() && !searchHistory.contains(query)) {
            // Keep only the 5 most recent items
            searchHistory = (listOf(query) + searchHistory).take(5)
        }
    }

    // 1. THIS IS THE BRAIN OF THE THEME
    var isDarkMode by remember { mutableStateOf(true) }

    // 2. DEFINE THE VARIABLES
    // Instead of saying "White", we say "textColor"
    val bgColor = if (isDarkMode) Color(0xFF000814) else Color(0xFFF0F2F5)
    val textColor = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val cardBg = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White

    var selectedFile by remember { mutableStateOf<FileEntry?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    //var isDarkMode by remember { mutableStateOf(true) }
    var currentSort by remember { mutableStateOf(SortOrder.NAME) }

    //val bgColor = if (isDarkMode) DarkBg else LightBg
    //val textColor = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val subTextColor = if (isDarkMode) Color.White.copy(0.4f) else Color.Black.copy(0.5f)

    val displayFiles = remember(files, searchQuery, currentSort) {
        files.filter { it.name.contains(searchQuery, ignoreCase = true) }
            .let { filtered ->
                when (currentSort) {
                    SortOrder.NAME -> filtered.sortedBy { it.name.lowercase() }
                    SortOrder.SIZE -> filtered.sortedByDescending { it.size }
                    SortOrder.TYPE -> filtered.sortedBy { it.extension }
                }
            }
    }

    val totalSize = displayFiles.sumOf { it.size }

    Column((Modifier.fillMaxSize().background(bgColor))) {
        Text("Hello", color = textColor)

        // --- HEADER ---
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = textColor) }
                    Column(Modifier.padding(start = 8.dp)) {
                        Text("SYSTEM CLUSTER", color = AccentColor.copy(0.7f), style = MaterialTheme.typography.overline.copy(letterSpacing = 3.sp))
                        Text(clusterName.uppercase(), color = textColor, style = MaterialTheme.typography.h4, fontWeight = FontWeight.Black)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isDarkMode = !isDarkMode }) {
                        Icon(if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, null, tint = AccentColor)
                    }
                    Button(
                        onClick = { ExportManager.exportToFolder(clusterName, displayFiles) },
                        colors = ButtonDefaults.buttonColors(AccentColor),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.Download, null, Modifier.size(16.dp), tint = Color(0xFF000814))
                        Text(" EXTRACT", fontWeight = FontWeight.Bold, color = Color(0xFF000814))
                    }
                }
            }

            Row(Modifier.padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                StatItem("OBJECTS", displayFiles.size.toString(), isDarkMode)
                Box(Modifier.padding(horizontal = 20.dp).width(1.dp).height(24.dp).background(subTextColor.copy(0.2f)))
                StatItem("VOLUME", formatFileSize(totalSize), isDarkMode)
            }

            DistributionBar(displayFiles)
        }

        // --- CONTROLS ---
        Column(Modifier.padding(horizontal = 24.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SORT DROPDOWN
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SORT BY:", color = Color.White.copy(0.4f), style = MaterialTheme.typography.caption)
                    Spacer(Modifier.width(8.dp))
                    SortButton("NAME", currentSort == SortOrder.NAME) { currentSort = SortOrder.NAME }
                    SortButton("SIZE", currentSort == SortOrder.SIZE) { currentSort = SortOrder.SIZE }
                    SortButton("TYPE", currentSort == SortOrder.TYPE) { currentSort = SortOrder.TYPE }
                }

                // THEME TOGGLE
                IconButton(onClick = { *//* We will add theme state here *//* }) {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = "Toggle Theme",
                        tint = Color(0xFFD2B48C)
                    )
                }
            }
            Box(Modifier.padding(horizontal = 24.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearch(it)
                        showHistory = true // Show history when typing
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Filter data points...", color = subTextColor) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = subTextColor) },
                    trailingIcon = {
                        // Button to save the current search to history
                        IconButton(onClick = { addToHistory(searchQuery) }) {
                            Icon(Icons.Default.History, null, tint = AccentColor)
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = textColor,
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = subTextColor.copy(0.2f)
                    )
                )

                // THE HISTORY DROPDOWN
                if (showHistory && searchHistory.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp), // Positioned below the text field
                        elevation = 8.dp,
                        backgroundColor = if (isDarkMode) Color(0xFF0A0E14) else Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, subTextColor.copy(0.1f))
                    ) {
                        Column {
                            searchHistory.forEach { historyItem ->
                                TextButton(
                                    onClick = {
                                        searchQuery = historyItem
                                        onSearch(historyItem)
                                        showHistory = false
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                ) {
                                    Row(Modifier.fillMaxWidth(), Arrangement.Start) {
                                        Icon(Icons.Default.Restore, null, tint = subTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(historyItem, color = textColor)
                                    }
                                }
                            }
                            // Option to clear history
                            TextButton(onClick = { searchHistory = emptyList() }) {
                                Text("CLEAR HISTORY", color = AccentColor, style = MaterialTheme.typography.overline)
                            }
                        }
                    }
                }
            }
        }

        // --- CONTENT ---
        Row(Modifier.fillMaxSize().padding(top = 16.dp)) {
            // FIX for Line 119: Added modifier as first parameter
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(displayFiles) { file ->
                    FileRowItem(file, selectedFile == file, isDarkMode) { selectedFile = file }
                }
            }
            if (selectedFile != null) {
                DetailsPanel(selectedFile!!, isDarkMode) { selectedFile = null }
            }
        }
    }
}*/

@Composable
fun FileListView(
    clusterName: String,
    files: List<FileEntry>,
    onBack: () -> Unit,
    onSearch: (String) -> Unit
) {
    var selectedFile by remember { mutableStateOf<FileEntry?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isDarkMode by remember { mutableStateOf(true) }
    var currentSort by remember { mutableStateOf(SortOrder.NAME) }

    val bgColor = if (isDarkMode) Color(0xFF000814) else Color(0xFFF0F2F5)
    val textColor = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val subTextColor = if (isDarkMode) Color.White.copy(0.4f) else Color.Black.copy(0.5f)

    // Sorting Engine
    val displayFiles = remember(files, searchQuery, currentSort) {
        files.filter { it.name.contains(searchQuery, ignoreCase = true) }
            .sortedWith(when (currentSort) {
                SortOrder.NAME -> compareBy { it.name.lowercase() }
                SortOrder.SIZE -> compareByDescending { it.size }
                SortOrder.TYPE -> compareBy { it.extension }
            })
    }

    Column(Modifier.fillMaxSize().background(bgColor)) {
        // --- RESTORED HEADER ---
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = textColor) }
                    Column(Modifier.padding(start = 8.dp)) {
                        Text("SYSTEM CLUSTER", color = AccentColor.copy(0.7f), style = MaterialTheme.typography.overline)
                        Text(clusterName.uppercase(), color = textColor, style = MaterialTheme.typography.h4, fontWeight = FontWeight.Black)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isDarkMode = !isDarkMode }) {
                        Icon(if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, null, tint = AccentColor)
                    }
                    // RESTORED EXTRACT BUTTON
                    Button(
                        onClick = { ExportManager.exportToFolder(clusterName, displayFiles) },
                        colors = ButtonDefaults.buttonColors(AccentColor),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.Download, null, Modifier.size(16.dp), tint = Color(0xFF000814))
                        Text(" EXTRACT", fontWeight = FontWeight.Bold, color = Color(0xFF000814))
                    }
                }
            }

            // RESTORED STATS
            Row(Modifier.padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                StatItem("OBJECTS", displayFiles.size.toString(), isDarkMode)
                Box(Modifier.padding(horizontal = 20.dp).width(1.dp).height(24.dp).background(subTextColor.copy(0.2f)))
                StatItem("VOLUME", formatBytes(displayFiles.sumOf { it.size }), isDarkMode)
            }
            DistributionBar(displayFiles)
        }

        // --- SEARCH BAR ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; onSearch(it) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            placeholder = { Text("Filter data points...", color = subTextColor) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = subTextColor) },
            colors = TextFieldDefaults.outlinedTextFieldColors(textColor = textColor, focusedBorderColor = AccentColor)
        )

        // --- CONTENT (List + Details Panel) ---
        Row(Modifier.fillMaxSize().padding(top = 16.dp)) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayFiles) { file ->
                    FileRowItem(file, selectedFile == file, isDarkMode) { selectedFile = file }
                }
            }
            // RESTORED POP-UP MENU
            if (selectedFile != null) {
                DetailsPanel(selectedFile!!, isDarkMode) { selectedFile = null }
            }
        }
    }
}

@Composable
fun DistributionBar(files: List<FileEntry>) {
    if (files.isEmpty()) return

    // 1. Calculate stats based on TOTAL BYTES, not just count
    val totalClusterSize = remember(files) { files.sumOf { it.size } }

    val weightStats = remember(files, totalClusterSize) {
        if (totalClusterSize == 0L) return@remember emptyList<Pair<String, Float>>()

        files.groupBy { it.extension.lowercase() }
            .mapValues { (_, fileGroup) ->
                fileGroup.sumOf { it.size }.toFloat() / totalClusterSize
            }
            .toList()
            .sortedByDescending { it.second }
            .take(4) // Increased to 4 for better detail
    }

    val colors = listOf(AccentColor, Color(0xFF5DADE2), Color(0xFF58D68D), Color(0xFFF4D03F))

    Column(Modifier.padding(top = 16.dp)) {
        Text(
            text = "STORAGE COMPOSITION",
            color = AccentColor.copy(0.5f),
            style = MaterialTheme.typography.overline,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- THE PROPORTIONAL BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp) // Slightly thicker for a modern look
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(0.1f))
        ) {
            weightStats.forEachIndexed { i, s ->
                Box(
                    Modifier
                        .weight(s.second.coerceAtLeast(0.01f)) // Ensure even tiny files show a sliver
                        .fillMaxHeight()
                        .background(colors.getOrElse(i) { Color.Gray })
                )
            }
        }

        // --- THE DATA LEGEND ---
        FlowRow(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp), // Use horizontalArrangement
            verticalArrangement = Arrangement.spacedBy(8.dp)     // Use verticalArrangement
        ) {
            weightStats.forEachIndexed { i, s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(colors[i]))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        // Now shows how much SPACE it takes, not just count
                        text = "${s.first.uppercase()} ${(s.second * 100).toInt()}%",
                        color = Color.White.copy(0.8f),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- MISSING COMPONENT FIX ---
@Composable
fun FileRowItem(file: FileEntry, isSelected: Boolean, isDarkMode: Boolean, onClick: () -> Unit) {
    val subTextColor = if (isDarkMode) Color.White.copy(0.4f) else Color.Black.copy(0.5f)
    val textColor = if (isDarkMode) Color.White else Color.Black

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        backgroundColor = if (isSelected) AccentColor.copy(0.1f) else Color.Transparent,
        elevation = 0.dp
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, null, tint = if (isSelected) AccentColor else subTextColor)
            Column(Modifier.padding(start = 12.dp)) {
                Text(file.name, color = textColor)
                Text(file.path, color = subTextColor, style = MaterialTheme.typography.caption, maxLines = 1)
            }
        }
    }
}

@Composable
fun DetailsPanel(file: FileEntry, isDarkMode: Boolean, onClose: () -> Unit) {
    val bgColor = if (isDarkMode) Color(0xFF0A0E14) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    Column(Modifier.width(320.dp).fillMaxHeight().background(bgColor).padding(24.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("DATA INFO", color = AccentColor, style = MaterialTheme.typography.overline)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = textColor.copy(0.5f)) }
        }
        Text(file.name, color = textColor, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
        Divider(Modifier.padding(vertical = 16.dp), textColor.copy(0.1f))

        InfoRow("TYPE", file.extension.uppercase(), isDarkMode)
        InfoRow("LOCATION", file.path, isDarkMode)

        Spacer(Modifier.weight(1f))
        Button(onClick = { revealFileInExplorer(file.path) }, Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(AccentColor)) {
            Text("REVEAL IN EXPLORER", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatItem(label: String, value: String, isDarkMode: Boolean) {
    val textColor = if (isDarkMode) Color.White else Color.Black
    Column {
        Text(label, color = AccentColor.copy(0.6f), style = MaterialTheme.typography.overline)
        Text(value, color = textColor, style = MaterialTheme.typography.body1.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun InfoRow(label: String, value: String, isDarkMode: Boolean) {
    val textColor = if (isDarkMode) Color.White else Color.Black
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, color = textColor.copy(0.4f), style = MaterialTheme.typography.caption)
        Text(value, color = textColor, style = MaterialTheme.typography.body2, modifier = Modifier.padding(top = 2.dp))
    }
}

/*fun formatFileSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) "%.2f MB".format(mb) else if (kb >= 1.0) "%.2f KB".format(kb) else "$size B"
}*/

@Composable
fun SortButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFFD2B48C) else Color.White.copy(0.3f),
            style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold)
        )
    }
}
// Ensure revealFileInExplorer and FileEntry class are available in your project scope.


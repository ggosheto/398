package com.clusterview.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun FileListView(
    clusterName: String,
    files: List<FileEntry>,
    onBack: () -> Unit,
    onSearch: (String) -> Unit
) {
    // Brand Colors
    val OxfordBlue = Color(0, 33, 71)
    val Tan = Color(210, 180, 140)
    var query by remember { mutableStateOf("")
    }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportFileName by remember { mutableStateOf("${clusterName.replace(" ", "_")}_Export") }

    Column(Modifier.fillMaxSize().background(Color(0xFFF1F3F5))) {

        // --- HEADER SECTION (Oxford Blue) ---
        Column(
            Modifier.fillMaxWidth()
                .background(OxfordBlue)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Tan)
            }

            Spacer(Modifier.height(12.dp))

            // Title and Export Button Row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = clusterName,
                        style = MaterialTheme.typography.h4,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Browsing ${files.size} categorized files",
                        color = Tan.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.body2
                    )
                }

                // EXPORT BUTTON
                Button(
                    onClick = {
                        showExportDialog = true  // <--- CHANGE THIS LINE HERE
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Tan),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.elevation(defaultElevation = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = OxfordBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "EXPORT",
                        color = OxfordBlue,
                        style = MaterialTheme.typography.button,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Search Bar
            TextField(
                value = query,
                onValueChange = { query = it; onSearch(it) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                placeholder = { Text("Filter files within this cluster...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OxfordBlue) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = OxfordBlue
                )
            )
        }

        // --- FILE LIST SECTION ---
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            items(items = files) { file ->
                Card(
                    elevation = 0.dp,
                    shape = RoundedCornerShape(10.dp),
                    backgroundColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable { openFileInSystem(file.path) }
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dynamic Icon
                        val iconColor = getIconColorForExtension(file.extension)
                        Box(
                            Modifier.size(44.dp).background(iconColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = file.extension.take(3).uppercase(),
                                color = iconColor,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        // File Info
                        Column(Modifier.weight(1f)) {
                            Text(file.name, fontWeight = FontWeight.Bold, color = OxfordBlue, maxLines = 1)
                            Text(file.path, style = MaterialTheme.typography.caption, color = Color.Gray, maxLines = 1)
                        }

                        // Action Button
                        IconButton(onClick = { openFolderInSystem(file.path) }) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Open Folder",
                                tint = Tan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        androidx.compose.ui.window.Dialog(
            onCloseRequest = { showExportDialog = false },
            title = "Export Data",
            state = androidx.compose.ui.window.rememberDialogState(width = 450.dp, height = 300.dp)
        ) {
            Column(
                Modifier.fillMaxSize().background(OxfordBlue).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Export Cluster", color = Tan, style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(8.dp))
                Text("Choose a name for your CSV report", color = Color.White.copy(alpha = 0.6f))

                Spacer(Modifier.height(24.dp))

                // Custom Styled Input
                TextField(
                    value = exportFileName,
                    onValueChange = { exportFileName = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White.copy(alpha = 0.1f),
                        textColor = Color.White,
                        cursorColor = Tan,
                        focusedIndicatorColor = Tan
                    ),
                    trailingIcon = { Text(".csv", color = Tan, modifier = Modifier.padding(end = 8.dp)) }
                )

                Spacer(Modifier.weight(1f))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = { showExportDialog = false },
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Tan.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Tan)
                    ) {
                        Text("CANCEL")
                    }

                    // Save Button
                    Button(
                        onClick = {
                            // We call the logic directly here
                            saveFilesToCSV(files, "$exportFileName.csv")
                            showExportDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Tan)
                    ) {
                        Text("SAVE FILE", color = OxfordBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

}


fun getIconColorForExtension(ext: String): Color {
    return when (ext.lowercase()) {
        "pdf" -> Color(0xFFD32F2F)      // Deep Red
        "jpg", "png", "jpeg" -> Color(0xFF388E3C) // Forest Green
        "txt", "doc", "docx" -> Color(0xFF1976D2) // Marine Blue
        "zip", "rar", "7z" -> Color(0xFFFFA000)   // Amber/Orange
        else -> Color(0xFF607D8B)        // Steel Grey for unknown types
    }
}
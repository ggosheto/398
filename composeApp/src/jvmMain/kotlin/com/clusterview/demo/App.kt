package com.clusterview.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
//import com.clusterview.openFolderPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.clusterview.demo.FileScanner.scanFolder

// 1. DATA MODELS (Ensures "Cannot infer type" errors stop)
/*data class User(val id: Int, val email: String, val username: String)
data class ClusterSummary(val id: Int, val name: String, val fileCount: Int)
data class FileEntry(val name: String, val path: String, val size: Long, val extension: String)*/

@Composable
fun App() {
    // --- 2. STATE MANAGEMENT ---
    var currentScreen by remember {
        mutableStateOf(if (AuthManager.isUserRemembered()) "home" else "login")
    }
    // Define this at the top of your App function
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }
    var currentUser by remember {
        mutableStateOf<User?>(
            if (AuthManager.isUserRemembered()) {
                DatabaseManager.getUserById(AuthManager.getSavedUserId())
            } else null
        )
    }
    var clusters by remember { mutableStateOf(emptyList<ClusterSummary>()) }
    var selectedClusterName by remember { mutableStateOf("") }
    var loadedFiles by remember { mutableStateOf(emptyList<FileEntry>()) }

    // --- 3. PERSISTENCE CHECK ---
    LaunchedEffect(Unit) {
        if (AuthManager.isUserRemembered()) {
            val userId = AuthManager.getSavedUserId()
            // Using a safe null check to avoid type mismatch
            val user = DatabaseManager.getUserById(userId) as? User
            if (user != null) {
                currentUser = user
                clusters = DatabaseManager.getClusterSummaries() // Removed 'id' to fix "Too many arguments"
                currentScreen = "home"
            }
        }
    }

    // --- 4. NAVIGATION ENGINE ---
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                "login" -> LoginView(
                    onAuthSuccess = { user ->
                        currentUser = user
                        currentScreen = "home"
                    },
                    onNavigateToSignUp = { currentScreen = "signup" } // Add this parameter to LoginView
                )
                "signup" -> SignUpView(
                    onSignUpSuccess = {
                        currentScreen = "login" // This is what makes the page change!
                    },
                    onNavigateToLogin = {
                        currentScreen = "login"
                    }
                )

                "HOME" -> {
                    HomeView(
                        user = currentUser,
                        onLogoutSuccess = {
                            currentUser = null      // 1. Clear the current user data
                            currentScreen = "LOGIN" // 2. Navigate back to the Login page
                        }
                    )
                }

                "list" -> {
                    FileListView(
                        clusterName = selectedClusterName,
                        files = loadedFiles,
                        onBack = { currentScreen = "home" },
                        onSearch = { query ->
                            // Simple filter to avoid DB calls during search
                            loadedFiles = loadedFiles.filter { it.name.contains(query, ignoreCase = true) }
                        }
                    )
                }
            }
        }
    }


    // --- COLORS ---
    val OxfordBlue = Color(0, 33, 71)
    val Tan = Color(210, 180, 140)
    val LightGrayBg = Color(0xFFF1F3F5)

    // --- STATE MANAGEMENT ---
    val scope = rememberCoroutineScope()
   // var clusters by remember { mutableStateOf(DatabaseManager.getClusterSummaries()) }
    var isScanning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("System Ready") }

    // Navigation & Search State
    //var selectedCluster by remember { mutableStateOf<ClusterSummary?>(null) }
    var filesInCluster by remember { mutableStateOf(listOf<FileEntry>()) }

    /*MaterialTheme {
        Row(Modifier.fillMaxSize().background(LightGrayBg)) {

            // --- SIDEBAR (Oxford Blue) ---
            Column(
                Modifier.width(280.dp).fillMaxHeight().background(OxfordBlue).padding(24.dp)
            ) {
                Text("ClusterView", color = Tan, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                Text("v1.0 Professional", color = Tan.copy(alpha = 0.5f), style = MaterialTheme.typography.caption)

                Spacer(Modifier.height(40.dp))

                // STATS CARD
                Card(
                    backgroundColor = Tan, // Background is Tan
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("TOTAL FILES", color = OxfordBlue, style = MaterialTheme.typography.overline, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${clusters.sumOf { it.fileCount }}",
                            color = OxfordBlue, // Text is Oxford Blue for contrast
                            style = MaterialTheme.typography.h4,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // SYSTEM HEALTH (Step 11 Addition)
                Divider(color = Tan.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(10.dp)
                            .background(if (isScanning) Color.Yellow else Color(0xFF4CAF50), CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (isScanning) "Scanning..." else statusText,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.body2
                    )
                }
                Spacer(Modifier.height(24.dp))

                // IMPORT BUTTON (Tan)
                Button(
                    enabled = !isScanning,
                    onClick = {
                        val path = openFolderPicker()
                        if (path != null) {
                            isScanning = true
                            scope.launch(Dispatchers.IO) {
                                DatabaseManager.clearAllData()
                                FileScanner.scanFolder(path) { *//* progress update *//* }
                                ClusterLogic.generateInitialClusters()
                                clusters = DatabaseManager.getClusterSummaries()
                                statusText = "Last scan: Success"
                                isScanning = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Tan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("IMPORT FOLDER", color = OxfordBlue, fontWeight = FontWeight.ExtraBold)
                }
            }

            // --- MAIN CONTENT AREA ---
            Column(Modifier.fillMaxSize()) {
                if (selectedCluster == null) {
                    // Check if we have no clusters and aren't currently scanning
                    if (clusters.isEmpty() && !isScanning) {

                        // --- PROFESSIONAL EMPTY STATE ---
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Large icon in Oxford Blue with transparency
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = OxfordBlue.copy(alpha = 0.2f),
                                modifier = Modifier.size(100.dp)
                            )

                            Spacer(Modifier.height(24.dp))

                            Text(
                                text = "No Clusters Found",
                                style = MaterialTheme.typography.h5,
                                color = OxfordBlue,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Import a folder from the sidebar to organize your files.",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 40.dp)
                            )
                        }
                    } else {
                        // --- REGULAR DASHBOARD VIEW ---
                        Column(Modifier.padding(32.dp)) {
                            Text(
                                text = "Dashboard",
                                style = MaterialTheme.typography.h4,
                                fontWeight = FontWeight.ExtraBold,
                                color = OxfordBlue
                            )
                            Text(
                                text = "Your library categorized into smart clusters",
                                color = Color.Gray
                            )

                            Spacer(Modifier.height(32.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 220.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(clusters) { cluster ->
                                    ClusterCard(cluster, onClick = {
                                        filesInCluster = DatabaseManager.getFilesByCluster(cluster.id)
                                        selectedCluster = cluster
                                    })
                                }
                            }
                        }
                    }
                } else {
                    // --- DETAILED FILE LIST VIEW ---
                    FileListView(
                        clusterName = selectedCluster!!.name,
                        files = filesInCluster,
                        onBack = { selectedCluster = null },
                        onSearch = { query ->
                            filesInCluster = DatabaseManager.searchFilesInCluster(selectedCluster!!.id, query)
                        }
                    )
                }
            }
        }
    }*/
}

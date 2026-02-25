package com.clusterview.demo

// --- CRITICAL IMPORTS: These fix 'Modifier', 'dp', 'Text', 'Button', etc. ---
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

// --- FIXES: Unresolved reference 'Screen' (15+ errors) ---
enum class Screen {
    LOGIN, DASHBOARD, MAP, DETAIL
}

@Composable
fun NavigationController() {
    // Load real clusters from file - this will be shared across screens
    val allClusters = remember { mutableStateListOf<Cluster>().apply { addAll(loadClustersFromFile()) } }

    // Add a key to trigger recomposition when returning from other screens
    var refreshKey by remember { mutableStateOf(0) }

    // Store the current authenticated user
    var currentUser by remember { mutableStateOf<User?>(null) }

    // Check if user is already logged in on startup
    var currentScreen by remember {
        mutableStateOf(
            if (AuthManager.isUserRemembered()) {
                val savedUserId = AuthManager.getSavedUserId()
                val savedUser = DatabaseManager.getUserById(savedUserId)
                if (savedUser != null) {
                    currentUser = savedUser
                    Screen.DASHBOARD
                } else {
                    Screen.LOGIN
                }
            } else {
                Screen.LOGIN
            }
        )
    }

    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }

    when (currentScreen) {
        Screen.LOGIN -> {
            AuthView(onLoginSuccess = { user ->
                currentUser = user
                currentScreen = Screen.DASHBOARD
            })
        }

        Screen.DASHBOARD -> {
            // Reload clusters when returning to dashboard
            LaunchedEffect(refreshKey) {
                allClusters.clear()
                allClusters.addAll(loadClustersFromFile())
            }

            HomeView(
                user = currentUser,
                onClusterClick = { cluster ->
                    selectedCluster = cluster
                    currentScreen = Screen.DETAIL
                },
                onOpenMap = {
                    currentScreen = Screen.MAP
                },
                onLogoutSuccess = {
                    currentUser = null
                    AuthManager.clear()
                    currentScreen = Screen.LOGIN
                }
            )
        }

        Screen.MAP -> {
            VisualizationMapView(
                clusters = allClusters,
                onBack = {
                    refreshKey++ // Trigger reload
                    currentScreen = Screen.DASHBOARD
                },
                onClusterClick = { cluster ->
                    selectedCluster = cluster
                    currentScreen = Screen.DETAIL
                }
            )
        }

        Screen.DETAIL -> {
            selectedCluster?.let { cluster ->
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Details for ${cluster.name}", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Path: ${cluster.path}")
                    Button(onClick = {
                        refreshKey++ // Trigger reload
                        currentScreen = Screen.DASHBOARD
                    }) {
                        Text("Back")
                    }
                }
            }
        }
    }
}

// Load clusters from the same file that HomeView uses
fun loadClustersFromFile(): List<Cluster> {
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
                parts[5].toBoolean()
            )
        } else if (parts.size == 5) {
            Cluster(parts[0].toInt(), parts[1], parts[3].toInt(), parts[2], parts[4], false)
        } else null
    }
}


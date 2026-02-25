package com.clusterview.demo

// --- CRITICAL IMPORTS: These fix 'Modifier', 'dp', 'Text', 'Button', etc. ---
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// --- FIXES: Unresolved reference 'Screen' (15+ errors) ---
enum class Screen {
    LOGIN, DASHBOARD, MAP, DETAIL
}

@Composable
fun NavigationController() {
    // --- FIXES: Parameter errors for path and lastModified ---
    val allClusters = remember {
        listOf(
            // Order must be: id (Int), name (String), fileCount (Int), path (String), lastModified (String)
            Cluster(1, "Alpha Node", 120, "/path/alpha", "2026-02-24"),
            Cluster(2, "Beta Node", 45, "/path/beta", "2026-02-24")
        )
    }

    val currentUser = User(
        id = 1,
        username = "admin_user",
        email = "admin@clusterview.com",
        passwordHash = "n/a", // Dummy value
        name = "Administrator"
    )

    // --- FIXES: 'by' delegate and 'selectedCluster' errors ---
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }

    when (currentScreen) {
        Screen.LOGIN -> {
            AuthView(onLoginSuccess = { currentScreen = Screen.DASHBOARD })
        }

        Screen.DASHBOARD -> {
            HomeView(
                user = currentUser,
                onLogoutSuccess = { currentScreen = Screen.LOGIN }
            )
        }

        Screen.MAP -> {
            VisualizationMapView(
                clusters = allClusters,
                onBack = { currentScreen = Screen.DASHBOARD },
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
                    Button(onClick = { currentScreen = Screen.DASHBOARD }) {
                        Text("Back")
                    }
                }
            }
        }
    }
}
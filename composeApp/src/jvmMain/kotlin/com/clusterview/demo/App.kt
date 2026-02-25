package com.clusterview.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun App() {
    var currentScreen by remember {
        mutableStateOf(if (AuthManager.isUserRemembered()) "home" else "login")
    }
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

    LaunchedEffect(Unit) {
        if (AuthManager.isUserRemembered()) {
            val userId = AuthManager.getSavedUserId()
            val user = DatabaseManager.getUserById(userId) as? User
            if (user != null) {
                currentUser = user
                clusters = DatabaseManager.getClusterSummaries()
                currentScreen = "home"
            }
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                "login" -> LoginView(
                    onAuthSuccess = { user ->
                        currentUser = user
                        currentScreen = "home"
                    },
                    onNavigateToSignUp = { currentScreen = "signup" }
                )
                "signup" -> SignUpView(
                    onSignUpSuccess = { currentScreen = "login" },
                    onNavigateToLogin = { currentScreen = "login" }
                )

                "home" -> {
                    HomeView(
                        user = currentUser,
                        onClusterClick = { cluster ->
                            selectedCluster = cluster
                            selectedClusterName = cluster.name
                            currentScreen = "list"
                        },
                        onOpenMap = {
                            currentScreen = "map"
                        },
                        onLogoutSuccess = {
                            currentUser = null
                            currentScreen = "login"
                        }
                    )
                }

                "map" -> {
                    VisualizationMapView(
                        clusters = clusters.map { Cluster(it.id, it.name, it.fileCount, "", "") },
                        onBack = { currentScreen = "home" },
                        onClusterClick = { cluster ->
                            selectedCluster = cluster
                            currentScreen = "list"
                        }
                    )
                }

                "list" -> {
                    FileListView(
                        clusterName = selectedClusterName,
                        files = loadedFiles,
                        onBack = { currentScreen = "home" },
                        onSearch = { query ->
                            loadedFiles = loadedFiles.filter { it.name.contains(query, ignoreCase = true) }
                        }
                    )
                }
            }
        }
    }

    val OxfordBlue = Color(0, 33, 71)
    val Tan = Color(210, 180, 140)
    val LightGrayBg = Color(0xFFF1F3F5)

    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("System Ready") }

    var filesInCluster by remember { mutableStateOf(listOf<FileEntry>()) }

}

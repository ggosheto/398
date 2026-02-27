package com.clusterview.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf("splash") }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }
    val selectedPath = "/user/path/to/folder"
    val clusters = remember { mutableStateListOf<Cluster>() }
    var selectedClusterName by remember { mutableStateOf("") }
    var loadedFiles by remember { mutableStateOf(emptyList<FileEntry>()) }
    val folder = java.io.File(selectedPath)
    val actualFiles = folder.listFiles()?.filter { it.isFile }?.map { it.name } ?: emptyList()

    LaunchedEffect(Unit) {
        if (AuthManager.isUserRemembered()) {
            val savedId = AuthManager.getSavedUserId()
            val user = DatabaseManager.getUserById(savedId)

            if (user != null) {
                currentUser = user
                currentScreen = "home"
            } else {
                currentScreen = "login"
            }
        } else {
            currentScreen = "login"
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
                    onSignUpSuccess = { user ->
                        if (user != null) {
                            currentUser = user
                            currentScreen = "home"
                        } else {
                            currentScreen = "login"
                        }
                    },
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
                            AuthManager.clear()
                            currentScreen = "login"
                        }
                    )
                }

                "map" -> {
                    VisualizationMapView(
                        clusters = clusters,
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
}
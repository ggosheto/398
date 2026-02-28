package com.clusterview.demo

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp

fun main() = application {
    DatabaseManager.init()

    val clusters = remember { mutableStateListOf<Cluster>() }
    var currentScreen by remember { mutableStateOf("login") }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }
    var clipboardFiles by remember { mutableStateOf(setOf<FileEntry>()) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "ClusterView",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        MaterialTheme {
            NavigationController(
                currentScreen = currentScreen,
                currentUser = currentUser,
                clusters = clusters,
                onClusterClick = { cluster ->
                    selectedCluster = cluster
                    currentScreen = "list"
                },
                onOpenMap = { currentScreen = "map" },
                onLogoutSuccess = {
                    currentUser = null
                    clusters.clear()
                    currentScreen = "login"
                },
                onCreateNewCluster = { name: String, pcPath: String ->
                    val newCluster = Cluster(
                        id = clusters.size + 1,
                        name = name,
                        path = pcPath,
                        fileCount = 0,
                        lastModified = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy (HH:mm)")),
                        files = emptyList(),
                        isUserCreated = true
                    )

                    clusters.add(newCluster)
                },
                onBack = {
                    currentScreen = "home"
                }
            )
        }
    }
}
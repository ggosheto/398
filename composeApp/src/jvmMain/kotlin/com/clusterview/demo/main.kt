package com.clusterview.demo

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    // Top-level state that persists across screens
    val clusters = remember { mutableStateListOf<Cluster>() }
    var currentScreen by remember { mutableStateOf("login") }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }
    var clipboardFiles by remember { mutableStateOf(setOf<FileEntry>()) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "ClusterView",
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
                    clusters.add(Cluster(
                        id = clusters.size + 1,
                        name = name,
                        path = pcPath,
                        fileCount = 0,
                        lastModified = "Created Now",
                        files = emptyList(),
                        isUserCreated = true
                    ))
                },
                onBack = { currentScreen = "home" }
            )
        }
    }
}
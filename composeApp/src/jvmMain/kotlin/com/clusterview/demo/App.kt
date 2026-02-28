
package com.clusterview.demo

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.io.File

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf("splash") }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }
    var selectedClusterName by remember { mutableStateOf("") }

    val clusters = remember { mutableStateListOf<Cluster>() }
    var clipboardFiles by remember { mutableStateOf(setOf<FileEntry>()) }

    LaunchedEffect(Unit) {
        DatabaseManager.init()
        if (AuthManager.isUserRemembered()) {
            val savedId = AuthManager.getSavedUserId()
            val user = DatabaseManager.getUserById(savedId)
            if (user != null) {
                currentUser = user
                currentScreen = "home"
            } else { currentScreen = "login" }
        } else { currentScreen = "login" }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(targetState = currentScreen) { target ->
                when (target) {
                    "login" -> LoginView(
                        onAuthSuccess = { user -> currentUser = user; currentScreen = "home" },
                        onNavigateToSignUp = { currentScreen = "signup" }
                    )

                    "home" -> HomeView(
                        user = currentUser,
                        onClusterClick = { cluster ->
                            selectedCluster = cluster
                            selectedClusterName = cluster.name
                            currentScreen = "list"
                        },
                        onOpenMap = { currentScreen = "map" },
                        onLogoutSuccess = {
                            currentUser = null
                            AuthManager.clear()
                            clusters.clear()
                            clipboardFiles = emptySet()
                            currentScreen = "login"
                        }
                    )

                    "list" -> {
                        // FIX: Mapping String to FileEntry with correct types (Long for lastModified)
                        val mappedFiles = selectedCluster?.files?.map { fileName ->
                            FileEntry(
                                name = fileName,
                                size = 0L, // Use 0L to satisfy Long type requirement
                                extension = fileName.substringAfterLast(".", ""),
                                lastModified = 0L, // Use 0L to satisfy Long type requirement
                                path = "${selectedCluster?.path}${File.separator}$fileName"
                            )
                        } ?: emptyList()

                        FileListView(
                            clusterName = selectedClusterName,
                            files = mappedFiles,
                            isUserCreated = selectedCluster?.isUserCreated ?: false,
                            onBack = { currentScreen = "home" },
                            onSearch = { /* Search handled in View */ },
                            onCopyFile = { fileEntry ->
                                clipboardFiles = clipboardFiles + fileEntry
                            },
                            onPasteFiles = {
                                selectedCluster?.let { cluster ->
                                    val updatedFiles = cluster.files.toMutableList()
                                    clipboardFiles.forEach { updatedFiles.add(it.name) }
                                    val index = clusters.indexOf(cluster)
                                    if (index != -1) {
                                        clusters[index] = cluster.copy(
                                            files = updatedFiles.distinct(),
                                            fileCount = updatedFiles.distinct().size
                                        )
                                    }
                                }
                                clipboardFiles = emptySet()
                            }
                        )
                    }
                }
            }
        }
    }
}

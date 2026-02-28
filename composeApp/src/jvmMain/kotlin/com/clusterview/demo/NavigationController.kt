package com.clusterview.demo

import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import java.io.File

enum class Screen {
    LOGIN, DASHBOARD, MAP, DETAIL
}

@Composable
fun NavigationController(
    currentScreen: String,
    currentUser: User?,
    clusters: List<Cluster>,
    onClusterClick: (Cluster) -> Unit,
    onOpenMap: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onCreateNewCluster: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val allClusters = remember { mutableStateListOf<Cluster>().apply { addAll(loadClustersFromDatabase(currentUser?.id ?: -1)) } }

    var refreshKey by remember { mutableStateOf(0) }

    var currentUser by remember { mutableStateOf<User?>(null) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.LOGIN -> {
                AuthView(onLoginSuccess = { user ->
                    currentUser = user
                    currentScreen = Screen.DASHBOARD
                })
            }

            Screen.DASHBOARD -> {
                LaunchedEffect(refreshKey) {
                    allClusters.clear()
                    allClusters.addAll(loadClustersFromDatabase(currentUser?.id ?: -1))
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
                        refreshKey++
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
                    Box(modifier = Modifier.fillMaxSize().background(VelvetTheme.CoreGradient)) {
                        ClusterDetailView(
                            cluster = cluster,
                            onRefresh = {
                                val folder = File(cluster.path)
                                if (folder.exists()) {
                                    val newCount = folder.listFiles()?.filter { it.isFile }?.size ?: 0
                                    val newTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy (HH:mm)"))
                                    val duplicatesFound = hasDuplicates(cluster.path)
                                    val index = allClusters.indexOfFirst { it.id == cluster.id }
                                    if (index != -1) {
                                        val updated = cluster.copy(
                                            fileCount = newCount,
                                            lastModified = newTime,
                                            hasDuplicates = duplicatesFound
                                        )
                                        allClusters[index] = updated
                                        selectedCluster = updated
                                        saveClustersToDatabase(allClusters, currentUser?.id ?: -1)
                                    }
                                }
                            },
                            onBack = {
                                refreshKey++
                                currentScreen = Screen.DASHBOARD
                            }
                        )
                    }
                }
            }
        }
    }
}

package com.clusterview.demo

import VisualizationMapView
import androidx.compose.runtime.*

// This enum helps us keep track of where we are without using messy strings
enum class Screen {
    DASHBOARD,
    DETAIL,
    MAP
}

@Composable
fun NavigationController(allClusters: List<Cluster>) {
    // 1. STATE: Which screen are we on?
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

    // 2. STATE: Which cluster is currently being inspected?
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }

    // 3. THE SWITCHBOARD
    when (currentScreen) {
        Screen.DASHBOARD -> {
            HomeView(
                onClusterClick = { cluster ->
                    selectedCluster = cluster
                    currentScreen = Screen.DETAIL
                },
                onOpenMap = {
                    currentScreen = Screen.MAP
                },
                // --- ADD THIS LINE TO FIX THE ERROR ---
                onLogoutSuccess = {
                    // Tell the app what to do!
                    // Usually, this means navigating to a Login screen
                    // or simply printing a message for now:
                    println("User logged out")
                }
            )
        }

        Screen.DETAIL -> {
            selectedCluster?.let { cluster ->
                ClusterDetailView(
                    cluster = cluster,
                    onRefresh = { /* Your refresh logic */ },
                    onBack = { currentScreen = Screen.DASHBOARD }
                )
            }
        }

        Screen.MAP -> {
            VisualizationMapView(
                clusters = allClusters,
                onBack = { currentScreen = Screen.DASHBOARD }
            )
        }
    }
}
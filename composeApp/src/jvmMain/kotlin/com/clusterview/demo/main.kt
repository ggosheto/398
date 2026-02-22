package com.clusterview.demo

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.Window



/*
fun main() = application {
    // Set the window size to a cinematic wide ratio
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "CLUSTER VIEW // SYSTEM ARCHIVE",
        undecorated = false // Keep this false for now so you can close it easily
    ) {
        // THIS IS THE KEY:
        // It must call App() and nothing else.
        App()
    }
}*/

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "ClusterView Topology") {
        // You'll need your list of clusters here
        val myClusters = loadClusters() // Ensure loadClusters() returns List<Cluster>
        NavigationController(allClusters = myClusters)
    }
}

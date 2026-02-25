package com.clusterview.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.remember

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ClusterView Topology"
    ) {
        NavigationController()
    }
}
package com.clusterview.demo

import java.io.File

object FileScanner {

    fun scanFolder(rootPath: String, onProgress: (Int) -> Unit) {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) return

        DatabaseManager.seedClusters()

        val allFiles = mutableListOf<FileEntry>()
        val filesToProcess = root.walkTopDown()
            .filter { it.isFile && !it.name.startsWith(".") }
            .toList()

        val totalCount = filesToProcess.size

        filesToProcess.forEachIndexed { index, file ->
            val ext = file.extension.lowercase()

            val entry = FileEntry(
                id = 0,
                name = file.name,
                path = file.absolutePath,
                extension = ext,
                size = file.length(),
                lastModified = file.lastModified(),
                clusterId = mapExtensionToClusterId(ext)
            )

            allFiles.add(entry)

            if (index % 50 == 0) {
                val percentage = ((index.toFloat() / totalCount) * 100).toInt()
                onProgress(percentage)
            }
        }

        DatabaseManager.batchInsertFiles(allFiles)
        onProgress(100)
    }

    private fun mapExtensionToClusterId(extension: String): Int {
        return when (extension) {
            "jpg", "jpeg", "png", "gif", "webp" -> 1 // Images
            "pdf", "doc", "docx", "txt", "md", "xlsx" -> 2 // Documents
            "mp4", "mkv", "mov", "avi" -> 3 // Video
            "mp3", "wav", "flac", "aac" -> 4 // Audio
            else -> 5
        }
    }

    fun openFileInSystem(path: String) {
        try {
            val file = java.io.File(path)
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file)
            }
        } catch (e: Exception) {
            println("Error opening file: ${e.message}")
        }
    }

    fun revealFileInExplorer(path: String) {
        try {
            val file = java.io.File(path)
            val os = System.getProperty("os.name").lowercase()

            when {
                os.contains("win") -> {
                    Runtime.getRuntime().exec("explorer.exe /select, \"${file.absolutePath}\"")
                }
                os.contains("mac") -> {
                    Runtime.getRuntime().exec(arrayOf("open", "-R", file.absolutePath))
                }
                else -> {
                    java.awt.Desktop.getDesktop().open(file.parentFile)
                }
            }
        } catch (e: Exception) {
            println("Error revealing file: ${e.message}")
        }
    }
}
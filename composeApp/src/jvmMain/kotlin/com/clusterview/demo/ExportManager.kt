package com.clusterview.demo

object ExportManager {
    fun exportToFolder(clusterName: String, files: List<FileEntry>) {
        val exportRoot = java.io.File("Exported_Clusters")
        val clusterFolder = java.io.File(exportRoot, clusterName)

        try {
            if (!clusterFolder.exists()) {
                val created = clusterFolder.mkdirs()
                println("Folder creation status: $created")
            }

            files.forEach { fileEntry ->
                val source = java.io.File(fileEntry.path)
                val destination = java.io.File(clusterFolder, source.name)

                if (source.exists()) {
                    source.copyTo(destination, overwrite = true)
                }
            }

            println("Export Complete: ${files.size} files moved to ${clusterFolder.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
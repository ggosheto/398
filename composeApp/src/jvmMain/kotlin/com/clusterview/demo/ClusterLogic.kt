package com.clusterview.demo

import java.sql.Statement

object ClusterLogic {

    fun generateInitialClusters() {
        val files = DatabaseManager.getAllFiles()

        val groups = files.groupBy { it.extension }

        DatabaseManager.getConnection().use { conn ->
            conn.autoCommit = false
            try {
                groups.forEach { (ext, fileList) ->
                    val clusterName = if (ext.isEmpty()) "Unknown" else ext.uppercase()
                    val color = getRandomColor()

                    val clusterId = insertClusterAndGetId(clusterName, color)

                    updateFilesWithClusterId(fileList.map { it.id }, clusterId)
                }
                conn.commit()
            } catch (e: Exception) {
                conn.rollback()
                e.printStackTrace()
            }
        }
    }

    private fun getRandomColor(): String {
        val colors = listOf("#5865F2", "#EB459E", "#FEE75C", "#57F287", "#ED4245", "#9B59B6")
        return colors.random()
    }

    private fun insertClusterAndGetId(name: String, color: String): Int {
        val sql = "INSERT INTO clusters (name, color) VALUES (?, ?)"
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                pstmt.setString(1, name)
                pstmt.setString(2, color)
                pstmt.executeUpdate()
                val rs = pstmt.generatedKeys
                return if (rs.next()) rs.getInt(1) else -1
            }
        }
    }

    private fun updateFilesWithClusterId(fileIds: List<Int>, clusterId: Int) {
        val sql = "UPDATE files SET cluster_id = ? WHERE id = ?"
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { pstmt ->
                for (id in fileIds) {
                    pstmt.setInt(1, clusterId)
                    pstmt.setInt(2, id)
                    pstmt.addBatch()
                }
                pstmt.executeBatch()
            }
        }
    }
}
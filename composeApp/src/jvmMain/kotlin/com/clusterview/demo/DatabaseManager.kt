package com.clusterview.demo

import java.io.File
import java.sql.Connection
import java.sql.DriverManager

object DatabaseManager {
    private val userHome = System.getProperty("user.home")
    private val appDataFolder = File(userHome, "ClusterViewData")
    private val dbFile = File(appDataFolder, "clusterview.db")
    private val DB_URL = "jdbc:sqlite:${dbFile.absolutePath}"
    private val storageFile = File(appDataFolder, "users_db.txt")

    private val registeredUsers = mutableListOf<User>()
    private var connection: Connection? = null

    fun init() {
        if (!appDataFolder.exists()) {
            appDataFolder.mkdirs()
        }
        initDatabase()
        loadUsersFromDisk()
    }

    fun getConnection(): Connection {
        if (!appDataFolder.exists()) appDataFolder.mkdirs()

        if (connection == null || connection!!.isClosed) {
            Class.forName("org.sqlite.JDBC")
            connection = DriverManager.getConnection(DB_URL)
        }
        return connection!!
    }

    private fun loadUsersFromDisk() {
        if (storageFile.exists()) {
            registeredUsers.clear()
            storageFile.readLines().forEach { line ->
                val parts = line.split("|")
                if (parts.size == 5) {
                    registeredUsers.add(User(parts[0].toInt(), parts[1], parts[2], parts[3], parts[4]))
                }
            }
        }
    }

    private fun saveUserToDisk(user: User) {
        val data = "${user.id}|${user.email}|${user.username}|${user.passwordHash}|${user.name}\n"
        storageFile.appendText(data)
    }

    fun initDatabase() {
        try {
            getConnection().use { conn ->
                val statement = conn.createStatement()
                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS clusters (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        color TEXT,
                        path TEXT
                    )
                """.trimIndent())

                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS files (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        path TEXT UNIQUE NOT NULL,
                        extension TEXT,
                        size INTEGER,
                        last_modified TEXT,
                        cluster_id INTEGER,
                        FOREIGN KEY (cluster_id) REFERENCES clusters(id)
                    )
                """.trimIndent())

                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_file_name ON files(name)")
                seedClusters()
            }
        } catch (e: Exception) {
            println("Database Init Error: ${e.message}")
        }
    }

    fun insertFile(file: FileEntry) {
        val sql = "INSERT OR REPLACE INTO files (name, path, extension, size, last_modified, cluster_id) VALUES (?, ?, ?, ?, ?, ?)"
        getConnection().prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, file.name)
            pstmt.setString(2, file.path)
            pstmt.setString(3, file.extension)
            pstmt.setLong(4, file.size)
            pstmt.setString(5, file.lastModified.toString())
            pstmt.setObject(6, file.clusterId)
            pstmt.executeUpdate()
        }
    }

    fun getAllFiles(): List<FileEntry> {
        val files = mutableListOf<FileEntry>()
        getConnection().use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT * FROM files")
            while (rs.next()) {
                files.add(FileEntry(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    path = rs.getString("path"),
                    extension = rs.getString("extension"),
                    size = rs.getLong("size"),
                    lastModified = rs.getString("last_modified")?.toLongOrNull() ?: 0L,
                    clusterId = rs.getInt("cluster_id")
                ))
            }
        }
        return files
    }

    fun batchInsertFiles(files: List<FileEntry>) {
        val sql = "INSERT OR REPLACE INTO files (name, path, extension, size, last_modified, cluster_id) VALUES (?, ?, ?, ?, ?, ?)"
        val conn = getConnection()
        conn.autoCommit = false
        try {
            conn.prepareStatement(sql).use { pstmt ->
                for (file in files) {
                    pstmt.setString(1, file.name)
                    pstmt.setString(2, file.path)
                    pstmt.setString(3, file.extension)
                    pstmt.setLong(4, file.size)
                    pstmt.setString(5, file.lastModified.toString())
                    pstmt.setObject(6, file.clusterId)
                    pstmt.addBatch()
                }
                pstmt.executeBatch()
            }
            conn.commit()
        } catch (e: Exception) {
            conn.rollback()
            throw e
        } finally {
            conn.autoCommit = true
        }
    }

    fun clearAllData() {
        getConnection().use { conn ->
            val statement = conn.createStatement()
            statement.executeUpdate("DELETE FROM files")
            statement.executeUpdate("DELETE FROM clusters")
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='files' OR name='clusters'")
        }
    }

    fun getFilesByCluster(clusterId: Int): List<FileEntry> {
        val files = mutableListOf<FileEntry>()
        getConnection().prepareStatement("SELECT * FROM files WHERE cluster_id = ?").use { pstmt ->
            pstmt.setInt(1, clusterId)
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                files.add(FileEntry(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    path = rs.getString("path"),
                    extension = rs.getString("extension"),
                    size = rs.getLong("size"),
                    lastModified = rs.getString("last_modified")?.toLongOrNull() ?: 0L,
                    clusterId = rs.getInt("cluster_id")
                ))
            }
        }
        return files
    }

    fun searchFilesInCluster(clusterId: Int, query: String): List<FileEntry> {
        val files = mutableListOf<FileEntry>()
        getConnection().prepareStatement("SELECT * FROM files WHERE cluster_id = ? AND name LIKE ?").use { pstmt ->
            pstmt.setInt(1, clusterId)
            pstmt.setString(2, "%$query%")
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                files.add(FileEntry(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    path = rs.getString("path"),
                    extension = rs.getString("extension"),
                    size = rs.getLong("size"),
                    lastModified = rs.getString("last_modified")?.toLongOrNull() ?: 0L,
                    clusterId = rs.getInt("cluster_id")
                ))
            }
        }
        return files
    }

    fun getClusterIdForExtension(extension: String): Int {
        return when (extension.lowercase()) {
            "jpg", "png", "gif", "svg" -> 1
            "pdf", "docx", "txt", "xlsx" -> 2
            "mp4", "mov", "avi" -> 3
            "mp3", "wav", "flac" -> 4
            else -> 5
        }
    }

    fun seedClusters() {
        val categories = listOf(
            "Images" to "#FFD2B48C",
            "Documents" to "#FF4FC3F7",
            "Video" to "#FFBA68C8",
            "Audio" to "#FFFF8A65",
            "Archive" to "#FF90A4AE"
        )
        try {
            getConnection().use { conn ->
                val sql = "INSERT OR IGNORE INTO clusters (id, name, color) VALUES (?, ?, ?)"
                conn.prepareStatement(sql).use { pstmt ->
                    categories.forEachIndexed { index, pair ->
                        pstmt.setInt(1, index + 1)
                        pstmt.setString(2, pair.first)
                        pstmt.setString(3, pair.second)
                        pstmt.addBatch()
                    }
                    pstmt.executeBatch()
                }
            }
        } catch (e: Exception) { }
    }

    fun getClusterStats(clusterId: Int): Pair<Int, Long> {
        var count = 0
        var totalSize = 0L
        val sql = "SELECT COUNT(*), SUM(size) FROM files WHERE cluster_id = ?"
        getConnection().prepareStatement(sql).use { pstmt ->
            pstmt.setInt(1, clusterId)
            val rs = pstmt.executeQuery()
            if (rs.next()) {
                count = rs.getInt(1)
                totalSize = rs.getLong(2)
            }
        }
        return Pair(count, totalSize)
    }

    fun getUserById(userId: Int): User? {
        return try {
            User(userId, "user@example.com", "OlympiadCandidate", "mock_hash", "Olympiad Candidate")
        } catch (e: Exception) { null }
    }

    fun getUserByEmail(email: String): User? {
        return User(1, email, "OlympiadUser", "mock_hash", "Olympiad User")
    }

    fun getClusterSummaries(): List<ClusterSummary> = emptyList()

    fun registerUser(email: String, pass: String): String? {
        if (registeredUsers.any { it.email.equals(email, ignoreCase = true) }) {
            return "EMAIL ALREADY REGISTERED"
        }
        val newUser = User(registeredUsers.size + 1, email, email.substringBefore("@"), pass, email.substringBefore("@"))
        registeredUsers.add(newUser)
        saveUserToDisk(newUser)
        return null
    }

    fun verifyLogin(email: String, pass: String): User? {
        if (registeredUsers.isEmpty()) loadUsersFromDisk()
        return registeredUsers.find { it.email == email && it.passwordHash == pass }
    }

    fun deleteCluster(clusterId: Int): Boolean {
        return try {
            getConnection().use { conn ->
                val deleteFilesSql = "DELETE FROM files WHERE cluster_id = ?"
                conn.prepareStatement(deleteFilesSql).use { pstmt ->
                    pstmt.setInt(1, clusterId)
                    pstmt.executeUpdate()
                }
                val deleteClusterSql = "DELETE FROM clusters WHERE id = ?"
                conn.prepareStatement(deleteClusterSql).use { pstmt ->
                    pstmt.setInt(1, clusterId)
                    pstmt.executeUpdate()
                }
            }
            true
        } catch (e: Exception) { false }
    }

    fun getClusterWithFiles(path: String): List<String> {
        val folder = File(path)
        return if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.filter { it.isFile }?.map { it.name } ?: emptyList()
        } else emptyList()
    }

    fun getFilesFromPath(path: String): List<String> {
        val folder = File(path)
        return if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.filter { it.isFile }?.map { it.name } ?: emptyList()
        } else emptyList()
    }

    fun closeDatabase() {
        connection?.close()
    }
}
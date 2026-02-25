package com.clusterview.demo

data class FileEntry(
    val id: Int = 0,
    val name: String,
    val path: String,
    val extension: String,
    val size: Long,
    val lastModified: Long,
    val clusterId: Int? = null,
    var isStarred: Boolean = false
)

data class Cluster(
    val id: Int,
    val name: String,
    val fileCount: Int,
    val path: String,
    val lastModified: String,
    val hasDuplicates: Boolean = false
)

data class User(
    val id: Int,
    val email: String,
    val username: String,
    val passwordHash: String,
    val name: String
)
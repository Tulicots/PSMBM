package com.example.model

data class SambaServer(
    val id: String,
    val name: String,
    val hostIp: String,
    val port: Int = 445,
    val shareName: String = "Music",
    val username: String = "",
    val domain: String = "",
    val isAnonymous: Boolean = false,
    val isConnected: Boolean = false,
    val trackCount: Int = 0
)

data class SambaShareItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0,
    val format: AudioFormat? = null
)

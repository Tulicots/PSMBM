package com.example.model

enum class SmbConnectionStatus {
    IDLE,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class SambaServer(
    val id: String,
    val name: String,
    val hostIp: String,
    val port: Int = 445,
    val shareName: String = "Musica",
    val username: String = "",
    val password: String = "",
    val domain: String = "",
    val isAnonymous: Boolean = true,
    val isConnected: Boolean = false,
    val currentPath: String = "/",
    val trackCount: Int = 0
)

data class SambaShareItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0,
    val format: AudioFormat? = null
) {
    val isAudioFile: Boolean get() = !isDirectory && format != null

    fun formattedSize(): String {
        if (isDirectory) return "Carpeta"
        if (sizeBytes <= 0) return "0 MB"
        val mb = sizeBytes / (1024f * 1024f)
        return "%.1f MB".format(mb)
    }
}

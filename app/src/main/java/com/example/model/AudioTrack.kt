package com.example.model

enum class AudioFormat(
    val extension: String,
    val displayName: String,
    val badgeColorHex: Long
) {
    FLAC("flac", "FLAC 24-bit Hi-Res", 0xFF10B981),
    MP3("mp3", "MP3 320 kbps", 0xFF3B82F6),
    M4A("m4a", "M4A Lossless", 0xFF8B5CF6),
    WAV("wav", "WAV Studio Master", 0xFFF59E0B),
    OGG("ogg", "OGG Vorbis", 0xFFEC4899);

    companion object {
        fun fromFileName(name: String): AudioFormat {
            val ext = name.substringAfterLast('.', "").lowercase()
            return entries.firstOrNull { it.extension == ext } ?: MP3
        }
    }
}

enum class TrackSource {
    LOCAL_STORAGE,
    SAMBA_SERVER,
    HI_FI_DEMO
}

data class AudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val format: AudioFormat,
    val qualityDetails: String = "44.1 kHz / 16-bit",
    val uri: String,
    val coverUrl: String? = null,
    val source: TrackSource = TrackSource.HI_FI_DEMO,
    val fileSizeMb: Float = 0f
) {
    fun formattedDuration(): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}

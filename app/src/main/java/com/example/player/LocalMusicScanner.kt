package com.example.player

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.model.AudioFormat
import com.example.model.AudioTrack
import com.example.model.TrackSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object LocalMusicScanner {
    private const val TAG = "LocalMusicScanner"
    private const val PREFS_NAME = "sonora_music_scanner_prefs"
    private const val KEY_CUSTOM_FOLDERS = "custom_folders"

    private val supportedExtensions = setOf("flac", "mp3", "m4a", "wav", "ogg", "aac", "opus")

    fun getCustomFolders(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_CUSTOM_FOLDERS, emptySet()) ?: emptySet()
        return set.toList()
    }

    fun addCustomFolder(context: Context, folderPath: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_CUSTOM_FOLDERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(folderPath)
        prefs.edit().putStringSet(KEY_CUSTOM_FOLDERS, current).apply()
    }

    fun removeCustomFolder(context: Context, folderPath: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_CUSTOM_FOLDERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(folderPath)
        prefs.edit().putStringSet(KEY_CUSTOM_FOLDERS, current).apply()
    }

    /**
     * Scans all standard device music locations (MediaStore, /Music, /Downloads,
     * external USB storage for Android TV, and user custom folders).
     */
    suspend fun scanAll(context: Context): List<AudioTrack> = withContext(Dispatchers.IO) {
        val results = mutableListOf<AudioTrack>()
        val seenPaths = mutableSetOf<String>()

        // 1. Scan via MediaStore
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol).toString()
                    val path = cursor.getString(dataCol) ?: continue
                    if (seenPaths.contains(path)) continue
                    seenPaths.add(path)

                    val title = cursor.getString(titleCol) ?: File(path).nameWithoutExtension
                    val artist = cursor.getString(artistCol)?.takeIf { it != "<unknown>" } ?: "Artista Local"
                    val album = cursor.getString(albumCol)?.takeIf { it != "<unknown>" } ?: "Almacenamiento Local"
                    val duration = cursor.getLong(durCol)
                    val sizeBytes = cursor.getLong(sizeCol)
                    val format = AudioFormat.fromFileName(path)

                    results.add(
                        AudioTrack(
                            id = "local_ms_$id",
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = if (duration > 0) duration else 180000L,
                            format = format,
                            qualityDetails = "📱 Local • ${format.displayName}",
                            uri = if (path.startsWith("file://") || path.startsWith("content://")) path else "file://$path",
                            source = TrackSource.LOCAL_STORAGE,
                            fileSizeMb = (sizeBytes / (1024f * 1024f))
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MediaStore scan failed or permission restricted", e)
        }

        // 2. Direct directory scanning (Standard directories on Android & Android TV)
        val candidateFolders = mutableListOf<File>()

        try {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)?.let { candidateFolders.add(it) }
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.let { candidateFolders.add(it) }
            File("/storage/emulated/0/Music").takeIf { it.exists() }?.let { candidateFolders.add(it) }
            File("/storage/emulated/0/Download").takeIf { it.exists() }?.let { candidateFolders.add(it) }

            // Android TV external USB drives mounted in /storage/
            val storageRoot = File("/storage")
            if (storageRoot.exists() && storageRoot.isDirectory) {
                storageRoot.listFiles()?.forEach { drive ->
                    if (drive.isDirectory && drive.name != "emulated" && drive.name != "self") {
                        candidateFolders.add(drive)
                    }
                }
            }

            // Custom user folders from Settings
            getCustomFolders(context).forEach { customPath ->
                val folder = File(customPath)
                if (folder.exists() && folder.isDirectory) {
                    candidateFolders.add(folder)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error enumerating storage directories", e)
        }

        // Recursively scan files in folders
        for (folder in candidateFolders.distinctBy { it.absolutePath }) {
            scanDirectoryRecursively(folder, seenPaths, results)
        }

        // 3. Fallback demo tracks if device has zero music files, so UI is never empty
        val sampleTracks = SampleMusicProvider.getSampleTracks(context)
        (results + sampleTracks).distinctBy { it.id }
    }

    private fun scanDirectoryRecursively(
        directory: File,
        seenPaths: MutableSet<String>,
        outputList: MutableList<AudioTrack>,
        depth: Int = 0
    ) {
        if (depth > 4 || !directory.exists() || !directory.isDirectory) return

        try {
            val files = directory.listFiles() ?: return
            for (file in files) {
                if (file.isDirectory) {
                    // Skip hidden directories like .thumbnails, Android/data
                    if (!file.name.startsWith(".") && file.name != "Android") {
                        scanDirectoryRecursively(file, seenPaths, outputList, depth + 1)
                    }
                } else if (file.isFile) {
                    val ext = file.extension.lowercase()
                    if (supportedExtensions.contains(ext) && !seenPaths.contains(file.absolutePath)) {
                        seenPaths.add(file.absolutePath)
                        val track = buildTrackFromFile(file)
                        outputList.add(track)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore access errors on restricted subdirectories
        }
    }

    private fun buildTrackFromFile(file: File): AudioTrack {
        var title = file.nameWithoutExtension.replace('_', ' ')
        var artist = "Archivo Local"
        var album = file.parentFile?.name ?: "Almacenamiento"
        var durationMs = 180000L

        // Try reading embedded tags via MediaMetadataRetriever
        try {
            MediaMetadataRetriever().use { mmr ->
                mmr.setDataSource(file.absolutePath)
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let {
                    if (it.isNotBlank()) title = it
                }
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let {
                    if (it.isNotBlank()) artist = it
                }
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.let {
                    if (it.isNotBlank()) album = it
                }
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let {
                    if (it > 0) durationMs = it
                }
            }
        } catch (e: Exception) {
            // Fallback to filename parsing if tag retriever is unsupported for format
            if (title.contains("-")) {
                val parts = title.split("-", limit = 2)
                artist = parts[0].trim()
                title = parts[1].trim()
            }
        }

        val format = AudioFormat.fromFileName(file.name)
        val sizeBytes = file.length()

        return AudioTrack(
            id = "local_file_${file.absolutePath.hashCode()}",
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            format = format,
            qualityDetails = "📱 Local • ${format.displayName}",
            uri = "file://${file.absolutePath}",
            source = TrackSource.LOCAL_STORAGE,
            fileSizeMb = (sizeBytes / (1024f * 1024f))
        )
    }
}

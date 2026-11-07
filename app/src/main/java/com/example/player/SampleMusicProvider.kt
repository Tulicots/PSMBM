package com.example.player

import android.content.Context
import android.provider.MediaStore
import com.example.model.AudioFormat
import com.example.model.AudioTrack
import com.example.model.TrackSource

object SampleMusicProvider {

    // Royalty-free demo tracks that showcase different formats and playback capabilities
    val defaultSampleTracks = listOf(
        AudioTrack(
            id = "demo_1_flac",
            title = "Aura Nocturna (Hi-Res Acoustic)",
            artist = "Sonora Studio Ensemble",
            album = "Audiophile Sessions Vol. 1",
            durationMs = 214000L,
            format = AudioFormat.FLAC,
            qualityDetails = "96.0 kHz / 24-bit • 2840 kbps",
            uri = "https://commondatastorage.googleapis.com/codeskulptor-demos/riceracer_assets/music/race.ogg",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            source = TrackSource.HI_FI_DEMO,
            fileSizeMb = 48.5f
        ),
        AudioTrack(
            id = "demo_2_mp3",
            title = "Midnight City Lights",
            artist = "Synthetica",
            album = "Neon Drift Horizon",
            durationMs = 178000L,
            format = AudioFormat.MP3,
            qualityDetails = "44.1 kHz / 16-bit • 320 kbps CBR",
            uri = "https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Kangaroo_MusiQue_-_The_Neverending_Story.mp3",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            source = TrackSource.HI_FI_DEMO,
            fileSizeMb = 8.2f
        ),
        AudioTrack(
            id = "demo_3_m4a",
            title = "Analog Dreamscape (Lossless AAC)",
            artist = "The Velvet Frequency",
            album = "Tape & Tube Memories",
            durationMs = 195000L,
            format = AudioFormat.M4A,
            qualityDetails = "48.0 kHz / 24-bit • 256 kbps VBR",
            uri = "https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Sevish_-__nbsp_.mp3",
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            source = TrackSource.HI_FI_DEMO,
            fileSizeMb = 7.1f
        ),
        AudioTrack(
            id = "demo_4_wav",
            title = "Cosmic Reverberation",
            artist = "Astral Sound Lab",
            album = "Master Tape Direct",
            durationMs = 240000L,
            format = AudioFormat.WAV,
            qualityDetails = "192.0 kHz / 32-bit Float • Uncompressed",
            uri = "https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg",
            coverUrl = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=600&auto=format&fit=crop&q=80",
            source = TrackSource.HI_FI_DEMO,
            fileSizeMb = 65.4f
        ),
        AudioTrack(
            id = "demo_5_ogg",
            title = "Nordic Horizon Strings",
            artist = "Fjord Symphony",
            album = "Northern Echoes",
            durationMs = 162000L,
            format = AudioFormat.OGG,
            qualityDetails = "44.1 kHz / 16-bit • 500 kbps Q10",
            uri = "https://commondatastorage.googleapis.com/codeskulptor-assets/sounddogs/soundtrack.ogg",
            coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80",
            source = TrackSource.HI_FI_DEMO,
            fileSizeMb = 6.4f
        )
    )

    fun scanLocalTracks(context: Context): List<AudioTrack> {
        val tracks = mutableListOf<AudioTrack>()
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
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

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
                    val title = cursor.getString(titleCol) ?: "Pista desconocida"
                    val artist = cursor.getString(artistCol) ?: "Artista desconocido"
                    val album = cursor.getString(albumCol) ?: "Álbum local"
                    val duration = cursor.getLong(durCol)
                    val path = cursor.getString(dataCol) ?: ""
                    val sizeBytes = cursor.getLong(sizeCol)
                    val format = AudioFormat.fromFileName(path)

                    tracks.add(
                        AudioTrack(
                            id = "local_$id",
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = if (duration > 0) duration else 180000L,
                            format = format,
                            qualityDetails = "Archivo local • ${format.displayName}",
                            uri = path,
                            source = TrackSource.LOCAL_STORAGE,
                            fileSizeMb = (sizeBytes / (1024f * 1024f))
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Permission or empty media store fallback
        }
        return tracks
    }
}

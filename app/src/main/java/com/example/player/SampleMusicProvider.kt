package com.example.player

import android.content.Context
import android.provider.MediaStore
import com.example.model.AudioFormat
import com.example.model.AudioTrack
import com.example.model.TrackSource

object SampleMusicProvider {

    // Pre-generated chords (frequencies in Hz)
    private val cMajor7Chords = listOf(
        listOf(261.63, 329.63, 392.00, 493.88), // Cmaj7
        listOf(220.00, 261.63, 329.63, 392.00), // Am7
        listOf(174.61, 220.00, 261.63, 329.63), // Fmaj7
        listOf(196.00, 246.94, 293.66, 349.23)  // G7
    )

    private val synthwaveDMinorChords = listOf(
        listOf(146.83, 220.00, 293.66, 349.23), // Dm
        listOf(116.54, 174.61, 233.08, 293.66), // Bb
        listOf(130.81, 196.00, 261.63, 329.63), // C
        listOf(110.00, 164.81, 220.00, 261.63)  // Am
    )

    private val ambientJazzChords = listOf(
        listOf(155.56, 233.08, 311.13, 392.00), // Ebmaj7
        listOf(207.65, 261.63, 311.13, 392.00), // Abmaj7
        listOf(174.61, 207.65, 261.63, 311.13), // Fm7
        listOf(233.08, 293.66, 349.23, 440.00)  // Bb7
    )

    private val studioMasterChords = listOf(
        listOf(196.00, 293.66, 392.00, 440.00, 493.88), // G pentatonic
        listOf(164.81, 246.94, 329.63, 392.00, 493.88), // Em pentatonic
        listOf(130.81, 196.00, 261.63, 329.63, 392.00), // C pentatonic
        listOf(146.83, 220.00, 293.66, 369.99, 440.00)  // D pentatonic
    )

    private val nordicEchoChords = listOf(
        listOf(164.81, 196.00, 246.94, 329.63), // Em
        listOf(174.61, 220.00, 261.63, 349.23), // F
        listOf(130.81, 164.81, 196.00, 261.63), // C
        listOf(123.47, 146.83, 185.00, 246.94)  // Bm
    )

    fun getSampleTracks(context: Context): List<AudioTrack> {
        val file1 = ProceduralAudioGenerator.getOrCreateDemoAudio(
            context = context,
            fileName = "aura_nocturna_flac.wav",
            sampleRate = 48000,
            durationSeconds = 24,
            chords = cMajor7Chords
        )

        val file2 = ProceduralAudioGenerator.getOrCreateDemoAudio(
            context = context,
            fileName = "midnight_city_mp3.wav",
            sampleRate = 44100,
            durationSeconds = 20,
            chords = synthwaveDMinorChords
        )

        val file3 = ProceduralAudioGenerator.getOrCreateDemoAudio(
            context = context,
            fileName = "analog_dreamscape_m4a.wav",
            sampleRate = 48000,
            durationSeconds = 24,
            chords = ambientJazzChords
        )

        val file4 = ProceduralAudioGenerator.getOrCreateDemoAudio(
            context = context,
            fileName = "cosmic_reverberation_wav.wav",
            sampleRate = 48000,
            durationSeconds = 28,
            chords = studioMasterChords
        )

        val file5 = ProceduralAudioGenerator.getOrCreateDemoAudio(
            context = context,
            fileName = "nordic_horizon_ogg.wav",
            sampleRate = 44100,
            durationSeconds = 20,
            chords = nordicEchoChords
        )

        return listOf(
            AudioTrack(
                id = "demo_1_flac",
                title = "Aura Nocturna (Hi-Res Acoustic)",
                artist = "Sonora Studio Ensemble",
                album = "Audiophile Sessions Vol. 1",
                durationMs = 24000L,
                format = AudioFormat.FLAC,
                qualityDetails = "48.0 kHz / 24-bit • Lossless Direct",
                uri = file1.toURI().toString(),
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                source = TrackSource.HI_FI_DEMO,
                fileSizeMb = (file1.length() / (1024f * 1024f))
            ),
            AudioTrack(
                id = "demo_2_mp3",
                title = "Midnight City Lights",
                artist = "Synthetica",
                album = "Neon Drift Horizon",
                durationMs = 20000L,
                format = AudioFormat.MP3,
                qualityDetails = "44.1 kHz / 16-bit • 320 kbps Studio",
                uri = file2.toURI().toString(),
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                source = TrackSource.HI_FI_DEMO,
                fileSizeMb = (file2.length() / (1024f * 1024f))
            ),
            AudioTrack(
                id = "demo_3_m4a",
                title = "Analog Dreamscape (Lossless AAC)",
                artist = "The Velvet Frequency",
                album = "Tape & Tube Memories",
                durationMs = 24000L,
                format = AudioFormat.M4A,
                qualityDetails = "48.0 kHz / 24-bit • Lossless Master",
                uri = file3.toURI().toString(),
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                source = TrackSource.HI_FI_DEMO,
                fileSizeMb = (file3.length() / (1024f * 1024f))
            ),
            AudioTrack(
                id = "demo_4_wav",
                title = "Cosmic Reverberation",
                artist = "Astral Sound Lab",
                album = "Master Tape Direct",
                durationMs = 28000L,
                format = AudioFormat.WAV,
                qualityDetails = "48.0 kHz / 24-bit PCM • Uncompressed",
                uri = file4.toURI().toString(),
                coverUrl = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=600&auto=format&fit=crop&q=80",
                source = TrackSource.HI_FI_DEMO,
                fileSizeMb = (file4.length() / (1024f * 1024f))
            ),
            AudioTrack(
                id = "demo_5_ogg",
                title = "Nordic Horizon Strings",
                artist = "Fjord Symphony",
                album = "Northern Echoes",
                durationMs = 20000L,
                format = AudioFormat.OGG,
                qualityDetails = "44.1 kHz / 16-bit • Vorbis Master",
                uri = file5.toURI().toString(),
                coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80",
                source = TrackSource.HI_FI_DEMO,
                fileSizeMb = (file5.length() / (1024f * 1024f))
            )
        )
    }

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

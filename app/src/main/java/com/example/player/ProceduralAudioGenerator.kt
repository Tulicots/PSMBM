package com.example.player

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object ProceduralAudioGenerator {

    /**
     * Generates a pleasant, melodic 16-bit PCM WAV audio file with stereo ambient chords.
     * Guaranteed 100% offline, zero network errors, zero 403 HTTP codes.
     */
    fun getOrCreateDemoAudio(
        context: Context,
        fileName: String,
        sampleRate: Int = 44100,
        durationSeconds: Int = 20,
        chords: List<List<Double>>
    ): File {
        val file = File(context.cacheDir, fileName)
        if (file.exists() && file.length() > 44) {
            return file
        }

        val numChannels = 2
        val bitsPerSample = 16
        val totalSamples = sampleRate * durationSeconds
        val dataSize = totalSamples * numChannels * (bitsPerSample / 8)

        val buffer = ByteBuffer.allocate(44 + dataSize).apply {
            order(ByteOrder.LITTLE_ENDIAN)

            // 1. RIFF Chunk
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(36 + dataSize)
            put("WAVE".toByteArray(Charsets.US_ASCII))

            // 2. fmt Subchunk
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16) // Subchunk1Size for PCM
            putShort(1) // AudioFormat 1 = PCM
            putShort(numChannels.toShort())
            putInt(sampleRate)
            putInt(sampleRate * numChannels * (bitsPerSample / 8)) // ByteRate
            putShort((numChannels * (bitsPerSample / 8)).toShort()) // BlockAlign
            putShort(bitsPerSample.toShort())

            // 3. data Subchunk
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(dataSize)

            // 4. Generate melodic musical audio
            val chordDuration = durationSeconds.toDouble() / chords.size.coerceAtLeast(1)

            for (sampleIndex in 0 until totalSamples) {
                val time = sampleIndex.toDouble() / sampleRate
                val chordIndex = ((time / chordDuration).toInt()).coerceIn(0, chords.lastIndex)
                val currentChord = chords[chordIndex]
                val timeInChord = time % chordDuration

                // Gentle envelope to avoid clicks
                val attack = (timeInChord / 0.4).coerceAtMost(1.0)
                val release = ((chordDuration - timeInChord) / 0.4).coerceIn(0.0, 1.0)
                val envelope = attack * release

                var sampleLeft = 0.0
                var sampleRight = 0.0

                // Sum sine waves for chord notes
                currentChord.forEachIndexed { noteIndex, freq ->
                    // Harmonics: fundamental + gentle overtone
                    val wave1 = sin(2.0 * PI * freq * time)
                    val wave2 = 0.3 * sin(2.0 * PI * (freq * 2.0) * time)
                    val wave = (wave1 + wave2) / currentChord.size

                    // Stereo panning across the chords
                    val pan = 0.3 + 0.4 * (noteIndex.toDouble() / currentChord.size.coerceAtLeast(1))
                    sampleLeft += wave * (1.0 - pan)
                    sampleRight += wave * pan
                }

                // Warm bass drone under the chords (fundamental / 2)
                val bassFreq = (currentChord.firstOrNull() ?: 261.63) / 2.0
                val bassWave = 0.25 * sin(2.0 * PI * bassFreq * time)

                val finalLeft = ((sampleLeft * envelope * 0.7 + bassWave * 0.3) * 32767.0).toInt().coerceIn(-32768, 32767)
                val finalRight = ((sampleRight * envelope * 0.7 + bassWave * 0.3) * 32767.0).toInt().coerceIn(-32768, 32767)

                putShort(finalLeft.toShort())
                putShort(finalRight.toShort())
            }
        }

        FileOutputStream(file).use { fos ->
            fos.write(buffer.array())
        }

        return file
    }

    fun getOrCreateTrackFile(
        context: Context,
        fileName: String,
        frequencyHz: Double = 440.0,
        durationSeconds: Int = 30,
        title: String = "Track"
    ): File {
        val chords = listOf(
            listOf(frequencyHz, frequencyHz * 1.25, frequencyHz * 1.5),
            listOf(frequencyHz * 0.75, frequencyHz, frequencyHz * 1.25),
            listOf(frequencyHz * 0.89, frequencyHz * 1.12, frequencyHz * 1.33),
            listOf(frequencyHz, frequencyHz * 1.25, frequencyHz * 1.5)
        )
        val cleanName = if (fileName.endsWith(".wav")) fileName else "$fileName.wav"
        return getOrCreateDemoAudio(context, cleanName, 44100, durationSeconds, chords)
    }
}

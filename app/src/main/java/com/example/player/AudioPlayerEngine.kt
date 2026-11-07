package com.example.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.model.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerPlaybackState(
    val currentTrack: AudioTrack? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutesLeft: Int = 0,
    val errorMessage: String? = null
)

class AudioPlayerEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    private var exoPlayer: ExoPlayer? = null

    private val _playbackState = MutableStateFlow(PlayerPlaybackState())
    val playbackState: StateFlow<PlayerPlaybackState> = _playbackState.asStateFlow()

    private var playlist: List<AudioTrack> = emptyList()
    private var currentIndex: Int = -1

    init {
        initializePlayer()
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
                        if (isPlaying) {
                            startProgressUpdates()
                        } else {
                            stopProgressUpdates()
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                val duration = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
                                _playbackState.value = _playbackState.value.copy(
                                    durationMs = duration,
                                    errorMessage = null
                                )
                            }
                            Player.STATE_ENDED -> {
                                playNext()
                            }
                            Player.STATE_BUFFERING -> {
                                updateBufferedPosition()
                            }
                            Player.STATE_IDLE -> {}
                        }
                    }
                })
            }
    }

    fun setPlaylist(tracks: List<AudioTrack>, startIndex: Int = 0, autoPlay: Boolean = true) {
        playlist = tracks
        if (tracks.isEmpty()) return
        currentIndex = startIndex.coerceIn(0, tracks.lastIndex)
        val track = tracks[currentIndex]
        loadTrack(track, autoPlay)
    }

    fun playTrack(track: AudioTrack) {
        val foundIndex = playlist.indexOfFirst { it.id == track.id }
        if (foundIndex != -1) {
            currentIndex = foundIndex
        } else {
            playlist = listOf(track) + playlist
            currentIndex = 0
        }
        loadTrack(track, autoPlay = true)
    }

    private fun loadTrack(track: AudioTrack, autoPlay: Boolean) {
        val player = exoPlayer ?: return
        try {
            val mediaItem = MediaItem.fromUri(track.uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            if (autoPlay) {
                player.play()
            }
            _playbackState.value = _playbackState.value.copy(
                currentTrack = track,
                currentPositionMs = 0L,
                durationMs = track.durationMs,
                errorMessage = null
            )
        } catch (e: Exception) {
            _playbackState.value = _playbackState.value.copy(
                errorMessage = "Error al cargar pista: ${e.message}"
            )
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            if (_playbackState.value.currentTrack == null && playlist.isNotEmpty()) {
                setPlaylist(playlist, 0, autoPlay = true)
            } else {
                player.play()
            }
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun playNext() {
        if (playlist.isEmpty()) return
        currentIndex = if (_playbackState.value.isShuffleEnabled) {
            (playlist.indices).random()
        } else {
            (currentIndex + 1) % playlist.size
        }
        loadTrack(playlist[currentIndex], autoPlay = true)
    }

    fun playPrevious() {
        if (playlist.isEmpty()) return
        val currentPos = exoPlayer?.currentPosition ?: 0L
        if (currentPos > 3000L) {
            seekTo(0L)
            return
        }
        currentIndex = if (currentIndex - 1 < 0) playlist.lastIndex else currentIndex - 1
        loadTrack(playlist[currentIndex], autoPlay = true)
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
    }

    fun seekForward(deltaMs: Long = 10000L) {
        val current = exoPlayer?.currentPosition ?: 0L
        val duration = exoPlayer?.duration ?: 0L
        val target = (current + deltaMs).coerceAtMost(duration)
        seekTo(target)
    }

    fun seekRewind(deltaMs: Long = 10000L) {
        val current = exoPlayer?.currentPosition ?: 0L
        val target = (current - deltaMs).coerceAtLeast(0L)
        seekTo(target)
    }

    fun toggleShuffle() {
        val newShuffle = !_playbackState.value.isShuffleEnabled
        _playbackState.value = _playbackState.value.copy(isShuffleEnabled = newShuffle)
    }

    fun toggleRepeat() {
        val nextMode = when (_playbackState.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer?.repeatMode = nextMode
        _playbackState.value = _playbackState.value.copy(repeatMode = nextMode)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = 0)
            return
        }
        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = minutes)
        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (isActive && remaining > 0) {
                delay(60000L)
                remaining -= 1
                _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = remaining)
            }
            pause()
            _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = 0)
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    _playbackState.value = _playbackState.value.copy(
                        currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = player.duration.coerceAtLeast(0L),
                        bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L)
                    )
                }
                delay(500L)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
    }

    private fun updateBufferedPosition() {
        exoPlayer?.let {
            _playbackState.value = _playbackState.value.copy(
                bufferedPositionMs = it.bufferedPosition.coerceAtLeast(0L)
            )
        }
    }

    fun release() {
        progressJob?.cancel()
        sleepTimerJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
}

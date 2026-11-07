package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.player.AudioPlayerEngine
import com.example.player.SampleMusicProvider
import com.example.ui.MainTvApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var playerEngine: AudioPlayerEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on while playing and optimize window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        playerEngine = AudioPlayerEngine(this)

        // Preload default sample tracks into playlist
        playerEngine.setPlaylist(SampleMusicProvider.getSampleTracks(this), startIndex = 0, autoPlay = false)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainTvApp(
                    playerEngine = playerEngine,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Intercept standard TV remote media controls
        return when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                playerEngine.play()
                true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                playerEngine.pause()
                true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                playerEngine.togglePlayPause()
                true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                playerEngine.playNext()
                true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                playerEngine.playPrevious()
                true
            }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                playerEngine.seekForward()
                true
            }
            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                playerEngine.seekRewind()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerEngine.release()
    }
}

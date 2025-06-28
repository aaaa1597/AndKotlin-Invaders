package com.aaa.andkotlininvaders

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class BackgroundMusicManager(private val context: Context): DefaultLifecycleObserver {
    private var mediaPlayer: MediaPlayer? = null

    fun startPlaying() {
        if(mediaPlayer?.isPlaying != true)
            mediaPlayer?.start()
    }

    fun stopPlaying() {
        mediaPlayer?.stop()
        mediaPlayer?.prepare()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        mediaPlayer = MediaPlayer.create(context, R.raw.maou_bgm_8bit17).apply {
            isLooping = true
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        mediaPlayer?.start()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        mediaPlayer?.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        mediaPlayer?.release()
    }
}

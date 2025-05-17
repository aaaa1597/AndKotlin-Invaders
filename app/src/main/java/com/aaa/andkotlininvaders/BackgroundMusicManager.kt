package com.aaa.andkotlininvaders

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class BackgroundMusicManager(private val context: Context) : DefaultLifecycleObserver {
    private var mediaPlayer: MediaPlayer? = null

    fun startPlaying() {
        if(mediaPlayer?.isPlaying != true) {
            mediaPlayer?.start()
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        mediaPlayer = MediaPlayer.create(context, R.raw.maou_bgm_8bit17).apply {
            isLooping = true
        }
        Throwable().stackTrace[0].let {
            Log.d("aaaaa", "aaaaa ${mediaPlayer}  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        mediaPlayer?.start()
        Throwable().stackTrace[0].let {
            Log.d("aaaaa", "aaaaa ${mediaPlayer}  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        mediaPlayer?.pause()
        Throwable().stackTrace[0].let {
            Log.d("aaaaa", "aaaaa ${mediaPlayer}  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        mediaPlayer?.release()
        Throwable().stackTrace[0].let {
            Log.d("aaaaa", "aaaaa ${mediaPlayer}  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
        }
    }
}

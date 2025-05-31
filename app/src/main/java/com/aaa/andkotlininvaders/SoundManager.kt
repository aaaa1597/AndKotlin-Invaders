package com.aaa.andkotlininvaders

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.SoundPool
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class SoundManager(context: Context) : DefaultLifecycleObserver {
    private var soundPool: SoundPool = SoundPool.Builder()
                                .setMaxStreams(2)
                                .setAudioAttributes(AudioAttributes.Builder()
                                                                   .setContentType(CONTENT_TYPE_MUSIC)
                                                                   .build())
                                .build()
    var soundId: Int = 0
    var streamId: Int = 0
    init {
        Log.d("aaaaa", "aaaaa SoundManager::init()")
        soundId = soundPool.load(context, R.raw.player_bullet_sound, 1) ?: 0
    }

    fun play() {
        streamId = soundPool.play(soundId, 1F, 1F, 1, 0, 1f) ?: 0
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.d("aaaaa", "aaaaa SoundManager::onCreate()")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d("aaaaa", "aaaaa SoundManager::onStart()")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Log.d("aaaaa", "aaaaa SoundManager::onResume()")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        soundPool.stop(streamId)
        Log.d("aaaaa", "aaaaa SoundManager::onPause()")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d("aaaaa", "aaaaa SoundManager::onStop()")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        soundPool.release()
        Log.d("aaaaa", "aaaaa SoundManager::onDestroy()")
    }
}
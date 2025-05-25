package com.aaa.andkotlininvaders

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.SoundPool
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class BulletMusicManager(private val context: Context): DefaultLifecycleObserver {
    var soundId: Int = 0
    var streamId: Int = 0
    private var soundPool: SoundPool? = null
    fun init() {
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder().setContentType(CONTENT_TYPE_MUSIC)
                .build())
            .build()
        soundId = soundPool?.load(context, R.raw.player_bullet_sound, 1) ?: 0
    }

    fun play() {
        streamId = soundPool?.play(soundId, 1F, 1F, 1, 0, 1f) ?: 0
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        soundPool?.stop(streamId)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        soundPool?.release()
        soundPool = null
    }
}
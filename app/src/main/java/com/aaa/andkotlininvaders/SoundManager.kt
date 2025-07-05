package com.aaa.andkotlininvaders

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.SoundPool
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class SoundManager(context: Context) : DefaultLifecycleObserver {
    private var soundPool: SoundPool = SoundPool.Builder()
                                .setMaxStreams(2)
                                .setAudioAttributes(AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(CONTENT_TYPE_MUSIC)
                                    .build())
                                .build()
    var soundId: Int = 0
    var streamId: Int = 0
    init {
        soundId = soundPool.load(context, R.raw.player_bullet_sound, 1)
    }

    fun play() {
        streamId = soundPool.play(soundId, 1F, 1F, 1, 0, 1f)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        soundPool.stop(streamId)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        soundPool.release()
    }
}
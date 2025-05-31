package com.aaa.andkotlininvaders

import android.content.Context
import android.util.AttributeSet
import android.view.View

class BulletView: View {
    /* Viewを継承するときのお約束 */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

//    private var bulletStateList = mutableListOf<Bullet>()

    private lateinit var fireSound: SoundManager
    fun setSoundManager(soundManager: SoundManager) {
        fireSound = soundManager
    }

}
package com.aaa.andkotlininvaders

import android.content.Context
import android.util.AttributeSet
import android.util.Range
import android.view.View

class EnemyClusterView: View {
    /* Viewを継承するときのお約束 */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


}

data class EnemyColumn(val range: Range<Float>, val enemyList: List<Enemy> = listOf()) {
    fun areAnyVisible(): Boolean {
        return enemyList.any { it.isVisible }
    }
}

class Enemy {
    var isVisible: Boolean = true
}
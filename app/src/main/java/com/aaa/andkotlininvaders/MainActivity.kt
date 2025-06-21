package com.aaa.andkotlininvaders

import android.graphics.Rect
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.aaa.andkotlininvaders.databinding.ActivityMainBinding
import kotlinx.coroutines.Job

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val mainActivityViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[MainActivityViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /* ジェスチャバックを無効化 */
        binding.root.systemGestureExclusionRects = ArrayList<Rect>().apply {
            add(Rect(0, android.R.attr.height - 650, android.R.attr.width, android.R.attr.height - 450))
            add(Rect(0, android.R.attr.height - 450, android.R.attr.width, android.R.attr.height - 250))
            add(Rect(0, android.R.attr.height - 250, android.R.attr.width, android.R.attr.height - 50))
        }

        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
    }
}
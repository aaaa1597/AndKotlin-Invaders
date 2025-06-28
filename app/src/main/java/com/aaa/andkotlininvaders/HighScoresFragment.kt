package com.aaa.andkotlininvaders

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aaa.andkotlininvaders.databinding.FragmentHighScoresBinding
import com.aaa.andkotlininvaders.databinding.FragmentMainMenuBinding

class HighScoresFragment : Fragment() {
    private lateinit var _binding: FragmentHighScoresBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHighScoresBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val score = DataStore.getHighScore(requireContext())
        val level = DataStore.getMaxLevels(requireContext())
        _binding.txtScoreboard.text = getString(R.string.level, score, level)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HighScoresFragment().apply {
            }
    }
}

object DataStore {
    private const val HIGH_SCORE = "HIGH_SCORE"
    private const val MAX_LEVEL = "MAX_LEVEL"
    fun setHighScore(context: Context, score: Int, level: Int) {
        val maxLevel = getMaxLevels(context)
        if(level < maxLevel) return

        val highScore = getHighScore(context)
        if(level==maxLevel && highScore < score) {
            val pref = context.getSharedPreferences("Data", MODE_PRIVATE).edit()
            pref.putInt(HIGH_SCORE, score)
            pref.apply()
        }
        else if(level > maxLevel) {
            val pref = context.getSharedPreferences("Data", MODE_PRIVATE).edit()
            pref.putInt(MAX_LEVEL, level)
            pref.putInt(HIGH_SCORE, score)
            pref.apply()
        }
    }

    fun getHighScore(context: Context): Int {
        return context.getSharedPreferences("Data", MODE_PRIVATE).getInt(HIGH_SCORE, 0)
    }

    fun getMaxLevels(context: Context): Int {
        return context.getSharedPreferences("Data", MODE_PRIVATE).getInt(MAX_LEVEL, 0)
    }
}

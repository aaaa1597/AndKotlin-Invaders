package com.aaa.andkotlininvaders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aaa.andkotlininvaders.databinding.FragmentGameClearedBinding
import com.aaa.andkotlininvaders.databinding.FragmentGameSceneBinding

class GameClearedFragment : Fragment() {
    private lateinit var _binding: FragmentGameClearedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentGameClearedBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MainActivityViewModel.LevelInfo.level++
        val level = MainActivityViewModel.LevelInfo.level
        _binding.txtCleared.text = resources.getText(R.string.level_complete, level.toString())
    }
}

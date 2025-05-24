package com.aaa.andkotlininvaders

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aaa.andkotlininvaders.databinding.FragmentGameSceneBinding

class GameSceneFragment : Fragment() {
    private lateinit var _binding: FragmentGameSceneBinding
    private val viewModel: GameSceneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding = FragmentGameSceneBinding.inflate(inflater, container, false)
        return _binding.root
    }

    companion object {
        fun newInstance() = GameSceneFragment()
    }
}
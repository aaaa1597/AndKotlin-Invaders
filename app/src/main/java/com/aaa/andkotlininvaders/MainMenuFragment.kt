package com.aaa.andkotlininvaders

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.aaa.andkotlininvaders.databinding.FragmentMainMenuBinding

class MainMenuFragment : Fragment() {
    private lateinit var _binding: FragmentMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* ゲーム開始 */
        _binding.btnStart.setOnClickListener {

        }

        /* スコア画面 */
        _binding.btnViewScores.setOnClickListener {
            findNavController().navigate(R.id.action_to_highScores_zoom)
        }

        /* 終了ボタン */
        _binding.btnExit.setOnClickListener {
            requireActivity().finish()
        }

        _binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            _binding.btnStart.apply {
                postDelayed({animate().translationY(y).setDuration(300)}, 100)
                y = 0f
            }
            _binding.btnViewScores.apply {
                postDelayed({animate().translationY(y).setDuration(300)}, 200)
                y = 0f
            }
            _binding.btnExit.apply {
                postDelayed({animate().translationY(y).setDuration(300)}, 300)
                y = 0f
            }
        }
    }
}
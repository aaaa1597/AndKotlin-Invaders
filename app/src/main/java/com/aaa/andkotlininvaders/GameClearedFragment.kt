package com.aaa.andkotlininvaders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.aaa.andkotlininvaders.databinding.FragmentGameClearedBinding

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

        /* レベル数文字列設定 */
        MainActivityViewModel.LevelInfo.level
        val level = MainActivityViewModel.LevelInfo.level+1
        _binding.txtCleared.text = resources.getString(R.string.level_complete, level)

        /* 次レベル開始 */
        _binding.btnRestart2.setOnClickListener {
            MainActivityViewModel.LevelInfo.increment()
            val bundle = bundleOf("LEVEL" to MainActivityViewModel.LevelInfo.level+1)
            findNavController().navigate(R.id.action_to_countdown_zoom2, bundle)
        }

        /* スコア画面 */
        _binding.btnViewScores2.setOnClickListener {
            findNavController().navigate(R.id.action_to_highScores_zoom2)
        }

        /* 終了ボタン */
        _binding.btnExit2.setOnClickListener {
            requireActivity().finish()
        }
    }
}

package com.aaa.andkotlininvaders

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.aaa.andkotlininvaders.databinding.FragmentCountdownBinding
import com.aaa.andkotlininvaders.databinding.FragmentMainMenuBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CountdownFragment : Fragment() {
    private var _level: Int = 0
    private lateinit var _binding: FragmentCountdownBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            _level = it.getInt("LEVEL",0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentCountdownBinding.inflate(inflater, container, false)
        _binding.txtCountdownTitle.text = getString(R.string.title_countdown, _level)
        return _binding.root
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch{
            /* カウントダウン3秒実行 */
            (3 downTo 1).forEach {
                _binding.txtCountdown.text = it.toString()
                delay(1000)
            }
            /* 完了後、ゲーム画面に遷移 */
            findNavController().navigate(R.id.action_to_gameScene_zoom)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(level: Int) =
            CountdownFragment().apply {
                arguments = Bundle().apply {
                    putInt("LEVEL", level)
                }
            }
    }
}
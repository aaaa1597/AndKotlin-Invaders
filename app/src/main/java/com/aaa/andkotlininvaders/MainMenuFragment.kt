package com.aaa.andkotlininvaders

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
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
            val bundle = bundleOf("LEVEL" to 1)
            findNavController().navigate(R.id.action_to_countdown_zoom, bundle)
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
            if(lifecycle.currentState != Lifecycle.State.RESUMED)
                return@addOnGlobalLayoutListener

            Log.d("aaaaa", "aaaaa R.id.main(left,top,right,bottom)=(${_binding.root.left},${_binding.root.top},${_binding.root.right},${_binding.root.bottom})")
            Log.d("aaaaa", "aaaaa R.id.main(x,y,translationX,translationY)=(${_binding.root.x},${_binding.root.y},${_binding.root.translationX},${_binding.root.translationY})")
            Log.d("aaaaa", "aaaaa R.id.main(left+translationX,top+translationY)=(${_binding.root.left+_binding.root.translationX},${_binding.root.top+_binding.root.translationY}})")

            _binding.logoView.apply {
                translationY = -(top+height.toFloat()+20)
                postDelayed({animate().translationY(0f).setDuration(300)}, 600)
            }
            _binding.btnStart.apply {
                translationY = -(top+height.toFloat())
                postDelayed({animate().translationY(0f).setDuration(300)}, 300)
            }
            _binding.btnViewScores.apply {
                translationY = -(top+height.toFloat())
                postDelayed({animate().translationY(0f).setDuration(300)}, 500)
            }
            _binding.btnExit.apply {
                translationY = -(top+height.toFloat())
                postDelayed({animate().translationY(0f).setDuration(300)}, 700)
            }
        }
    }
}
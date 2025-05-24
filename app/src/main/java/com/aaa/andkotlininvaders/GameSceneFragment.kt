package com.aaa.andkotlininvaders

import android.graphics.Color
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aaa.andkotlininvaders.databinding.FragmentGameSceneBinding
import kotlinx.coroutines.launch

class GameSceneFragment : Fragment() {
    private lateinit var _binding: FragmentGameSceneBinding
    private val viewModel: GameSceneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.init()

        /* 得点の監視処理 */
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.scoreFlow.collect{
                _binding.txtScore.text = it.toString()
            }
        }}

        /* 弾薬数の監視処理 */
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.bulletCountFlow.collect {
                /* 更新前保持 */
                val preval: Int = _binding.txtBulletcounter.text.toString().toIntOrNull() ?: 0
                /* 弾薬数更新 */
                _binding.txtBulletcounter.text = it.toString()
                /* 弾薬数が25%以下になるなら赤字に */
                if(viewModel.isRemainRed())
                    _binding.txtBulletcounter.setTextColor(Color.RED)
                else
                    _binding.txtBulletcounter.setTextColor(Color.WHITE)
                /* 敵から弾薬ゲットしたら弾薬数増(嬉!!)のアニメ開始 */
                if(preval<it) {
                    _binding.txtBulletcounter.animate()
                        .scaleXBy(0.1f).scaleYBy(0.1f).setDuration(100)
                        .setInterpolator(LinearInterpolator())
                        .withEndAction{
                            _binding.txtBulletcounter.postDelayed({
                                /* 縮小→元のサイズに戻すアニメを実行 */
                                _binding.txtBulletcounter.animate().cancel()
                                _binding.txtBulletcounter.animate()
                                    .scaleX(1f).scaleY(1f).setDuration(500)
                                    .setInterpolator(OvershootInterpolator())
                                    .start()
                            },200)
                        }
                        .start()
                }
            }
        }}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding = FragmentGameSceneBinding.inflate(inflater, container, false)
        return _binding.root
    }

    companion object {
        fun newInstance() = GameSceneFragment()
    }
}
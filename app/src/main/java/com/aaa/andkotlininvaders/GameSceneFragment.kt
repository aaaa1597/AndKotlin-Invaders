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
import androidx.navigation.fragment.findNavController
import com.aaa.andkotlininvaders.EnemyClusterView.EnemyClusterViewEventCallback
import com.aaa.andkotlininvaders.databinding.FragmentGameSceneBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GameSceneFragment : Fragment() {
    private lateinit var _binding: FragmentGameSceneBinding
    private val viewModel: GameSceneViewModel by viewModels()
    private var scoreFlowJob: Job = Job()
    private var remainFlowJob: Job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.init()

        /* 得点の監視処理 */
        scoreFlowJob = lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.scoreFlow.collect{
                _binding.txtScore.text = it.toString()
            }
        }}

        /* 弾薬数の監視処理 */
        remainFlowJob = lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.RESUMED) {
            GameSceneViewModel.BulletRemain.remainFlow.collect {
                /* 更新前保持 */
                val preval: Int = _binding.txtBulletcounter.text.toString().toIntOrNull() ?: 0
                /* 弾薬数更新 */
                _binding.txtBulletcounter.text = it.toString()
                /* 弾薬数が25%以下になるなら赤字に */
                if(GameSceneViewModel.BulletRemain.isRemainRed())
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val soundManager = SoundManager(requireContext())
        lifecycle.addObserver(soundManager)
        _binding.viwSpaceShipView.fireSound   = soundManager
        _binding.viwEnemyclusterview.fireSound= soundManager
        _binding.viwEnemyclusterview.setOnEnemyClusterViewEventCallback(object: EnemyClusterViewEventCallback {
            override fun onEnemyBreached() {
                /* ゲームオーバー画面に遷移 */
                findNavController().navigate(R.id.action_to_youdeid_zoom)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        scoreFlowJob.cancel()
        remainFlowJob.cancel()
    }

    companion object {
        fun newInstance() = GameSceneFragment()
    }
}
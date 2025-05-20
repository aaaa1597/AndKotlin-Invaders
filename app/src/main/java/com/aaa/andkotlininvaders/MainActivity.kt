package com.aaa.andkotlininvaders

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
    val viewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[MainViewModel::class.java]
    }
//    lateinit var levelCompleteScene: SceneContainer<LevelCompleteSceneBinding>
//    lateinit var levelZeroGameScene: SceneContainer<LevelZeroGameBinding>
//    lateinit var levelStartWarpScene: SceneContainer<LevelStartWarpSceneBinding>
//    lateinit var gameMenuScene: SceneContainer<MainMenuSceneBinding>
//    lateinit var youDiedScene: SceneContainer<YouDiedSceneBinding>
//    lateinit var gameScene: SceneContainer<GameSceneBinding>
//    lateinit var levelStartScene: SceneContainer<LevelStartSceneBinding>
//    lateinit var gameOverScene: SceneContainer<GameOverSceneBinding>
//    lateinit var highScoreScene: SceneContainer<HighscoresSceneBinding>

    private val backgroundMusicManager by lazy {
        BackgroundMusicManager(applicationContext).apply {
            lifecycle.addObserver(this)
        }
    }

    private val transitionManager by lazy {
        TransitionManager()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        goFullScreen()
//        initDataStore(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })

        initScenes()
//        observeScreenStates()
    }

    private fun initScenes() {
//        gameMenuScene =
//            MainMenuSceneBinding.inflate(layoutInflater, binding.root, false).let {
//                SceneContainer(it, Scene(binding.rootContainer, it.root))
//            }
//
//        levelStartScene =
//            LevelStartSceneBinding.inflate(layoutInflater, binding.root, false).let {
//                SceneContainer(it, Scene(binding.rootContainer, it.root))
//            }
//
//        youDiedScene =
//            YouDiedSceneBinding.inflate(layoutInflater, binding.root, false).let {
//                SceneContainer(it, Scene(binding.rootContainer, it.root))
//            }
//
//        highScoreScene =
//            HighscoresSceneBinding.inflate(layoutInflater, binding.root, false).let {
//                SceneContainer(it, Scene(binding.rootContainer, it.root))
//            }
//
//        resetGameScene()
//
//        gameOverScene =
//            GameOverSceneBinding.inflate(layoutInflater, binding.root, false).let {
//                SceneContainer(it, Scene(binding.rootContainer, it.root))
//            }
    }

//    override fun initBulletTracking(softBodyObjectData: SoftBodyObjectData) {
//        if (softBodyObjectData.sender == BulletView.Sender.PLAYER) {
//            gameScene.binding.enemiesView.checkCollision(softBodyObjectData)
//        } else {
//            gameScene.binding.spaceShipView.checkCollision(softBodyObjectData)
//        }
//    }

//    override fun cancelTracking(bulletId: UUID, sender: BulletView.Sender) {
//        if (sender == BulletView.Sender.PLAYER) {
//            gameScene.binding.spaceShipView.removeSoftBodyEntry(bulletId)
//        } else {
//            gameScene.binding.enemiesView.removeSoftBodyEntry(bulletId)
//        }
//    }
//
//    override fun onCollision(softBodyObject: SoftBodyObjectData) {
//        if (softBodyObject.objectType == SoftBodyObjectType.BULLET) {
//            gameScene.binding.bulletView.destroyBullet(softBodyObject.objectId)
//        } else {
//            gameScene.binding.dropsView.destroyObject(softBodyObject.objectId)
//        }
//    }
//
//    override fun onAllEliminated(ammoCount: Int) {
//        showLevelCompleteScene(ammoCount)
//    }

//    override fun onCanonReady(enemyX: Float, enemyY: Float) {
//        gameScene.binding.bulletView.fire(enemyX, enemyY, BulletView.Sender.ENEMY)
//    }
//
//    override fun hasDrop(enemyX: Float, enemyY: Float) {
//        gameScene.binding.dropsView.dropGift(enemyX, enemyY)
//    }

    fun resetGameScene() {
//        binding.rootContainer.removeAllViews()
//        levelCompleteScene =
//            LevelCompleteSceneBinding.inflate(layoutInflater, binding.root, false).let {
//                SceneContainer(it, Scene(binding.rootContainer, it.root))
//            }
//
//        levelZeroGameScene =
//            LevelZeroGameBinding.inflate(layoutInflater, binding.root, false).let {
//                SceneContainer(it, Scene(binding.rootContainer, it.root))
//            }
//
//        levelStartWarpScene =
//            LevelStartWarpSceneBinding.inflate(layoutInflater, binding.root, false).let {
//                SceneContainer(it, Scene(binding.rootContainer, it.root))
//            }
//
//        gameScene = GameSceneBinding.inflate(layoutInflater, binding.root, false).let {
//            SceneContainer(it, Scene(binding.rootContainer, it.root))
//        }.apply {
//            binding.apply {
//                bulletView.softBodyObjectTracker = this@MainActivity
//                dropsView.softBodyObjectTracker = this@MainActivity
//                healthView.onHealthEmpty = {
//                    viewModel.updateUIState(ScreenStates.YouDied)
//                }
//                enemiesView.enemyDetailsCallback = this@MainActivity
//                enemiesView.onCollisionCallBack = this@MainActivity
//                spaceShipView.onCollisionCallBack = this@MainActivity
//            }
//        }
    }

//    override fun onGameOver() {
//        viewModel.updateUIState(ScreenStates.YouDied)
//    }

    fun transitionFromTo(fromScene: Scene, toScene: Scene, transition: Transition) {
        transitionManager.setTransition(fromScene, toScene, transition)
        transitionManager.transitionTo(toScene)
    }

    fun transitionTo(toScene: Scene, transition: Transition) {
        transitionManager.setTransition(toScene, transition)
        transitionManager.transitionTo(toScene)
    }

    internal var uiEventJob: Job = Job()

    override fun onDestroy() {
        super.onDestroy()
        uiEventJob.cancel()
    }

//    private fun observeScreenStates() {
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.observeScreenState().collect {
//                    uiEventJob.cancel()
//                    when (it) {
//                        ScreenStates.AppInit -> {
//                            backgroundMusicManager.startPlaying()
//                        }
//
//                        else -> {
//
//                        }
//                    }
//                }
//            }
//        }
//    }
}
package com.example.videoplayer.UI

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.content.res.Resources
import android.media.AudioManager
import android.media.VolumeShaper.Configuration
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.Window.FEATURE_NO_TITLE
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.navArgument
import com.example.videoplayer.Model.VideoData
import com.example.videoplayer.R
import com.example.videoplayer.databinding.ActivityPlayerBinding
import com.example.videoplayer.util.DoubleClickListener
import com.github.vkay94.dtpv.youtube.YouTubeOverlay
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline.Window
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashChunkSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.common.net.HttpHeaders.USER_AGENT
import com.google.rpc.context.AttributeContext.Resource
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.list_view.*
import java.lang.Math.abs

private const val TAG = "PlayerActivity"

class PlayerActivity : AppCompatActivity(), AudioManager.OnAudioFocusChangeListener,
    GestureDetector.OnGestureListener {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var videoList: ArrayList<VideoData>
    private lateinit var gestureDetectorCompat: GestureDetectorCompat

    companion object {
        private lateinit var exoPlayer: ExoPlayer
        private var playbackPosition = 0L
        private var playWhenReady = true
        private lateinit var runnable: Runnable
        private var pos: Int = -1
        private var isLocked: Boolean = false
        private var audioManager: AudioManager? = null
        private var brigthness: Int = 0
        private var volume:Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setTheme(R.style.PlayerActivity)
        setContentView(binding.root)
        // for immersive mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // getting the data from recycler view
        val bundle: Bundle? = intent.extras
        pos = bundle?.getInt("position")!!

        if (bundle != null) {
            videoList = bundle.getParcelableArrayList("list")!!
        }
        gestureDetectorCompat = GestureDetectorCompat(this, this)
        preparePlayer()
        initializeBinding()


    }

    private fun preparePlayer() {
        try {
            exoPlayer.release()
        } catch (_: java.lang.Exception) {
        }
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer?.let { exo ->
            exo.playWhenReady = true
            doubleTapEnable()
            exo.setMediaSource(setMediaType())
            exo.playWhenReady = playWhenReady
            exo.prepare()
            playVideo()
        }
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) nextVideo()
            }
        })
        setVisibility()
    }

    private fun nextVideo() {
        pos = (pos + 1) % videoList.size
        preparePlayer()
    }

    private fun initializeBinding() {
        binding.backBtn.setOnClickListener {
            if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
                // change the orientation of the player
                playInFullScreen(false)
            } else {
                finish()
            }

        }
        binding.playPauseBtn.setOnClickListener {
            if (exoPlayer.isPlaying) pauseVideo()
            else playVideo()
        }
        binding.fullScreenbtn.setOnClickListener {
            if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
                playInFullScreen(true)
            } else {
                playInFullScreen(false)
            }
        }
        binding.lockBtn.setOnClickListener {

            if (!isLocked) {
                // hiding
                isLocked = true
                binding.playerView.hideController()
                binding.playerView.useController = false
                binding.lockBtn.setImageResource(R.drawable.close_lock)
            } else {
                //showing
                isLocked = false
                binding.playerView.useController = true
                binding.playerView.showController()
                binding.lockBtn.setImageResource(R.drawable.lock_open_icon)
            }
        }
    }

    private fun playVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.pause_icon)
        exoPlayer.play()
    }

    private fun pauseVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.play_icon)
        exoPlayer.pause()
    }


    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
    }

    private fun releasePlayer() {
        exoPlayer?.let { player ->
            playbackPosition = player.currentPosition
            playWhenReady = player.playWhenReady
            player.release()
        }
    }

    private fun playInFullScreen(enable: Boolean) {
        if (enable) {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.fullScreenbtn.setImageResource(R.drawable.fullscreen_exit)
        } else {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.fullScreenbtn.setImageResource(R.drawable.fullscreen_icon)
        }
        requestedOrientation = if (enable) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun setMediaType(): MediaSource {
        binding.videoTitle.text = videoList.get(pos).songName.toString()
        binding.videoTitle.isSelected = true
        val url: String = videoList.get(pos).songUrl.toString()
        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
        val mediaItem = MediaItem.fromUri(url)
        return ProgressiveMediaSource.Factory(defaultHttpDataSourceFactory)
            .createMediaSource(mediaItem)
    }

    private fun setVisibility() {
        runnable = Runnable {
            if (binding.playerView.isControllerVisible) changeVisibility(View.VISIBLE)
            else changeVisibility(View.INVISIBLE)
            Handler(Looper.getMainLooper()).postDelayed(runnable, 300)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    private fun changeVisibility(visibility: Int) {
        binding.topController.visibility = visibility
        binding.bottomController.visibility = visibility
        binding.playPauseBtn.visibility = visibility

        if (isLocked) binding.lockBtn.visibility = View.VISIBLE
        else binding.lockBtn.visibility = visibility


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun doubleTapEnable() {
        binding.playerView.player = exoPlayer
        binding.ytOverlay.performListener(object : YouTubeOverlay.PerformListener {
            override fun onAnimationEnd() {
                binding.ytOverlay.visibility = View.GONE
            }

            override fun onAnimationStart() {
                binding.ytOverlay.visibility = View.VISIBLE
            }
        })
        binding.ytOverlay.player(exoPlayer)
        binding.playerView.setOnTouchListener { v, event ->
            binding.playerView.isDoubleTapEnabled = false
            if(!isLocked){
                binding.playerView.isDoubleTapEnabled = true
                gestureDetectorCompat.onTouchEvent(event)
                if (event.action == MotionEvent.ACTION_UP){
                    binding.brigthnessIcon.visibility = View.GONE
                    binding.volumeIcon.visibility = View.GONE
                    // for immersive mode
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    WindowInsetsControllerCompat(window, binding.root).let { controller ->
                        controller.hide(WindowInsetsCompat.Type.systemBars())
                        controller.systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
            }

            return@setOnTouchListener false
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        audioManager?.abandonAudioFocus(this)
        releasePlayer()

    }

    override fun onAudioFocusChange(focusChange: Int) {
        TODO("Not yet implemented")
    }

    override fun onDown(e: MotionEvent): Boolean = false

    override fun onShowPress(e: MotionEvent) = Unit

    override fun onSingleTapUp(e: MotionEvent): Boolean = false

    override fun onLongPress(e: MotionEvent) = Unit

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean = false

    override fun onScroll(
        event: MotionEvent,
        event1: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        val sWidth = Resources.getSystem().displayMetrics.widthPixels
        val sHeight = Resources.getSystem().displayMetrics.heightPixels

        val border = 100 * Resources.getSystem().displayMetrics.density.toInt()
        if(event!!.x < border || event.y < border || event.x > sWidth - border || event.y > sHeight - border)
            return false

        if (abs(distanceX) < abs(distanceY)) {
            if (event!!.x < sWidth / 2) {
                //brigthness
                binding.brigthnessIcon.visibility = View.VISIBLE
                binding.volumeIcon.visibility = View.GONE
                val increase = distanceY > 0
                val newValue = if (increase) brigthness + 1 else brigthness - 1
                if (newValue in 0..30) brigthness = newValue
                binding.brigthnessIcon.text = brigthness.toString()
                setScreenBrightness(brigthness)
            } else {
                //volume
                binding.brigthnessIcon.visibility = View.GONE
                binding.volumeIcon.visibility = View.VISIBLE
                val maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val increase = distanceY > 0
                val newValue = if (increase) volume + 1 else volume - 1
                if (newValue in 0..maxVolume) volume = newValue
                binding.volumeIcon.text = volume.toString()
                audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volume,0)
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        if(audioManager == null) audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (brigthness != 0) setScreenBrightness(brigthness)
    }

    private fun setScreenBrightness(value: Int) {
        val d = 1.0f / 30
        val lp = this.window.attributes
        lp.screenBrightness = d * value
        this.window.attributes = lp
    }

}
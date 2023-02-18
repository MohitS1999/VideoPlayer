package com.example.videoplayer.UI

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.navArgument
import com.example.videoplayer.Model.VideoData
import com.example.videoplayer.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashChunkSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.common.net.HttpHeaders.USER_AGENT
import kotlinx.android.synthetic.main.activity_player.*

private const val TAG = "PlayerActivity"
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var videoList: ArrayList<VideoData>
    private lateinit var exoPlayer: ExoPlayer
    private var playbackPosition = 0L
    private var playWhenReady = true

    private var pos:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle: Bundle? = intent.extras
        pos = bundle?.getInt("position")!!

        if (bundle != null) {
            videoList = bundle.getParcelableArrayList("list")!!
        }
        preparePlayer()



    }

    private fun preparePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer?.let { exo ->
            exo.playWhenReady = true
            binding.playerView.player = exo
            exo.setMediaSource(setMediaType())
            exo.seekTo(playbackPosition)
            exo.playWhenReady = playWhenReady
            exo.prepare()
        }
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

    private fun setMediaType(): MediaSource {
        binding.videoTitle.text = videoList.get(pos).songName.toString()
        binding.videoTitle.isSelected = true
        val url:String = videoList.get(pos).songUrl.toString()
        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
        val mediaItem = MediaItem.fromUri(url)
        return ProgressiveMediaSource.Factory(defaultHttpDataSourceFactory)
            .createMediaSource(mediaItem)
    }


    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()

    }

}
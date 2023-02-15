package com.example.videoplayer.repository

import com.example.videoplayer.Adapter.VideoSongAdapter
import com.example.videoplayer.Model.VideoData
import com.example.videoplayer.util.UiState

interface VideoRepository {
    suspend fun getVideoSongs(result: (UiState.Success<ArrayList<VideoData>>) -> Unit)
}
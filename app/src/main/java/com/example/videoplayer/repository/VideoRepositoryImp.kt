package com.example.videoplayer.repository

import android.util.Log
import com.example.videoplayer.Adapter.VideoSongAdapter
import com.example.videoplayer.Model.VideoData
import com.example.videoplayer.util.UiState
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "VideoRepositoryImp"
class VideoRepositoryImp(
    private val database:FirebaseFirestore
):VideoRepository {

    private lateinit var songList:ArrayList<VideoData>



    override suspend fun getVideoSongs(result: (UiState.Success<ArrayList<VideoData>>) -> Unit) {
        songList = ArrayList()

        database.collection("videos")

            .addSnapshotListener{ snapshots ,e ->{

            }
                for (document in snapshots!!){
                    songList.add(VideoData(document.data.get("name").toString(),document.data.get("url").toString(),document.data.get("img_url").toString(),document.data.get("mf_date").toString()))

                }
                Log.d(TAG, "getSongs: $songList")
                result.invoke(UiState.Success(songList))
            }
    }
}
package com.example.videoplayer.UI

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videoplayer.Adapter.VideoSongAdapter
import com.example.videoplayer.Model.VideoData
import com.example.videoplayer.R
import com.example.videoplayer.databinding.FragmentVideoListBinding
import com.example.videoplayer.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import java.text.FieldPosition

private const val TAG = "VideoList"
@AndroidEntryPoint
class VideoList : Fragment() {
    private  lateinit var binding:FragmentVideoListBinding
    private val viewModel by viewModels<VideoViewModel>()
    private lateinit var videoList:ArrayList<VideoData>
    private lateinit var videoAdapter:VideoSongAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =FragmentVideoListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoList = ArrayList()
        observeSongs()

    }

    private fun observeSongs() {
        viewModel.getSongs.observe(viewLifecycleOwner){
            when(it){
                is UiState.Success  -> {
                    for (data in it.data) videoList.add(data)
                    Log.d(TAG, "observeSongs: $videoList")
                    updateRecyclerView()
                }
                is UiState.Loading -> {

                }
                is UiState.Failure -> {}
            }
        }
    }

    private fun updateRecyclerView(){
        Log.d(TAG, "updateRecyclerView: ${videoList.size}")
        binding.videoRecyclerView.setHasFixedSize(true)
        binding.videoRecyclerView.setItemViewCacheSize(20)
        binding.videoRecyclerView.layoutManager = LinearLayoutManager(context)
        videoAdapter = VideoSongAdapter(requireActivity(),videoList,::onSongClicked)
        binding.videoRecyclerView.adapter = videoAdapter
    }
    private fun onSongClicked(list:ArrayList<VideoData>,position: Int){
        Log.d(TAG, "onSongClicked:$position")
        val bundle =Bundle()
        bundle.putParcelableArrayList("list",list)
        bundle.putInt("position",position)
        findNavController().navigate(R.id.action_videoList_to_playerActivity,bundle)
    }


}
package com.example.videoplayer.UI

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_view,menu)
        val searchView = menu.findItem(R.id.seachView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                videoAdapter.filter.filter(newText.toString())
                return false
            }

        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
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

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView(){
        Log.d(TAG, "updateRecyclerView: ${videoList.size}")
        binding.videoRecyclerView.setHasFixedSize(true)
        binding.videoRecyclerView.setItemViewCacheSize(20)
        binding.videoRecyclerView.layoutManager = LinearLayoutManager(context)
        videoAdapter = VideoSongAdapter(requireActivity(),videoList,::onSongClicked)
        binding.videoRecyclerView.adapter = videoAdapter
        videoAdapter.notifyDataSetChanged()
    }
    private fun onSongClicked(list:ArrayList<VideoData>,position: Int){
        Log.d(TAG, "onSongClicked:$position")
        val bundle =Bundle()
        bundle.putParcelableArrayList("list",list)
        bundle.putInt("position",position)
        findNavController().navigate(R.id.action_videoList_to_playerActivity,bundle)
    }



}
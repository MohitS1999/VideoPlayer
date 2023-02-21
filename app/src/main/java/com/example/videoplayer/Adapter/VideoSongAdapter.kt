package com.example.videoplayer.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Binder
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.videoplayer.Model.VideoData
import com.example.videoplayer.databinding.ListViewBinding

private const val TAG = "VideoSongAdapter"
class VideoSongAdapter(
    private val context: Context,
    private val videoList:ArrayList<VideoData>,
    private val onSongClicked:(ArrayList<VideoData>,Int) -> Unit

) : RecyclerView.Adapter<VideoSongAdapter.MyHolder>(),Filterable {



    private var videoListFull:ArrayList<VideoData> = ArrayList(videoList)
    class MyHolder(binding:ListViewBinding):RecyclerView.ViewHolder(binding.root){
        val songName = binding.songName
        val image = binding.imageView
        val duration = binding.time
        val mfDate = binding.date
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(ListViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val videoData:VideoData = videoList.get(position)
        Glide.with(holder.itemView)
            .load(videoData.imgUrl)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .error(com.google.android.material.R.drawable.mtrl_ic_error)
            .into(holder.image)
        holder.mfDate.text = videoData.mfDate
        holder.songName.text = videoData.songName
        holder.itemView.setOnClickListener{
            onSongClicked(videoList,position)
        }

    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun getFilter(): Filter {
        return filterUser()
    }
    private fun filterUser() = object : Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val searchText:String = constraint.toString().toLowerCase()
            val temp:ArrayList<VideoData> = ArrayList()
            if (searchText.length == 0 || searchText.isEmpty()){
                temp.addAll(videoListFull)
            }else{
                for (document in videoListFull){
                    Log.d(TAG, "performFiltering: $document")
                    if (document.songName?.toLowerCase()?.contains(searchText) == true) temp.add(document)
                }
            }
            Log.d(TAG, "performFiltering: $temp")
            val filterResult:FilterResults = FilterResults()
            filterResult.values = temp
            return filterResult
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            videoList.clear()
            if (results != null) {
                videoList.addAll(results.values as Collection<VideoData>)
            }
            notifyDataSetChanged()
        }

    }

}
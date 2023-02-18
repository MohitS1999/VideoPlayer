package com.example.videoplayer.Model

import android.os.Parcel
import android.os.Parcelable

data class VideoData(
    val songName: String? ="",
    val songUrl: String? ="",
    val imgUrl: String? ="",
    val mfDate: String? ="",
    val time:Long=0
) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(songName)
        parcel.writeString(songUrl)
        parcel.writeString(imgUrl)
        parcel.writeString(mfDate)
        parcel.writeLong(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoData> {
        override fun createFromParcel(parcel: Parcel): VideoData {
            return VideoData(parcel)
        }

        override fun newArray(size: Int): Array<VideoData?> {
            return arrayOfNulls(size)
        }
    }
}
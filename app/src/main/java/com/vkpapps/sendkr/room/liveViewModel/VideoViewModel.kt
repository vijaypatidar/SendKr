package com.vkpapps.sendkr.room.liveViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vkpapps.sendkr.loader.PrepareDb
import com.vkpapps.sendkr.model.MediaInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        val videoInfos = ArrayList<MediaInfo>()
    }

    val videoInfosLiveData = MutableLiveData(videoInfos)

    private fun notifyDataChange() {
        videoInfosLiveData.postValue(VideoViewModel.videoInfos)
    }

    fun refreshData() {
        CoroutineScope(Dispatchers.IO).launch {
            PrepareDb().prepareVideo()
            notifyDataChange()
        }
    }


}

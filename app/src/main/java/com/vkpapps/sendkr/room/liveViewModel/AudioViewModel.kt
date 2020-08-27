package com.vkpapps.sendkr.room.liveViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vkpapps.sendkr.loader.PrepareDb
import com.vkpapps.sendkr.model.MediaInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        var audioInfos = ArrayList<MediaInfo>()
    }

    val mediaInfosLiveData: MutableLiveData<List<MediaInfo>> = MutableLiveData(audioInfos)

    private fun notifyDataChange() {
        mediaInfosLiveData.postValue(audioInfos)
    }

    fun refreshData() {
        CoroutineScope(Dispatchers.IO).launch {
            PrepareDb().prepareAudio()
            notifyDataChange()
        }
    }
}

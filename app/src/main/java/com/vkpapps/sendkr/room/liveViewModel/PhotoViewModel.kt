package com.vkpapps.sendkr.room.liveViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vkpapps.sendkr.loader.PrepareDb
import com.vkpapps.sendkr.model.MediaInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        val photoInfos = ArrayList<MediaInfo>()
    }

    val photoInfosLiveData = MutableLiveData(photoInfos)

    private fun notifyDataChange() {
        photoInfosLiveData.postValue(photoInfos)
    }

    fun refreshData() {
        CoroutineScope(IO).launch {
            PrepareDb().preparePhoto()
            notifyDataChange()
        }
    }

}

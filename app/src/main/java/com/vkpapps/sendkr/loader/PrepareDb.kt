package com.vkpapps.sendkr.loader

import com.vkpapps.sendkr.room.liveViewModel.AudioViewModel
import com.vkpapps.sendkr.room.liveViewModel.PhotoViewModel
import com.vkpapps.sendkr.room.liveViewModel.VideoViewModel

class PrepareDb {
    fun prepareAll() {
        PrepareAppList.appList
        prepareAudio()
        prepareVideo()
        preparePhoto()
    }

    fun prepareAudio() {
        AudioViewModel.audioInfos.clear()
        AudioViewModel.audioInfos.addAll(PrepareAudioList().getList())
    }

    fun preparePhoto() {
        PhotoViewModel.photoInfos.clear()
        PhotoViewModel.photoInfos.addAll(PreparePhotoList().getList())
    }

    fun prepareVideo() {
        VideoViewModel.videoInfos.clear()
        VideoViewModel.videoInfos.addAll(PrepareVideoList().getList())
    }
}
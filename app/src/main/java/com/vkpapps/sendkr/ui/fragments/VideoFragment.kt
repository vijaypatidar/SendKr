package com.vkpapps.sendkr.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.sendkr.App.Companion.isPhone
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.room.liveViewModel.VideoViewModel
import com.vkpapps.sendkr.ui.adapter.VideoAdapter
import com.vkpapps.sendkr.ui.fragments.dialog.FilterDialogFragment

/***
 * @author VIJAY PATIDAR
 */
class VideoFragment : MediaBaseFragment() {

    companion object {
        private var sortBy = FilterDialogFragment.SORT_BY_NAME
    }

    private val videoViewModel by lazy { ViewModelProvider(requireActivity()).get(VideoViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoViewModel.videoInfosLiveData.observe(requireActivity(), {
            onDataChanged(it)
        })

    }

    override fun getSpanCount(): Int {
        return if (isPhone) 1 else 2
    }

    override fun setSortBy(sortBy: Int) {
        VideoFragment.sortBy = sortBy
    }

    override fun getSortBy(): Int {
        return sortBy
    }


    override fun setAdapter(recyclerView: RecyclerView) {
        recyclerView.adapter = VideoAdapter(mediaInfos, this)
    }

    override fun onRefresh() {
        Logger.d("[VideoFragment][onRefresh]")
        videoViewModel.refreshData()
    }

}
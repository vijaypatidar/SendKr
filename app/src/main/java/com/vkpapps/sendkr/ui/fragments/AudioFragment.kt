package com.vkpapps.sendkr.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.sendkr.App.Companion.isPhone
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.room.liveViewModel.AudioViewModel
import com.vkpapps.sendkr.ui.adapter.AudioAdapter
import com.vkpapps.sendkr.ui.fragments.base.MediaBaseFragment
import com.vkpapps.sendkr.ui.fragments.dialog.FilterDialogFragment

/**
 * @author VIJAY PATIDAR
 */
class AudioFragment : MediaBaseFragment() {

    companion object {
        private var sortBy = FilterDialogFragment.SORT_BY_NAME
    }

    private val audioViewModel by lazy { ViewModelProvider(requireActivity()).get(AudioViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioViewModel.mediaInfosLiveData.observe(requireActivity(), {
            Logger.d("on audio changes")
            onDataChanged(it)
        })
    }

    override fun getSpanCount(): Int {
        return if (isPhone) 1 else 2
    }

    override fun setSortBy(sortBy: Int) {
        AudioFragment.sortBy = sortBy
    }

    override fun getSortBy(): Int {
        return sortBy
    }
    override fun getFileType(): Int {
        return FileType.FILE_TYPE_MUSIC
    }
    override fun setAdapter(recyclerView: RecyclerView) {
        recyclerView.adapter = AudioAdapter(mediaInfos, this)
    }

    override fun onRefresh() {
        Logger.d("[AudioFragment][onRefresh]")
        audioViewModel.refreshData()
    }
}

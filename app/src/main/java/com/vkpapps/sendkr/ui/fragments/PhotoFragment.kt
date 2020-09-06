package com.vkpapps.sendkr.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.sendkr.App.Companion.isPhone
import com.vkpapps.sendkr.room.liveViewModel.PhotoViewModel
import com.vkpapps.sendkr.ui.adapter.PhotoAdapter
import com.vkpapps.sendkr.ui.fragments.dialog.FilterDialogFragment

/***
 * @author VIJAY PATIDAR
 */
class PhotoFragment : MediaBaseFragment() {

    companion object {
        private var sortBy = FilterDialogFragment.SORT_BY_LATEST_FIRST
    }

    private val photoViewModel by lazy { ViewModelProvider(requireActivity()).get(PhotoViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoViewModel.photoInfosLiveData.observe(requireActivity(), {
            onDataChanged(it)
        })
    }

    override fun getSpanCount(): Int {
        return if (isPhone) 3 else 6
    }

    override fun setSortBy(sortBy: Int) {
        PhotoFragment.sortBy = sortBy
    }

    override fun getSortBy(): Int {
        return sortBy
    }

    override fun setAdapter(recyclerView: RecyclerView) {
        recyclerView.adapter = PhotoAdapter(mediaInfos, this)
    }

    override fun onRefresh() {
        photoViewModel.refreshData()
    }

}
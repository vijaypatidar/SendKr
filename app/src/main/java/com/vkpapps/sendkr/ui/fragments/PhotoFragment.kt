package com.vkpapps.sendkr.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vkpapps.sendkr.App.Companion.isPhone
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.interfaces.OnFileRequestPrepareListener
import com.vkpapps.sendkr.interfaces.OnMediaSelectListener
import com.vkpapps.sendkr.interfaces.OnNavigationVisibilityListener
import com.vkpapps.sendkr.model.MediaInfo
import com.vkpapps.sendkr.model.RawRequestInfo
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.room.liveViewModel.PhotoViewModel
import com.vkpapps.sendkr.ui.adapter.PhotoAdapter
import com.vkpapps.sendkr.ui.fragments.dialog.FilePropertyDialogFragment
import com.vkpapps.sendkr.ui.fragments.dialog.FilterDialogFragment
import com.vkpapps.sendkr.utils.MathUtils
import kotlinx.android.synthetic.main.fragment_photo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/***
 * @author VIJAY PATIDAR
 */
class PhotoFragment : Fragment(), OnMediaSelectListener, SwipeRefreshLayout.OnRefreshListener, FilterDialogFragment.OnFilterListener {
    companion object {
        private var sortBy = FilterDialogFragment.SORT_BY_LATEST_FIRST
    }

    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private val photoInfos: MutableList<MediaInfo> = ArrayList()
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    private var photoAdapter: PhotoAdapter? = null
    private var selectedCount = 0
    private var controller: NavController? = null
    private val photoViewModel by lazy { ViewModelProvider(requireActivity()).get(PhotoViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        controller = Navigation.findNavController(view)
        val spanCount = if (isPhone) {
            3
        } else {
            6
        }
        photoList.layoutManager = GridLayoutManager(requireContext(), spanCount)
        photoAdapter = PhotoAdapter(photoInfos, this)
        photoList.adapter = photoAdapter
        photoList.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (selectedCount == 0)
                    onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }
        swipeRefreshPhotoList.setOnRefreshListener(this)

        photoViewModel.photoInfosLiveData.observe(requireActivity(), {
            try {
                selectedCount = 0
                swipeRefreshPhotoList?.hide()
                if (it.isNotEmpty()) {
                    CoroutineScope(IO).launch {
                        it.forEach { item ->
                            if (item.isSelected) {
                                selectedCount++
                            }
                        }
                        withContext(Main) {
                            hideShowSendButton()
                        }
                    }
                    photoInfos.clear()
                    photoInfos.addAll(it)
                    photoAdapter?.notifyDataSetChanged()
                    emptyPhoto.visibility = View.GONE
                } else {
                    emptyPhoto.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        selectionView.btnSendFiles.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                photoInfos.forEach {
                    if (it.isSelected) {
                        it.isSelected = false
                        selected.add(RawRequestInfo(
                                it.name, it.uri, FileType.FILE_TYPE_PHOTO, MathUtils.getFileSize(DocumentFile.fromFile(it.uri.toFile()))
                        ))
                    }
                }
                selectedCount = 0
                withContext(Main) {
                    photoAdapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
                onFileRequestPrepareListener?.sendFiles(selected)
            }
        }

        selectionView.btnSelectNon.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                photoInfos.forEach {
                    it.isSelected = false
                }
                selectedCount = 0
                withContext(Main) {
                    photoAdapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }

        selectionView.btnSelectAll.setOnClickListener {
            CoroutineScope(IO).launch {
                selectedCount = 0
                photoInfos.forEach {
                    it.isSelected = true
                    selectedCount++
                }
                withContext(Main) {
                    photoAdapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }
    }

    private fun sort() {
        when (sortBy) {
            FilterDialogFragment.SORT_BY_NAME -> {
                photoInfos.sortBy { photoInfo -> photoInfo.name }
            }
            FilterDialogFragment.SORT_BY_NAME_Z_TO_A -> {
                photoInfos.sortBy { photoInfo -> photoInfo.name }
                photoInfos.reverse()
            }
            FilterDialogFragment.SORT_BY_OLDEST_FIRST -> {
                photoInfos.sortBy { photoInfo -> photoInfo.lastModified }
            }
            FilterDialogFragment.SORT_BY_LATEST_FIRST -> {
                photoInfos.sortBy { photoInfo -> photoInfo.lastModified * -1 }
            }
            FilterDialogFragment.SORT_BY_SIZE_ASC -> {
                photoInfos.sortBy { photoInfo -> photoInfo.size }
            }
            FilterDialogFragment.SORT_BY_SIZE_DSC -> {
                photoInfos.sortBy { photoInfo -> photoInfo.size * -1 }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_sorting) {
            FilterDialogFragment(sortBy, this).show(requireActivity().supportFragmentManager, "SortBy")
            true
        } else
            super.onOptionsItemSelected(item)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNavigationVisibilityListener) {
            onNavigationVisibilityListener = context
        }
        if (context is OnFileRequestPrepareListener) {
            onFileRequestPrepareListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        onNavigationVisibilityListener = null
    }

    override fun onResume() {
        super.onResume()
        hideShowSendButton()
    }

    private fun hideShowSendButton() {
        onNavigationVisibilityListener?.onNavVisibilityChange(selectedCount == 0)
        selectionView?.changeVisibility(selectedCount)
    }

    override fun onMediaLongClickListener(mediaInfo: MediaInfo) {
        controller?.navigate(object : NavDirections {
            override fun getArguments(): Bundle {
                return Bundle().apply {
                    putString(FilePropertyDialogFragment.PARAM_FILE_ID, mediaInfo.id)
                    putString(FilePropertyDialogFragment.PARAM_FILE_URI, mediaInfo.uri.toString())
                }
            }

            override fun getActionId(): Int {
                return R.id.filePropertyDialogFragment
            }
        })
    }

    override fun onMediaSelected(mediaInfo: MediaInfo) {
        selectedCount++
        hideShowSendButton()
    }

    override fun onMediaDeselected(mediaInfo: MediaInfo) {
        selectedCount--
        hideShowSendButton()
    }

    override fun onRefresh() {
        photoViewModel.refreshData()
    }

    override fun onFilterBy(sortBy: Int) {
        Companion.sortBy = sortBy
        CoroutineScope(IO).launch {
            if (!photoInfos.isNullOrEmpty()) {
                sort()
                withContext(Main) {
                    photoAdapter?.notifyDataSetChanged()
                    emptyPhoto?.visibility = View.GONE
                }
            } else {
                withContext(Main) {
                    emptyPhoto?.visibility = View.VISIBLE
                }
            }
        }
    }
}
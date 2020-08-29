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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.interfaces.OnFileRequestPrepareListener
import com.vkpapps.sendkr.interfaces.OnMediaSelectListener
import com.vkpapps.sendkr.interfaces.OnNavigationVisibilityListener
import com.vkpapps.sendkr.model.MediaInfo
import com.vkpapps.sendkr.model.RawRequestInfo
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.room.liveViewModel.VideoViewModel
import com.vkpapps.sendkr.ui.adapter.VideoAdapter
import com.vkpapps.sendkr.ui.fragments.dialog.FilePropertyDialogFragment
import com.vkpapps.sendkr.ui.fragments.dialog.FilterDialogFragment
import com.vkpapps.sendkr.utils.MathUtils
import kotlinx.android.synthetic.main.fragment_video.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/***
 * @author VIJAY PATIDAR
 */
class VideoFragment : Fragment(), OnMediaSelectListener, SwipeRefreshLayout.OnRefreshListener, FilterDialogFragment.OnFilterListener {
    companion object {
        private var sortBy = FilterDialogFragment.SORT_BY_LATEST_FIRST
    }

    private val videoInfos: MutableList<MediaInfo> = ArrayList()
    private var adapter: VideoAdapter? = null
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var selectedCount = 0
    private var controller: NavController? = null
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    private val videoViewModel by lazy { ViewModelProvider(requireActivity()).get(VideoViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        controller = Navigation.findNavController(view)
        videoList.layoutManager = LinearLayoutManager(requireContext())
        adapter = VideoAdapter(videoInfos, this)
        videoList.adapter = adapter
        videoList.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (selectedCount == 0)
                    onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }

        swipeRefreshVideoList.setOnRefreshListener(this)

        videoViewModel.videoInfosLiveData.observe(requireActivity(), {
            try {
                selectedCount = 0
                swipeRefreshVideoList?.hide()
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
                    videoInfos.clear()
                    videoInfos.addAll(it)
                    sort()
                    adapter?.notifyDataSetChanged()
                    emptyVideo.visibility = View.GONE
                } else {
                    emptyVideo.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        selectionView.btnSendFiles.setOnClickListener {

            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                videoInfos.forEach {
                    if (it.isSelected) {
                        it.isSelected = false
                        selected.add(RawRequestInfo(
                                it.name, it.uri, FileType.FILE_TYPE_VIDEO, MathUtils.getFileSize(DocumentFile.fromFile(it.uri.toFile()))
                        ))
                    }
                }
                selectedCount = 0
                withContext(Main) {
                    adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
                onFileRequestPrepareListener?.sendFiles(selected)
            }
        }

        selectionView.btnSelectNon.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                videoInfos.forEach {
                    it.isSelected = false
                }
                selectedCount = 0
                withContext(Main) {
                    adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }

        selectionView.btnSelectAll.setOnClickListener {
            CoroutineScope(IO).launch {
                selectedCount = 0
                videoInfos.forEach {
                    it.isSelected = true
                    selectedCount++
                }
                withContext(Main) {
                    adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
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

    override fun onResume() {
        super.onResume()
        hideShowSendButton()
    }

    private fun hideShowSendButton() {
        selectionView?.changeVisibility(selectedCount)
        onNavigationVisibilityListener?.onNavVisibilityChange(selectedCount == 0)
    }

    private fun sort() {
        when (sortBy) {
            FilterDialogFragment.SORT_BY_NAME -> {
                videoInfos.sortBy { videoInfo -> videoInfo.name }
            }
            FilterDialogFragment.SORT_BY_NAME_Z_TO_A -> {
                videoInfos.sortBy { videoInfo -> videoInfo.name }
                videoInfos.reverse()
            }
            FilterDialogFragment.SORT_BY_OLDEST_FIRST -> {
                videoInfos.sortBy { videoInfo -> videoInfo.lastModified }
            }
            FilterDialogFragment.SORT_BY_LATEST_FIRST -> {
                videoInfos.sortBy { videoInfo -> videoInfo.lastModified * -1 }
            }
            FilterDialogFragment.SORT_BY_SIZE_ASC -> {
                videoInfos.sortBy { videoInfo -> videoInfo.size }
            }
            FilterDialogFragment.SORT_BY_SIZE_DSC -> {
                videoInfos.sortBy { videoInfo -> videoInfo.size * -1 }
            }
        }
    }

    override fun onRefresh() {
        Logger.d("[VideoFragment][onRefresh]")
        videoViewModel.refreshData()
    }

    override fun onFilterBy(sortBy: Int) {
        VideoFragment.sortBy = sortBy
        CoroutineScope(IO).launch {
            if (!videoInfos.isNullOrEmpty()) {
                sort()
                withContext(Main) {
                    adapter?.notifyDataSetChanged()
                    emptyVideo?.visibility = View.GONE
                }
            } else {
                withContext(Main) {
                    emptyVideo?.visibility = View.VISIBLE
                }
            }
        }
    }

}
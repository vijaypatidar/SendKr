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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
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
import com.vkpapps.sendkr.room.liveViewModel.AudioViewModel
import com.vkpapps.sendkr.ui.adapter.AudioAdapter
import com.vkpapps.sendkr.ui.fragments.dialog.FilePropertyDialogFragment
import com.vkpapps.sendkr.ui.fragments.dialog.FilterDialogFragment
import com.vkpapps.sendkr.utils.MathUtils
import kotlinx.android.synthetic.main.fragment_music.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author VIJAY PATIDAR
 */
class AudioFragment : Fragment(), OnMediaSelectListener, SwipeRefreshLayout.OnRefreshListener, FilterDialogFragment.OnFilterListener {

    companion object {
        private var sortBy = FilterDialogFragment.SORT_BY_NAME
    }

    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var selectedCount = 0
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    private var controller: NavController? = null
    private val mediaInfos: MutableList<MediaInfo> = ArrayList()
    private var audioAdapter: AudioAdapter? = null
    private val audioViewModel by lazy { ViewModelProvider(requireActivity()).get(AudioViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        controller = Navigation.findNavController(view)
        audioAdapter = AudioAdapter(mediaInfos, this, view.context)
        audioList.itemAnimator = DefaultItemAnimator()

        val isPhone = resources.getBoolean(R.bool.isPhone)
        val spanCount = if (isPhone) 1 else 2
        audioList.layoutManager = GridLayoutManager(requireContext(), spanCount)
        audioList.adapter = audioAdapter
        audioList.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (selectedCount == 0) {
                    onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                } else {
                    onNavigationVisibilityListener?.onNavVisibilityChange(false)
                }
                return false
            }
        }
        swipeRefreshAudioList.setOnRefreshListener(this)
        //load music
        audioViewModel.mediaInfosLiveData.observe(requireActivity(), {
            Logger.d("on audio changes")
            try {
                selectedCount = 0
                swipeRefreshAudioList?.hide()
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
                    mediaInfos.clear()
                    mediaInfos.addAll(it)
                    sort()
                    audioAdapter?.notifyDataSetChanged()
                    emptyMusic.visibility = View.GONE
                } else {
                    emptyMusic.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })


        selectionView.btnSendFiles.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                mediaInfos.forEach {
                    if (it.isSelected) {
                        it.isSelected = false
                        selected.add(RawRequestInfo(
                                it.name, it.uri, FileType.FILE_TYPE_MUSIC, MathUtils.getFileSize(DocumentFile.fromFile(it.uri.toFile()))
                        ))
                    }
                }
                selectedCount = 0
                withContext(Main) {
                    audioAdapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
                onFileRequestPrepareListener?.sendFiles(selected)
            }
        }

        selectionView.btnSelectNon.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                mediaInfos.forEach {
                    it.isSelected = false
                }
                selectedCount = 0
                withContext(Main) {
                    audioAdapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }

        selectionView.btnSelectAll.setOnClickListener {
            CoroutineScope(IO).launch {
                selectedCount = 0
                mediaInfos.forEach {
                    it.isSelected = true
                    selectedCount++
                }
                withContext(Main) {
                    audioAdapter?.notifyDataSetChanged()
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
        onFileRequestPrepareListener = null
    }

    override fun onResume() {
        super.onResume()
        hideShowSendButton()
    }

    private fun hideShowSendButton() {
        selectionView.changeVisibility(selectedCount)
        onNavigationVisibilityListener?.onNavVisibilityChange(selectedCount == 0)
    }

    private fun sort() {
        when (sortBy) {
            FilterDialogFragment.SORT_BY_NAME -> {
                mediaInfos.sortBy { audioInfo -> audioInfo.name }
            }
            FilterDialogFragment.SORT_BY_NAME_Z_TO_A -> {
                mediaInfos.sortBy { audioInfo -> audioInfo.name }
                mediaInfos.reverse()
            }
            FilterDialogFragment.SORT_BY_OLDEST_FIRST -> {
                mediaInfos.sortBy { audioInfo -> audioInfo.lastModified }
            }
            FilterDialogFragment.SORT_BY_LATEST_FIRST -> {
                mediaInfos.sortBy { audioInfo -> audioInfo.lastModified * -1 }
            }
            FilterDialogFragment.SORT_BY_SIZE_ASC -> {
                mediaInfos.sortBy { audioInfo -> audioInfo.size }
            }
            FilterDialogFragment.SORT_BY_SIZE_DSC -> {
                mediaInfos.sortBy { audioInfo -> audioInfo.size * -1 }
            }
        }
    }

    override fun onRefresh() {
        audioViewModel.refreshData()
    }

    override fun onFilterBy(sortBy: Int) {
        AudioFragment.sortBy = sortBy
        CoroutineScope(IO).launch {
            if (!mediaInfos.isNullOrEmpty()) {
                sort()
                withContext(Main) {
                    audioAdapter?.notifyDataSetChanged()
                    emptyMusic.visibility = View.GONE
                }
            } else {
                withContext(Main) {
                    emptyMusic.visibility = View.VISIBLE
                }
            }
        }
    }
}
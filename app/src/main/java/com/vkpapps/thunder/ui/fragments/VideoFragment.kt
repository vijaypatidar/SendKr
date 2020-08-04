package com.vkpapps.thunder.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.interfaces.OnFileRequestPrepareListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.model.RawRequestInfo
import com.vkpapps.thunder.model.VideoInfo
import com.vkpapps.thunder.model.constaints.FileType
import com.vkpapps.thunder.room.liveViewModel.VideoViewModel
import com.vkpapps.thunder.ui.adapter.VideoAdapter
import com.vkpapps.thunder.ui.adapter.VideoAdapter.OnVideoSelectListener
import com.vkpapps.thunder.ui.fragments.dialog.FilePropertyDialogFragment
import com.vkpapps.thunder.ui.fragments.dialog.FilterDialogFragment
import com.vkpapps.thunder.utils.MathUtils
import kotlinx.android.synthetic.main.fragment_video.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/***
 * @author VIJAY PATIDAR
 */
class VideoFragment : Fragment(), OnVideoSelectListener {
    private val videoInfos: MutableList<VideoInfo> = ArrayList()
    private var adapter: VideoAdapter? = null
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var selectedCount = 0
    private var controller: NavController? = null
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    private var sortBy = FilterDialogFragment.SORT_BY_LATEST_FIRST

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        controller = Navigation.findNavController(view)
        val recyclerView: RecyclerView = view.findViewById(R.id.videoList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = VideoAdapter(videoInfos, this)
        recyclerView.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (selectedCount == 0)
                    onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }
        recyclerView.adapter = adapter

        val videoViewModel = ViewModelProvider(requireActivity()).get(VideoViewModel::class.java)
        videoViewModel.videoInfos.observe(requireActivity(), androidx.lifecycle.Observer { it ->
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
                    Toast.makeText(requireContext(), "${selected.size} videos added to send queue", Toast.LENGTH_SHORT).show()
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
                withContext(Dispatchers.Main) {
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
                withContext(Dispatchers.Main) {
                    adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }


        val model = activity?.run {
            ViewModelProvider(this).get(FilterDialogFragment.SharedViewModel::class.java)
        }
        model?.sortBy?.observe(requireActivity(), androidx.lifecycle.Observer {
            if (it.target == 3) {
                Logger.d("Dialog result video ${it.sortBy}")
                sortBy = it.sortBy
                CoroutineScope(Dispatchers.IO).launch {
                    if (!videoInfos.isNullOrEmpty()) {
                        sort()
                        withContext(Dispatchers.Main) {
                            adapter?.notifyDataSetChanged()
                            emptyVideo.visibility = View.GONE
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            emptyVideo.visibility = View.VISIBLE
                        }
                    }
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_sorting) {
            controller?.navigate(object : NavDirections {
                override fun getArguments(): Bundle {
                    return Bundle().apply {
                        putInt(FilterDialogFragment.PARAM_TARGET, 3)
                        putInt(FilterDialogFragment.PARAM_CURRENT_SORT_BY, sortBy)
                    }
                }

                override fun getActionId(): Int {
                    return R.id.filterDialogFragment
                }
            })
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

    override fun onVideoLongClickListener(videoInfo: VideoInfo) {
        controller?.navigate(object : NavDirections {
            override fun getArguments(): Bundle {
                return Bundle().apply {
                    putString(FilePropertyDialogFragment.PARAM_FILE_ID, videoInfo.id)
                    putString(FilePropertyDialogFragment.PARAM_FILE_URI, videoInfo.uri.toString())
                }
            }

            override fun getActionId(): Int {
                return R.id.filePropertyDialogFragment
            }
        })
    }

    override fun onVideoSelected(videoInfo: VideoInfo) {
        selectedCount++
        hideShowSendButton()
    }

    override fun onVideoDeselected(videoInfo: VideoInfo) {
        selectedCount--
        hideShowSendButton()
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

}
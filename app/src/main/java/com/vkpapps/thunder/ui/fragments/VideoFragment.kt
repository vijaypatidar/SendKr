package com.vkpapps.thunder.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.vkpapps.thunder.R
import com.vkpapps.thunder.interfaces.OnFileRequestPrepareListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.model.FileType
import com.vkpapps.thunder.model.RawRequestInfo
import com.vkpapps.thunder.model.VideoInfo
import com.vkpapps.thunder.room.database.MyRoomDatabase
import com.vkpapps.thunder.ui.adapter.VideoAdapter
import com.vkpapps.thunder.ui.adapter.VideoAdapter.OnVideoSelectListener
import kotlinx.android.synthetic.main.fragment_video.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/***
 * @author VIJAY PATIDAR
 */
class VideoFragment : Fragment(), OnVideoSelectListener {
    private val videoInfos: MutableList<VideoInfo> = ArrayList()
    private var adapter: VideoAdapter? = null
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var selectedCount = 0
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView = view.findViewById(R.id.videoList)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = VideoAdapter(videoInfos, view, this)
        recyclerView.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }
        recyclerView.adapter = adapter
        adapter?.notifyDataSetChangedAndHideIfNull()

        MyRoomDatabase.getDatabase(requireContext()).videoDao()
                .getLiveVideoInfos().observe(requireActivity(), androidx.lifecycle.Observer {
                    videoInfos.clear()
                    videoInfos.addAll(it)
                    adapter?.notifyDataSetChangedAndHideIfNull()
                })

        btnSend.setOnClickListener {

            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(Dispatchers.IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                videoInfos.forEach {
                    if (it.isSelected) {
                        it.isSelected = false
                        selected.add(RawRequestInfo(
                                it.name, it.path, FileType.FILE_TYPE_VIDEO
                        ))
                    }
                }
                selectedCount = 0
                withContext(Dispatchers.Main) {
                    adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                    Toast.makeText(requireContext(), "${selected.size} videos added to send queue", Toast.LENGTH_SHORT).show()
                }
                onFileRequestPrepareListener?.sendFiles(selected, FileType.FILE_TYPE_VIDEO)
            }
        }
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

    override fun onVideoSelected(videoInfo: VideoInfo) {
        selectedCount++
        hideShowSendButton()
    }

    override fun onVideoDeselected(videoInfo: VideoInfo) {
        selectedCount--
        hideShowSendButton()
    }

    private fun hideShowSendButton() {
        if (btnSend.visibility == View.VISIBLE && selectedCount > 0) return
        if (selectedCount == 0) {
            btnSend.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_to_bottom)
            btnSend.visibility = View.GONE
            onNavigationVisibilityListener?.onNavVisibilityChange(true)
        } else {
            btnSend.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_bottom)
            btnSend.visibility = View.VISIBLE
            onNavigationVisibilityListener?.onNavVisibilityChange(false)
        }
    }
}
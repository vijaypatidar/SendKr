package com.vkpapps.thunder.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.vkpapps.thunder.R
import com.vkpapps.thunder.interfaces.OnFileRequestPrepareListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.model.AudioInfo
import com.vkpapps.thunder.model.FileType
import com.vkpapps.thunder.model.RawRequestInfo
import com.vkpapps.thunder.room.liveViewModel.AudioViewModel
import com.vkpapps.thunder.ui.adapter.AudioAdapter
import com.vkpapps.thunder.ui.adapter.AudioAdapter.OnAudioSelectedListener
import com.vkpapps.thunder.utils.PermissionUtils.askStoragePermission
import com.vkpapps.thunder.utils.PermissionUtils.checkStoragePermission
import kotlinx.android.synthetic.main.fragment_music.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author VIJAY PATIDAR
 */
class AudioFragment : Fragment(), OnAudioSelectedListener {
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var selectedCount = 0
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (checkStoragePermission(view.context)) {
            val recyclerView: RecyclerView = view.findViewById(R.id.audioList)
            var allSong: List<AudioInfo>? = null
            val audioAdapter = AudioAdapter(this, view.context)
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.layoutManager = LinearLayoutManager(view.context)
            recyclerView.adapter = audioAdapter
            recyclerView.onFlingListener = object : OnFlingListener() {
                override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                    onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                    return false
                }
            }

            //load music
            val audioViewModel = ViewModelProvider(requireActivity()).get(AudioViewModel::class.java)
            audioViewModel.audioInfos.observe(requireActivity(), androidx.lifecycle.Observer {
                if (it.isNotEmpty()) {
                    CoroutineScope(IO).launch {
                        it.forEach { item ->
                            if (item.isSelected) {
                                selectedCount++
                            }
                        }
                        withContext(Dispatchers.Main) {
                            hideShowSendButton()
                        }
                    }
                    allSong = it
                    audioAdapter.setAudioInfos(allSong)
                    emptyMusic.visibility = View.GONE
                } else {
                    emptyMusic.visibility = View.VISIBLE
                }
            })


            btnSend.setOnClickListener {
                if (selectedCount == 0) return@setOnClickListener
                CoroutineScope(IO).launch {
                    val selected = ArrayList<RawRequestInfo>()
                    allSong?.forEach {
                        if (it.isSelected) {
                            it.isSelected = false
                            selected.add(RawRequestInfo(
                                    it.name, it.path, FileType.FILE_TYPE_MUSIC
                            ))
                        }
                    }
                    selectedCount = 0
                    withContext(Dispatchers.Main) {
                        audioAdapter.notifyDataSetChanged()
                        hideShowSendButton()
                        Toast.makeText(requireContext(), "${selected.size} musics added to send queue", Toast.LENGTH_SHORT).show()
                    }
                    onFileRequestPrepareListener?.sendFiles(selected)
                }
            }
        } else {
            Navigation.findNavController(view).popBackStack()
            askStoragePermission(activity, 101)
        }
    }

    override fun onAudioSelected(audioMode: AudioInfo) {
        selectedCount++
        hideShowSendButton()
    }

    override fun onAudioDeselected(audioinfo: AudioInfo) {
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
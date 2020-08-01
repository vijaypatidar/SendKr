package com.vkpapps.thunder.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
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
import com.vkpapps.thunder.model.RawRequestInfo
import com.vkpapps.thunder.model.constaints.FileType
import com.vkpapps.thunder.room.liveViewModel.AudioViewModel
import com.vkpapps.thunder.ui.adapter.AudioAdapter
import com.vkpapps.thunder.ui.adapter.AudioAdapter.OnAudioSelectedListener
import com.vkpapps.thunder.utils.MathUtils
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
                    if (selectedCount == 0) {
                        onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                    } else {
                        onNavigationVisibilityListener?.onNavVisibilityChange(false)
                    }
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


            selectionView.btnSendFiles.setOnClickListener {
                if (selectedCount == 0) return@setOnClickListener
                CoroutineScope(IO).launch {
                    val selected = ArrayList<RawRequestInfo>()
                    allSong?.forEach {
                        if (it.isSelected) {
                            it.isSelected = false
                            selected.add(RawRequestInfo(
                                    it.name, it.uri, FileType.FILE_TYPE_MUSIC, MathUtils.getFileSize(DocumentFile.fromFile(it.uri.toFile()))
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

            selectionView.btnSelectNon.setOnClickListener {
                if (selectedCount == 0) return@setOnClickListener
                CoroutineScope(IO).launch {
                    allSong?.forEach {
                        it.isSelected = false
                    }
                    selectedCount = 0
                    withContext(Dispatchers.Main) {
                        audioAdapter.notifyDataSetChanged()
                        hideShowSendButton()
                    }
                }
            }

            selectionView.btnSelectAll.setOnClickListener {
                CoroutineScope(IO).launch {
                    selectedCount = 0
                    allSong?.forEach {
                        it.isSelected = true
                        selectedCount++
                    }
                    withContext(Dispatchers.Main) {
                        audioAdapter.notifyDataSetChanged()
                        hideShowSendButton()
                    }
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

    override fun onResume() {
        super.onResume()
        hideShowSendButton()
    }

    private fun hideShowSendButton() {
        selectionView.changeVisibility(selectedCount)
        onNavigationVisibilityListener?.onNavVisibilityChange(selectedCount == 0)
    }
}
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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.interfaces.OnFileRequestPrepareListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.model.AudioInfo
import com.vkpapps.thunder.model.RawRequestInfo
import com.vkpapps.thunder.model.constaints.FileType
import com.vkpapps.thunder.room.liveViewModel.AudioViewModel
import com.vkpapps.thunder.ui.adapter.AudioAdapter
import com.vkpapps.thunder.ui.adapter.AudioAdapter.OnAudioSelectedListener
import com.vkpapps.thunder.ui.fragments.dialog.FilePropertyDialogFragment
import com.vkpapps.thunder.ui.fragments.dialog.FilterDialogFragment
import com.vkpapps.thunder.utils.MathUtils
import kotlinx.android.synthetic.main.fragment_music.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author VIJAY PATIDAR
 */
class AudioFragment : Fragment(), OnAudioSelectedListener {

    companion object {
        private var sortBy = FilterDialogFragment.SORT_BY_NAME
    }

    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var selectedCount = 0
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    private var controller: NavController? = null
    private val audioInfos: MutableList<AudioInfo> = ArrayList()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        controller = Navigation.findNavController(view)

        val recyclerView: RecyclerView = view.findViewById(R.id.audioList)
        val audioAdapter = AudioAdapter(audioInfos, this, view.context)
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
            Logger.d("on audio changes")
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
                audioInfos.clear()
                audioInfos.addAll(it)
                sort()
                audioAdapter.notifyDataSetChanged()
                emptyMusic.visibility = View.GONE
            } else {
                emptyMusic.visibility = View.VISIBLE
            }
        })


        selectionView.btnSendFiles.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                audioInfos.forEach {
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
                audioInfos.forEach {
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
                audioInfos.forEach {
                    it.isSelected = true
                    selectedCount++
                }
                withContext(Dispatchers.Main) {
                    audioAdapter.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }


        val model = activity?.run {
            ViewModelProvider(this).get(FilterDialogFragment.SharedViewModel::class.java)
        }
        model?.sortBy?.observe(requireActivity(), androidx.lifecycle.Observer {
            if (it.target == 2) {
                Logger.d("Dialog result ${it.sortBy} audio")
                sortBy = it.sortBy
                CoroutineScope(IO).launch {
                    if (!audioInfos.isNullOrEmpty()) {
                        sort()
                        withContext(Main) {
                            audioAdapter.notifyDataSetChanged()
                            emptyMusic.visibility = View.GONE
                        }
                    } else {
                        withContext(Main) {
                            emptyMusic.visibility = View.VISIBLE
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
                        putInt(FilterDialogFragment.PARAM_TARGET, 2)
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

    override fun onAudioLongClickListener(audioInfo: AudioInfo) {
        controller?.navigate(object : NavDirections {
            override fun getArguments(): Bundle {
                return Bundle().apply {
                    putString(FilePropertyDialogFragment.PARAM_FILE_ID, audioInfo.id)
                    putString(FilePropertyDialogFragment.PARAM_FILE_URI, audioInfo.uri.toString())
                }
            }

            override fun getActionId(): Int {
                return R.id.filePropertyDialogFragment
            }
        })
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

    private fun sort() {
        when (sortBy) {
            FilterDialogFragment.SORT_BY_NAME -> {
                audioInfos.sortBy { audioInfo -> audioInfo.name }
            }
            FilterDialogFragment.SORT_BY_NAME_Z_TO_A -> {
                audioInfos.sortBy { audioInfo -> audioInfo.name }
                audioInfos.reverse()
            }
            FilterDialogFragment.SORT_BY_OLDEST_FIRST -> {
                audioInfos.sortBy { audioInfo -> audioInfo.lastModified }
            }
            FilterDialogFragment.SORT_BY_LATEST_FIRST -> {
                audioInfos.sortBy { audioInfo -> audioInfo.lastModified * -1 }
            }
            FilterDialogFragment.SORT_BY_SIZE_ASC -> {
                audioInfos.sortBy { audioInfo -> audioInfo.size }
            }
            FilterDialogFragment.SORT_BY_SIZE_DSC -> {
                audioInfos.sortBy { audioInfo -> audioInfo.size * -1 }
            }
        }
    }
}
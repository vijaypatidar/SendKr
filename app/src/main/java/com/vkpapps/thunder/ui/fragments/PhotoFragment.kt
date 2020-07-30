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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.vkpapps.thunder.R
import com.vkpapps.thunder.interfaces.OnFileRequestPrepareListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.model.PhotoInfo
import com.vkpapps.thunder.model.RawRequestInfo
import com.vkpapps.thunder.model.constaints.FileType
import com.vkpapps.thunder.room.liveViewModel.PhotoViewModel
import com.vkpapps.thunder.ui.adapter.PhotoAdapter
import com.vkpapps.thunder.ui.adapter.PhotoAdapter.OnPhotoSelectListener
import com.vkpapps.thunder.utils.MathUtils
import kotlinx.android.synthetic.main.fragment_photo.*
import kotlinx.android.synthetic.main.selection_options.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/***
 * @author VIJAY PATIDAR
 */
class PhotoFragment : Fragment(), OnPhotoSelectListener {
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private val photoInfos: MutableList<PhotoInfo> = ArrayList()
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    private var photoAdapter: PhotoAdapter? = null
    private var selectedCount = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoList.layoutManager = GridLayoutManager(requireContext(), 3)
        photoAdapter = PhotoAdapter(photoInfos, this, view)
        photoList.adapter = photoAdapter
        photoList.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (selectedCount == 0)
                    onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }

        val photoViewModel = ViewModelProvider(requireActivity()).get(PhotoViewModel::class.java)
        photoViewModel.photoInfos.observe(requireActivity(), androidx.lifecycle.Observer {
            if (it.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    it.forEach { item ->
                        if (item.isSelected) {
                            selectedCount++
                        }
                    }
                    withContext(Dispatchers.Main) {
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
        })
        selectionView.btnSendFiles.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(Dispatchers.IO).launch {
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
                withContext(Dispatchers.Main) {
                    photoAdapter?.notifyDataSetChanged()
                    hideShowSendButton()
                    Toast.makeText(requireContext(), "${selected.size} images added to send queue", Toast.LENGTH_SHORT).show()
                }
                onFileRequestPrepareListener?.sendFiles(selected)
            }
        }

        selectionView.btnSelectNon.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(Dispatchers.IO).launch {
                photoInfos.forEach {
                    it.isSelected = false
                }
                selectedCount = 0
                withContext(Dispatchers.Main) {
                    photoAdapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }

        selectionView.btnSelectAll.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                selectedCount = 0
                photoInfos.forEach {
                    it.isSelected = true
                    selectedCount++
                }
                withContext(Dispatchers.Main) {
                    photoAdapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
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

    override fun onResume() {
        super.onResume()
        hideShowSendButton()
    }

    private fun hideShowSendButton() {
        if (selectionSection.visibility == View.VISIBLE && selectedCount > 0) {
            onNavigationVisibilityListener?.onNavVisibilityChange(false)
            return
        }
        selectionView.changeVisibility(selectedCount)
        onNavigationVisibilityListener?.onNavVisibilityChange(selectedCount == 0)

    }

    override fun onPhotoSelected(photoInfo: PhotoInfo) {
        selectedCount++
        hideShowSendButton()
    }

    override fun onPhotoDeselected(photoInfo: PhotoInfo) {
        selectedCount--
        hideShowSendButton()
    }
}
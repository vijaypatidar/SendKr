package com.vkpapps.sendkr.ui.fragments

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
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.vkpapps.sendkr.ui.fragments.dialog.FilePropertyDialogFragment
import com.vkpapps.sendkr.ui.fragments.dialog.FilterDialogFragment
import com.vkpapps.sendkr.utils.MathUtils
import kotlinx.android.synthetic.main.fragment_media_base.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/***
 * @author VIJAY PATIDAR
 */
abstract class MediaBaseFragment : Fragment(), OnMediaSelectListener, SwipeRefreshLayout.OnRefreshListener, FilterDialogFragment.OnFilterListener {

    protected val mediaInfos: MutableList<MediaInfo> = ArrayList()
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var selectedCount = 0
    private var controller: NavController? = null
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media_base, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        controller = Navigation.findNavController(view)

        val spanCount = if (isPhone) getSpanCount() else getSpanCount()
        mediaList?.layoutManager = GridLayoutManager(requireContext(), spanCount)
        setAdapter(mediaList)
        mediaList?.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (selectedCount == 0)
                    onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }
        swipeRefreshMidiaList?.setOnRefreshListener(this)

        selectionView?.btnSendFiles?.setOnClickListener {

            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                mediaInfos.forEach {
                    if (it.isSelected) {
                        it.isSelected = false
                        selected.add(RawRequestInfo(
                                it.name, it.uri, FileType.FILE_TYPE_VIDEO, MathUtils.getFileSize(DocumentFile.fromFile(it.uri.toFile()))
                        ))
                    }
                }
                selectedCount = 0
                withContext(Main) {
                    mediaList?.adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
                onFileRequestPrepareListener?.sendFiles(selected)
            }
        }

        selectionView?.btnSelectNon?.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                mediaInfos.forEach {
                    it.isSelected = false
                }
                selectedCount = 0
                withContext(Main) {
                    mediaList?.adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }

        selectionView?.btnSelectAll?.setOnClickListener {
            CoroutineScope(IO).launch {
                selectedCount = 0
                mediaInfos.forEach {
                    it.isSelected = true
                    selectedCount++
                }
                withContext(Main) {
                    mediaList?.adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }
    }

    protected fun onDataChanged(list: ArrayList<MediaInfo>) {
        try {
            selectedCount = 0
            swipeRefreshMidiaList?.hide()
            if (list.isNotEmpty()) {
                CoroutineScope(IO).launch {
                    list.forEach { item ->
                        if (item.isSelected) {
                            selectedCount++
                        }
                    }
                    withContext(Main) {
                        hideShowSendButton()
                    }
                }
                mediaInfos.clear()
                mediaInfos.addAll(list)
                sort()
                mediaList.adapter?.notifyDataSetChanged()
                emptyMedia?.visibility = View.GONE
            } else {
                emptyMedia?.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_sorting) {
            FilterDialogFragment(getSortBy(), this).show(requireActivity().supportFragmentManager, "SortBy")
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

    abstract fun getSpanCount(): Int

    abstract fun setSortBy(sortBy: Int)

    abstract fun getSortBy(): Int

    abstract fun setAdapter(recyclerView: RecyclerView)

    override fun onDetach() {
        super.onDetach()
        onNavigationVisibilityListener = null
    }

    override fun onMediaLongClickListener(mediaInfo: MediaInfo) {
        try {
            FilePropertyDialogFragment(mediaInfo.uri, mediaInfo.id, mediaInfo.size).show(requireActivity().supportFragmentManager, "Property")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "error occurred, unable to display property", Toast.LENGTH_SHORT).show()
        }
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
        when (getSortBy()) {
            FilterDialogFragment.SORT_BY_NAME -> {
                mediaInfos.sortBy { mediaInfo -> mediaInfo.name }
            }
            FilterDialogFragment.SORT_BY_NAME_Z_TO_A -> {
                mediaInfos.sortBy { mediaInfo -> mediaInfo.name }
                mediaInfos.reverse()
            }
            FilterDialogFragment.SORT_BY_OLDEST_FIRST -> {
                mediaInfos.sortBy { mediaInfo -> mediaInfo.lastModified }
            }
            FilterDialogFragment.SORT_BY_LATEST_FIRST -> {
                mediaInfos.sortBy { mediaInfo -> mediaInfo.lastModified * -1 }
            }
            FilterDialogFragment.SORT_BY_SIZE_ASC -> {
                mediaInfos.sortBy { mediaInfo -> mediaInfo.size }
            }
            FilterDialogFragment.SORT_BY_SIZE_DSC -> {
                mediaInfos.sortBy { mediaInfo -> mediaInfo.size * -1 }
            }
        }
    }

    abstract override fun onRefresh()

    override fun onFilterBy(sortBy: Int) {
        setSortBy(sortBy)
        CoroutineScope(IO).launch {
            if (!mediaInfos.isNullOrEmpty()) {
                sort()
                withContext(Main) {
                    mediaList?.adapter?.notifyDataSetChanged()
                    emptyMedia?.visibility = View.GONE
                }
            } else {
                withContext(Main) {
                    emptyMedia?.visibility = View.VISIBLE
                }
            }
        }
    }

}
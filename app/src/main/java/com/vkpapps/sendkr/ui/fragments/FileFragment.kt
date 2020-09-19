package com.vkpapps.sendkr.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.interfaces.OnFileRequestPrepareListener
import com.vkpapps.sendkr.interfaces.OnNavigationVisibilityListener
import com.vkpapps.sendkr.model.FileInfo
import com.vkpapps.sendkr.model.RawRequestInfo
import com.vkpapps.sendkr.ui.adapter.FileAdapter
import com.vkpapps.sendkr.ui.fragments.base.MyFragment
import com.vkpapps.sendkr.ui.fragments.dialog.FilePropertyDialogFragment
import com.vkpapps.sendkr.ui.fragments.dialog.FilterDialogFragment
import com.vkpapps.sendkr.utils.KeyValue
import kotlinx.android.synthetic.main.fragment_file.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/***
 * @author VIJAY PATIDAR
 */
class FileFragment : MyFragment(), FileAdapter.OnFileSelectListener, FilterDialogFragment.OnFilterListener {

    companion object {
        const val FILE_ROOT = "FILE_ROOT"
        const val FRAGMENT_TITLE = "FRAGMENT_TITLE"
        private var sortBy = FilterDialogFragment.SORT_BY_NAME
    }

    private val files: MutableList<FileInfo> = ArrayList()
    private val folders: MutableList<FileInfo> = ArrayList()
    private val fileInfos: MutableList<FileInfo> = ArrayList()
    private var selectCount = 0
    private var init = false
    private var title: String? = "default"
    private var rootDir: String = DocumentFile.fromFile(File("/storage/emulated/0/")).uri.toString()
    private var myView: View? = null
    private var adapter: FileAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (requireArguments().containsKey(FILE_ROOT)) {
            val string = arguments?.getString(FILE_ROOT)
            if (string != null) {
                rootDir = string
            }
        }
        if (requireArguments().containsKey(FRAGMENT_TITLE)) {
            title = requireArguments().getString(FRAGMENT_TITLE)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val view = if (myView == null) {
            init = false
            inflater.inflate(R.layout.fragment_file, container, false)
        } else {
            myView!!
        }
        myView = view
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        try {
            if (!init) {
                init = true
                // show list and detail
                adapter = FileAdapter(this, controller!!, fileInfos)
                val recyclerView: RecyclerView = view.findViewById(R.id.fileList)
                recyclerView.adapter = adapter
                recyclerView.onFlingListener = object : OnFlingListener() {
                    override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                        if (selectCount == 0)
                            onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                        return false
                    }
                }

                val isPhone = resources.getBoolean(R.bool.isPhone)
                val spanCount = if (isPhone) 1 else 2
                recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
                val showHiddenFile = KeyValue(requireContext()).showHiddenFile
                CoroutineScope(IO).launch {
                    val listFiles = DocumentFile.fromFile(Uri.parse(rootDir).toFile()).listFiles()
                    listFiles.forEach {
                        if (showHiddenFile || it.name?.startsWith(".") == false) {
                            if (it.isDirectory) {
                                folders.add(FileInfo(it))
                            } else {
                                files.add(FileInfo(it))
                            }
                        }
                    }
                    sort()
                }
            }
        }catch (e:Exception){
            //exception
        }
    }

    override fun onSendSelected() {
        if (selectCount == 0) return
        CoroutineScope(IO).launch {
            val selected = ArrayList<RawRequestInfo>()
            fileInfos.forEach {
                try {
                    if (it.isSelected) {
                        it.isSelected = false
                        selected.add(RawRequestInfo(
                                it.name, it.uri, it.type, it.size
                        ))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            selectCount = 0
            withContext(Main) {
                adapter?.notifyDataSetChanged()
                hideShowSendButton()
            }
            onFileRequestPrepareListener?.sendFiles(selected)
        }
    }

    override fun onSelectAll() {
        CoroutineScope(IO).launch {
            selectCount = 0
            fileInfos.forEach {
                it.isSelected = true
                selectCount++
            }
            withContext(Main) {
                adapter?.notifyDataSetChanged()
                hideShowSendButton()
            }
        }
    }

    override fun onSelectNon() {
        CoroutineScope(IO).launch {
            selectCount = 0
            fileInfos.forEach {
                it.isSelected = false
            }
            withContext(Main) {
                adapter?.notifyDataSetChanged()
                hideShowSendButton()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // change title
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar
        if (supportActionBar != null) {
            supportActionBar.title = title
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val findItem = menu.findItem(R.id.menu_transferring)
        findItem?.actionView?.findViewById<CardView>(R.id.transferringActionView)?.setOnClickListener {
            controller?.navigate(object : NavDirections {
                override fun getArguments(): Bundle {
                    return Bundle()
                }

                override fun getActionId(): Int {
                    return R.id.action_fileFragment_to_transferringFragment
                }
            })
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

    override fun onDestroy() {
        super.onDestroy()
        fileInfos.clear()
        folders.clear()
        files.clear()
    }

    private fun hideShowSendButton() {
        onNavigationVisibilityListener?.onNavVisibilityChange(selectCount == 0)
        selectionView?.changeVisibility(selectCount)
    }

    override fun onFileDeselected(fileInfo: FileInfo) {
        selectCount--
        hideShowSendButton()
    }

    override fun onFileLongClickListener(fileInfo: FileInfo) {
        try {
            FilePropertyDialogFragment(fileInfo.uri, fileInfo.id, fileInfo.size).show(requireActivity().supportFragmentManager, "Property")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "error occurred, unable to display property", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFileSelected(fileInfo: FileInfo) {
        selectCount++
        hideShowSendButton()
    }

    private fun sort() {
        CoroutineScope(IO).launch {
            withContext(Main) {
                loadingFile?.visibility = View.VISIBLE
            }
            fileInfos.clear()
            sort(folders)
            sort(files)
            fileInfos.addAll(folders)
            fileInfos.addAll(files)
            withContext(Main) {
                adapter?.notifyDataSetChanged()
                loadingFile?.visibility = View.GONE
                if (fileInfos.size == 0) {
                    emptyDirectory?.visibility = View.VISIBLE
                } else {
                    emptyDirectory?.visibility = View.GONE
                }
            }
        }
    }

    private fun sort(list: MutableList<FileInfo>) {
        when (sortBy) {
            FilterDialogFragment.SORT_BY_NAME -> {
                list.sortBy { fileInfo -> fileInfo.name }
            }
            FilterDialogFragment.SORT_BY_NAME_Z_TO_A -> {
                list.sortBy { fileInfo -> fileInfo.name }
                list.reverse()
            }
            FilterDialogFragment.SORT_BY_OLDEST_FIRST -> {
                list.sortBy { fileInfo -> fileInfo.lastModified }
            }
            FilterDialogFragment.SORT_BY_LATEST_FIRST -> {
                list.sortBy { fileInfo -> fileInfo.lastModified * -1 }
            }
            FilterDialogFragment.SORT_BY_SIZE_ASC -> {
                list.sortBy { fileInfo -> fileInfo.size }
            }
            FilterDialogFragment.SORT_BY_SIZE_DSC -> {
                list.sortBy { fileInfo -> fileInfo.size * -1 }
            }
        }
    }

    override fun onFilterBy(sortBy: Int) {
        Companion.sortBy = sortBy
        sort()
    }
}
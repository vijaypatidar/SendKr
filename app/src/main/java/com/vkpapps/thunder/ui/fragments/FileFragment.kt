package com.vkpapps.thunder.ui.fragments

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
import com.vkpapps.thunder.model.FileInfo
import com.vkpapps.thunder.model.RawRequestInfo
import com.vkpapps.thunder.ui.adapter.FileAdapter
import com.vkpapps.thunder.ui.fragments.dialog.FilePropertyDialogFragment
import com.vkpapps.thunder.ui.fragments.dialog.FilterDialogFragment
import com.vkpapps.thunder.utils.MathUtils
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
class FileFragment : Fragment(), FileAdapter.OnFileSelectListener {

    companion object {
        const val FILE_ROOT = "FILE_ROOT"
        const val FRAGMENT_TITLE = "FRAGMENT_TITLE"
        private var sortBy = FilterDialogFragment.SORT_BY_NAME
    }

    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    private val files: MutableList<FileInfo> = ArrayList()
    private val folders: MutableList<FileInfo> = ArrayList()
    private val fileInfos: MutableList<FileInfo> = ArrayList()
    private var selectCount = 0
    private var init = false
    private var title: String? = "default"
    private var navController: NavController? = null
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
        navController = Navigation.findNavController(view)

        if (!init) {
            init = true
            // show list and detail
            adapter = FileAdapter(this, navController!!, fileInfos)
            val recyclerView: RecyclerView = view.findViewById(R.id.fileList)
            recyclerView.adapter = adapter
            recyclerView.onFlingListener = object : OnFlingListener() {
                override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                    if (selectCount == 0)
                        onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                    return false
                }
            }
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            CoroutineScope(IO).launch {
                val listFiles = DocumentFile.fromFile(Uri.parse(rootDir).toFile()).listFiles()
                listFiles.forEach {
                    if (it.isDirectory) {
                        folders.add(FileInfo(it, MathUtils.getFileSize(it)))
                    } else {
                        files.add(FileInfo(it, MathUtils.getFileSize(it)))
                    }
                }
                sort()
            }

            selectionView.btnSendFiles.setOnClickListener {
                if (selectCount == 0) return@setOnClickListener
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
                        Toast.makeText(requireContext(), "${selected.size} files added to send queue", Toast.LENGTH_SHORT).show()
                    }
                    onFileRequestPrepareListener?.sendFiles(selected)
                }
            }
            selectionView.btnSelectAll.setOnClickListener {
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
            selectionView.btnSelectNon.setOnClickListener {
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
        }
        val model = activity?.run {
            ViewModelProvider(this).get(FilterDialogFragment.SharedViewModel::class.java)
        }
        model?.sortBy?.observe(requireActivity(), androidx.lifecycle.Observer {
            if (it.target == 4) {
                Logger.d("sort by files ${it.sortBy}")
                sortBy = it.sortBy
                sort()
            }
        })
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
            navController?.navigate(object : NavDirections {
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
            navController?.navigate(object : NavDirections {
                override fun getArguments(): Bundle {
                    return Bundle().apply {
                        putInt(FilterDialogFragment.PARAM_TARGET, 4)
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

    override fun onDestroy() {
        super.onDestroy()
        Logger.d("File fragment destroy $rootDir")
        fileInfos.clear()
        folders.clear()
        files.clear()
        System.gc()
    }

    private fun hideShowSendButton() {
        onNavigationVisibilityListener?.onNavVisibilityChange(selectCount == 0)
        selectionView.changeVisibility(selectCount)
    }

    override fun onFileDeselected(fileInfo: FileInfo) {
        selectCount--
        hideShowSendButton()
    }

    override fun onFileLongClickListener(fileInfo: FileInfo) {
        navController?.navigate(object : NavDirections {
            override fun getArguments(): Bundle {
                return Bundle().apply {
                    putString(FilePropertyDialogFragment.PARAM_FILE_ID, fileInfo.id)
                    putString(FilePropertyDialogFragment.PARAM_FILE_URI, fileInfo.uri.toString())
                }
            }

            override fun getActionId(): Int {
                return R.id.filePropertyDialogFragment
            }
        })
    }

    override fun onFileSelected(fileInfo: FileInfo) {
        selectCount++
        hideShowSendButton()
    }

    private fun sort() {
        CoroutineScope(IO).launch {
            withContext(Main) {
                loadingFile.visibility = View.VISIBLE
            }
            fileInfos.clear()
            sort(folders)
            sort(files)
            fileInfos.addAll(folders)
            fileInfos.addAll(files)
            withContext(Main) {
                adapter?.notifyDataSetChanged()
                loadingFile.visibility = View.GONE
                if (fileInfos.size == 0) {
                    emptyDirectory.visibility = View.VISIBLE
                } else {
                    emptyDirectory.visibility = View.GONE
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
}
package com.vkpapps.thunder.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.vkpapps.thunder.R
import com.vkpapps.thunder.interfaces.OnFileRequestPrepareListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.model.FileInfo
import com.vkpapps.thunder.model.RawRequestInfo
import com.vkpapps.thunder.ui.adapter.FileAdapter
import kotlinx.android.synthetic.main.fragment_file.*
import kotlinx.android.synthetic.main.selection_options.*
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
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    private var selectCount = 0
    private var title: String? = "default"

    private var rootDir: String = "/storage/emulated/0/"
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
        return inflater.inflate(R.layout.fragment_file, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // change title
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar
        if (supportActionBar != null) {
            supportActionBar.title = title
        }

        // show list and detail
        val adapter = FileAdapter(this, view)
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
        var fileInfos: ArrayList<FileInfo>? = null
        CoroutineScope(IO).launch {
            val listFiles = DocumentFile.fromFile(File(rootDir)).listFiles()
            fileInfos = ArrayList()
            val folder = ArrayList<FileInfo>()
            val file = ArrayList<FileInfo>()
            listFiles.forEach {
                if (it.isDirectory) {
                    folder.add(FileInfo(it))
                } else {
                    file.add(FileInfo(it))
                }
            }
            folder.sortBy { it.name }
            file.sortBy { it.name }
            fileInfos?.addAll(folder)
            fileInfos?.addAll(file)
            withContext(Main) {
                adapter.setFileInfos(fileInfos)
                if (fileInfos?.size == 0) {
                    emptyDirectory.visibility = View.VISIBLE
                } else {
                    emptyDirectory.visibility = View.GONE
                }
            }
        }

        btnSendFiles.setOnClickListener {
            if (selectCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                fileInfos?.forEach {
                    try {
                        if (it.isSelected) {
                            it.isSelected = false
                            selected.add(RawRequestInfo(
                                    it.name!!, it.source!!, it.type
                            ))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                selectCount = 0
                withContext(Main) {
                    adapter.notifyDataSetChanged()
                    hideShowSendButton()
                    Toast.makeText(requireContext(), "${selected.size} files added to send queue", Toast.LENGTH_SHORT).show()
                }
                onFileRequestPrepareListener?.sendFiles(selected)
            }
        }

        btnAll.setOnClickListener {
            CoroutineScope(IO).launch {
                selectCount = 0
                fileInfos?.forEach {
                    if (!it.isDirectory) {
                        it.isSelected = true
                        selectCount++
                    }
                }
                withContext(Main) {
                    adapter.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }
        btnNon.setOnClickListener {
            CoroutineScope(IO).launch {
                selectCount = 0
                fileInfos?.forEach {
                    if (!it.isDirectory) {
                        it.isSelected = false
                    }
                }
                withContext(Main) {
                    adapter.notifyDataSetChanged()
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

    private fun hideShowSendButton() {
        if (selectionSection.visibility == View.VISIBLE && selectCount > 0) {
            onNavigationVisibilityListener?.onNavVisibilityChange(false)
            return
        }
        if (selectCount == 0) {
            selectionSection.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_to_bottom)
            selectionSection.visibility = View.GONE
            onNavigationVisibilityListener?.onNavVisibilityChange(true)
        } else {
            selectionSection.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_bottom)
            selectionSection.visibility = View.VISIBLE
            onNavigationVisibilityListener?.onNavVisibilityChange(false)
        }
    }

    companion object {
        const val FILE_ROOT = "FILE_ROOT"
        const val FRAGMENT_TITLE = "FRAGMENT_TITLE"
    }

    override fun onFileDeselected(fileInfo: FileInfo) {
        selectCount--
        hideShowSendButton()
    }

    override fun onFileSelected(fileInfo: FileInfo) {
        selectCount++
        hideShowSendButton()
    }
}
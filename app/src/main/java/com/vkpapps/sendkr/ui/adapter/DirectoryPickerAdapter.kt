package com.vkpapps.sendkr.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.utils.StorageManager
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/***
 * @author VIJAY PATIDAR
 */
class DirectoryPickerAdapter(val currentSelection: AppCompatTextView) : RecyclerView.Adapter<DirectoryPickerAdapter.DirHolder>() {
    var dirSelected: DocumentFile? = null
    private var currentTree = ArrayList<DocumentFile>()
    val stack = Stack<ArrayList<DocumentFile>>().apply {
    }

    init {
        try {
            currentTree.add(DocumentFile.fromFile(StorageManager(App.context).internal))
            StorageManager(App.context).external?.run {
                currentTree.add(DocumentFile.fromFile(this))
            }
            stack.push(currentTree)
            displayCurrentPath()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirHolder {
        return DirHolder(LayoutInflater.from(parent.context).inflate(R.layout.dir_list_item, parent, false))
    }

    override fun onBindViewHolder(dirHolder: DirHolder, position: Int) {
        try {
            if (position == 0) {
                dirHolder.dirTitle.text = "..."
                dirHolder.itemView.setOnClickListener {
                    if (stack.size > 1) {
                        stack.pop()
                        currentTree = stack.peek()
                        notifyDataSetChanged()
                        dirSelected = dirSelected?.parentFile
                        displayCurrentPath()
                    }
                }
            } else {
                val file = currentTree[position - 1]
                dirHolder.dirTitle.text = file.name
                dirHolder.itemView.setOnClickListener {
                    dirSelected = file
                    val nextTree = ArrayList<DocumentFile>()
                    file.listFiles().forEach {
                        if (it.isDirectory && !it.name!!.startsWith("."))
                            nextTree.add(it)
                    }
                    nextTree.sortBy { it.name }
                    stack.push(nextTree)
                    currentTree = nextTree
                    notifyDataSetChanged()
                    displayCurrentPath()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayCurrentPath() {
        try {
            currentSelection.text = File(dirSelected!!.uri.toFile(), "SendKr").absolutePath
        } catch (e: java.lang.Exception) {
            currentSelection.text = StorageManager(currentSelection.context).downloadDir.absolutePath
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return currentTree.size + 1
    }

    class DirHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dirTitle: TextView = itemView.findViewById(R.id.dirName)
    }
}
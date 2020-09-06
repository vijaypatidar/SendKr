package com.vkpapps.sendkr.room.liveViewModel

import android.app.Application
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vkpapps.sendkr.model.FileInfo
import com.vkpapps.sendkr.ui.fragments.dialog.FilterDialogFragment
import com.vkpapps.sendkr.utils.StorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File

class QuickAccessViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        val documents = ArrayList<FileInfo>()
        val apks = ArrayList<FileInfo>()
        val zips = ArrayList<FileInfo>()
        var loading = false
        var sortBy = FilterDialogFragment.SORT_BY_NAME
        var external: String? = null
    }

    val documentsLiveData = MutableLiveData(documents)
    val apkLiveData = MutableLiveData(apks)
    val zipsLiveData = MutableLiveData(zips)

    private fun clearList() {
        documents.clear()
        apks.clear()
        zips.clear()
    }

    fun refreshData() {
        if (!loading) {
            loading = true
            CoroutineScope(IO).launch {
                clearList()
                scan(DocumentFile.fromFile(StorageManager.internal))
                try {
                    external?.run {
                        scan(DocumentFile.fromFile(File(this)))
                    }
                } catch (e: Exception) {

                }
                sortData()
                loading = false
            }
        }
    }

    private fun notifyDataChanged() {
        zipsLiveData.postValue(zips)
        apkLiveData.postValue(apks)
        documentsLiveData.postValue(documents)
    }

    private fun scan(file: DocumentFile) {
        try {
            if (file.isDirectory) {
                file.listFiles().forEach {
                    scan(it)
                }
            } else {
                file.name?.run {
                    when {
                        this.endsWith(".pdf", true) -> {
                            documents.add(FileInfo(file))
                        }
                        this.endsWith(".docx", true) -> {
                            documents.add(FileInfo(file))
                        }
                        this.endsWith(".doc", true) -> {
                            documents.add(FileInfo(file))
                        }
                        this.endsWith(".odt", true) -> {
                            documents.add(FileInfo(file))
                        }
                        this.endsWith(".xls", true) -> {
                            documents.add(FileInfo(file))
                        }
                        this.endsWith(".xlsx", true) -> {
                            documents.add(FileInfo(file))
                        }
                        this.endsWith(".ods", true) -> {
                            documents.add(FileInfo(file))
                        }
                        this.endsWith(".ppt", true) -> {
                            documents.add(FileInfo(file))
                        }
                        this.endsWith(".pptx", true) -> {
                            documents.add(FileInfo(file))
                        }
                        this.endsWith(".txt", true) -> {
                            documents.add(FileInfo(file))
                        }
                        this.endsWith(".apk", true) -> {
                            apks.add(FileInfo(file))
                        }
                        this.endsWith(".obb", true) -> {
                            apks.add(FileInfo(file))
                        }
                        this.endsWith(".zip", true) -> {
                            zips.add(FileInfo(file))
                        }
                        this.endsWith(".7z", true) -> {
                            zips.add(FileInfo(file))
                        }
                        this.endsWith(".tar", true) -> {
                            zips.add(FileInfo(file))
                        }
                        this.endsWith(".rar", true) -> {
                            zips.add(FileInfo(file))
                        }
                        else -> {

                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sortData() {
        CoroutineScope(IO).launch {
            sort(apks)
            sort(documents)
            sort(zips)
            notifyDataChanged()
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

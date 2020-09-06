package com.vkpapps.sendkr.ui.fragments.dialog

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import com.squareup.picasso.Picasso
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.utils.MathUtils
import com.vkpapps.sendkr.utils.MyThumbnailUtils
import kotlinx.android.synthetic.main.fragment_file_property_dialog.*
import java.util.*

class FilePropertyDialogFragment(
        private var uri: Uri?,
        private var id: String?,
        private var size: Long) : MyBottomSheetDialogFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_property_dialog, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d("file property dialog $uri")
        btnClose.setOnClickListener { dismiss() }
        try {
            val file = DocumentFile.fromFile(uri!!.toFile())
            fileTitle.text = file.name ?: ""
            filePath.text = uri!!.toFile().path
            fileSize.text = MathUtils.longToStringSize(size.toDouble())
            fileLastModified.text = Date(file.lastModified()).toString()
            fileMimeType.text = file.type ?: "Folder"
            val type = file.type
            if (type != null) {
                val res: Boolean = when {
                    type.startsWith("image") -> {
                        Picasso.get().load(uri).into(thumbnail)
                        true
                    }
                    type.startsWith("video") -> {
                        MyThumbnailUtils.loadVideoThumbnail(id!!, uri!!, thumbnail)
                        true
                    }
                    type.startsWith("audio") -> {
                        MyThumbnailUtils.loadAudioThumbnail(id!!, uri!!, thumbnail)
                        true
                    }
                    type == "application/vnd.android.package-archive" -> {
                        MyThumbnailUtils.loadThumbnail(id!!, uri!!, FileType.FILE_TYPE_APP, thumbnail)
                        true
                    }
                    else -> false
                }
                if (res) thumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
            } else {
                val fileCount = file.listFiles().size
                val text = "${fileSize.text} | " + if (fileCount > 0) "$fileCount files " else "empty folder"
                fileSize.text = text
                thumbnail.setImageResource(R.drawable.ic_folder)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "error occurred!", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            dismiss()
        }

    }
}
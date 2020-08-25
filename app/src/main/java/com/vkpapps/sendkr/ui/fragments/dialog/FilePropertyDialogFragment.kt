package com.vkpapps.sendkr.ui.fragments.dialog

import android.app.Dialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.utils.MathUtils
import com.vkpapps.sendkr.utils.MyThumbnailUtils
import kotlinx.android.synthetic.main.fragment_file_property_dialog.*
import java.util.*

class FilePropertyDialogFragment : BottomSheetDialogFragment() {

    private var uri: Uri? = null
    private var id: String? = null
    private var size: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().run {
            uri = Uri.parse(this.getString(PARAM_FILE_URI))
            id = getString(PARAM_FILE_ID)
            size = getString(PARAM_FILE_SIZE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_property_dialog, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            this.setOnShowListener {
                (it as BottomSheetDialog)
                        .findViewById<View>(R.id.design_bottom_sheet)
                        ?.apply {
                            setBackgroundColor(Color.TRANSPARENT)
                        }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d("file property dialog $uri")
        btnClose.setOnClickListener { dismiss() }
        try {
            val file = DocumentFile.fromFile(uri!!.toFile())
            fileTitle.text = file.name ?: ""
            filePath.text = uri!!.toFile().path
            fileSize.text = size ?: MathUtils.getFileDisplaySize(file)
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

    companion object {
        const val PARAM_FILE_URI = "PARAM_TARGET_FRAGMENT"
        const val PARAM_FILE_ID = "PARAM_FILE_ID"
        const val PARAM_FILE_SIZE = "PARAM_FILE_SIZE"
    }
}
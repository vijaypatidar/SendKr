package com.vkpapps.sendkr.ui.adapter

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.interfaces.OnFileStatusChangeListener
import com.vkpapps.sendkr.model.RequestInfo
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.model.constant.StatusType
import com.vkpapps.sendkr.ui.fragments.FileFragment
import com.vkpapps.sendkr.ui.views.HorizontalProgressBar
import com.vkpapps.sendkr.utils.MathUtils
import com.vkpapps.sendkr.utils.MyThumbnailUtils
import com.vkpapps.sendkr.utils.OpenUtils

class RequestAdapter(context: Context, private val onFileStatusChangeListener: OnFileStatusChangeListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var requestInfos: List<RequestInfo> = ArrayList()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        return requestInfos[position].fileType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DefaultRequestHolder(inflater.inflate(R.layout.default_request_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val requestInfo = requestInfos[position]
        if (holder is DefaultRequestHolder) {
            holder.name.text = requestInfo.name
            holder.size.text = MathUtils.longToStringSize(requestInfo.size.toDouble())
            updateStatus(holder, requestInfo)
            if (requestInfo.status != StatusType.STATUS_COMPLETED) {
                holder.status.setOnClickListener {
                    if (requestInfo.fileType == FileType.FILE_TYPE_FOLDER) {
                        Toast.makeText(it.context, "Operation not supported for folder at this time.", Toast.LENGTH_SHORT).show()
                    } else {
                        Logger.d("[Adapter][onBindViewHolder] status = ${requestInfo.status}")
                        when (requestInfo.status) {
                            StatusType.STATUS_PENDING -> {
                                requestInfo.status = StatusType.STATUS_PAUSE
                            }
                            StatusType.STATUS_ONGOING -> {
                                requestInfo.status = StatusType.STATUS_PAUSE
                            }
                            StatusType.STATUS_PAUSE -> {
                                requestInfo.status = StatusType.STATUS_PENDING
                            }
                            StatusType.STATUS_FAILED -> {
                                requestInfo.transferred = 0
                                requestInfo.status = StatusType.STATUS_RETRY
                            }
                        }
                        updateStatus(holder, requestInfo)
                        onFileStatusChangeListener.onStatusChange(requestInfo)
                    }
                }
            }
            if (requestInfo.status == StatusType.STATUS_COMPLETED) {
                holder.thumbnail.setOnClickListener { v: View ->
                    when (requestInfo.fileType) {
                        FileType.FILE_TYPE_FOLDER -> {
                            Navigation.findNavController(v).navigate(object : NavDirections {
                                override fun getActionId(): Int {
                                    return R.id.fileFragment
                                }

                                override fun getArguments(): Bundle {
                                    val bundle = Bundle()
                                    bundle.putString(FileFragment.FRAGMENT_TITLE, requestInfo.name)
                                    bundle.putString(FileFragment.FILE_ROOT, requestInfo.uri.toString())
                                    return bundle
                                }
                            })
                        }
                        else -> {
                            OpenUtils.open(requestInfo.fileType, v.context, Uri.parse(requestInfo.uri))
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return requestInfos.size
    }

    fun setRequestInfos(requestInfos: List<RequestInfo>) {
        this.requestInfos = requestInfos
        notifyDataSetChanged()
    }

    private fun updateStatus(holder: DefaultRequestHolder, requestInfo: RequestInfo) {
        when (requestInfo.status) {
            StatusType.STATUS_PENDING -> {
                holder.status.setImageResource(R.drawable.ic_pending)
                setProgress(holder.progress, requestInfo.transferred, requestInfo.size)
                setIconType(holder.thumbnail, requestInfo.fileType)
            }
            StatusType.STATUS_ONGOING -> {
                holder.status.setImageResource(R.drawable.ic_action_pause)
                setIconType(holder.thumbnail, requestInfo.fileType)
                setProgress(holder.progress, requestInfo.transferred, requestInfo.size)
            }
            StatusType.STATUS_COMPLETED -> {
                holder.status.setImageResource(R.drawable.ic_status_completed)
                holder.progress.setProgress(100f)
                try {
                    MyThumbnailUtils.loadThumbnail(requestInfo.rid, Uri.parse(requestInfo.uri), requestInfo.fileType, holder.thumbnail)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            StatusType.STATUS_FAILED -> {
                setIconType(holder.thumbnail, requestInfo.fileType)
                setProgress(holder.progress, requestInfo.transferred, requestInfo.size)
                holder.status.setImageResource(R.drawable.ic_action_retry)
            }
            StatusType.STATUS_PAUSE -> {
                setIconType(holder.thumbnail, requestInfo.fileType)
                holder.status.setImageResource(R.drawable.ic_resume)
                setProgress(holder.progress, requestInfo.transferred, requestInfo.size)
            }
        }
    }

    private fun setIconType(thumbnail: AppCompatImageView, type: Int) {
        when (type) {
            FileType.FILE_TYPE_APP -> thumbnail.setImageResource(R.drawable.ic_android)
            FileType.FILE_TYPE_MUSIC -> thumbnail.setImageResource(R.drawable.ic_music)
            FileType.FILE_TYPE_VIDEO -> thumbnail.setImageResource(R.drawable.ic_movie)
            FileType.FILE_TYPE_PHOTO -> thumbnail.setImageResource(R.drawable.ic_photo)
            FileType.FILE_TYPE_FOLDER -> thumbnail.setImageResource(R.drawable.ic_folder)
            else -> thumbnail.setImageResource(R.drawable.ic_file)
        }
    }

    class DefaultRequestHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progress: HorizontalProgressBar = itemView.findViewById(R.id.progress)
        val name: AppCompatTextView = itemView.findViewById(R.id.name)
        val size: AppCompatTextView = itemView.findViewById(R.id.size)
        val thumbnail: AppCompatImageView = itemView.findViewById(R.id.thumbnail)
        val status: AppCompatImageView = itemView.findViewById(R.id.status)
    }

    private fun setProgress(progressBar: HorizontalProgressBar, transferred: Long, size: Long) {
        try {
            val progress = MathUtils.roundTo((transferred * 100 / size).toDouble())
            progressBar.setProgress(progress.toFloat())
        } catch (e: Exception) {
        }
    }


}
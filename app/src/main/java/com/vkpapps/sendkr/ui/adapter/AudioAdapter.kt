package com.vkpapps.sendkr.ui.adapter

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.interfaces.OnMediaSelectListener
import com.vkpapps.sendkr.model.MediaInfo
import com.vkpapps.sendkr.ui.adapter.AudioAdapter.AudioViewHolder
import com.vkpapps.sendkr.utils.IntentUtils
import com.vkpapps.sendkr.utils.MyThumbnailUtils


/**
 * @author VIJAY PATIDAR
 */
class AudioAdapter(private val mediaInfos: MutableList<MediaInfo>, private val onMediaSelectListener: OnMediaSelectListener, context: Context) : RecyclerView.Adapter<AudioViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val myThumbnailUtils = MyThumbnailUtils

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val inflate = inflater.inflate(R.layout.audio_list_item, parent, false)
        return AudioViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioinfo = mediaInfos[position]

        holder.audioTitle.text = audioinfo.name
        holder.btnSelect.isChecked = audioinfo.isSelected

        val onClick = View.OnClickListener {
            audioinfo.isSelected = !audioinfo.isSelected
            holder.btnSelect.isChecked = audioinfo.isSelected
            if (audioinfo.isSelected) {
                onMediaSelectListener.onMediaSelected(audioinfo)
            } else {
                onMediaSelectListener.onMediaDeselected(audioinfo)
            }
        }
        holder.itemView.setOnClickListener(onClick)
        holder.btnSelect.setOnClickListener(onClick)
        holder.audioIcon.setOnClickListener {
            IntentUtils.startActionViewIntent(it.context, audioinfo.uri)
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                MediaScannerConnection.scanFile(it.context, arrayOf(audioinfo.uri.path), null) { _, uri ->
                    run {
                        val type = it.context.contentResolver.getType(uri)
                        Logger.d("file $uri type = $type")
                        intent.setDataAndType(uri, type)
                        it.context.startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(it.context, "error occurred", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
        holder.itemView.setOnLongClickListener {
            onMediaSelectListener.onMediaLongClickListener(audioinfo)
            true
        }
        myThumbnailUtils.loadAudioThumbnail(audioinfo.id, audioinfo.uri, holder.audioIcon)
        holder.audioIcon.adjustViewBounds = true
    }

    override fun getItemCount(): Int {
        return mediaInfos.size
    }

    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var audioTitle: TextView = itemView.findViewById(R.id.audio_title)
        var audioIcon: AppCompatImageView = itemView.findViewById(R.id.audio_icon)
        var btnSelect: RadioButton = itemView.findViewById(R.id.btnSelect)
    }
}
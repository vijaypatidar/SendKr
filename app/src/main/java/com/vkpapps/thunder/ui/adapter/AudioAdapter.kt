package com.vkpapps.thunder.ui.adapter

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
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.model.AudioInfo
import com.vkpapps.thunder.ui.adapter.AudioAdapter.AudioViewHolder
import com.vkpapps.thunder.utils.IntentUtils
import com.vkpapps.thunder.utils.MyThumbnailUtils


/**
 * @author VIJAY PATIDAR
 */
class AudioAdapter(private val audioInfos: MutableList<AudioInfo>, private val onAudioSelectedListener: OnAudioSelectedListener, context: Context) : RecyclerView.Adapter<AudioViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val myThumbnailUtils = MyThumbnailUtils

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val inflate = inflater.inflate(R.layout.audio_list_item, parent, false)
        return AudioViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioinfo = audioInfos[position]

        holder.audioTitle.text = audioinfo.name
        holder.btnSelect.isChecked = audioinfo.isSelected
        holder.btnSelect.setOnClickListener {
            audioinfo.isSelected = !audioinfo.isSelected
            holder.btnSelect.isChecked = audioinfo.isSelected
            if (audioinfo.isSelected) {
                onAudioSelectedListener.onAudioSelected(audioinfo)
            } else {
                onAudioSelectedListener.onAudioDeselected(audioinfo)
            }
        }
        holder.audioIcon.setOnClickListener {
            IntentUtils.startIntentToPlayAudio(it.context, audioinfo.uri)
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
            onAudioSelectedListener.onAudioLongClickListener(audioinfo)
            true
        }
        myThumbnailUtils.loadAudioThumbnail(audioinfo.id, audioinfo.uri, holder.audioIcon)
        holder.audioIcon.adjustViewBounds = true
    }

    override fun getItemCount(): Int {
        return audioInfos.size
    }

    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var audioTitle: TextView = itemView.findViewById(R.id.audio_title)
        var audioIcon: AppCompatImageView = itemView.findViewById(R.id.audio_icon)
        var btnSelect: RadioButton = itemView.findViewById(R.id.btnSelect)
    }

    interface OnAudioSelectedListener {
        fun onAudioLongClickListener(audioinfo: AudioInfo)
        fun onAudioSelected(audioMode: AudioInfo)
        fun onAudioDeselected(audioinfo: AudioInfo)
    }
}
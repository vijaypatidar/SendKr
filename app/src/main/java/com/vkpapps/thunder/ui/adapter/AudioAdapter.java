package com.vkpapps.thunder.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.AudioInfo;
import com.vkpapps.thunder.utils.AdsUtils;
import com.vkpapps.thunder.utils.MyThumbnailUtils;
import com.vkpapps.thunder.utils.StorageManager;

import java.io.File;
import java.util.List;

/**
 * @author VIJAY PATIDAR
 */
public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {
    private List<AudioInfo> audioInfos = null;
    private OnAudioSelectedListener onAudioSelectedListener;
    private File thumbnails;
    private LayoutInflater inflater;
    private MyThumbnailUtils myThumbnailUtils = MyThumbnailUtils.INSTANCE;

    public AudioAdapter(@NonNull OnAudioSelectedListener onAudioSelectedListener, @NonNull Context context) {
        this.onAudioSelectedListener = onAudioSelectedListener;
        thumbnails = new StorageManager(context).getThumbnails();
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate;
        if (viewType == 1) {
            inflate = inflater.inflate(R.layout.audio_list_item, parent, false);
        } else {
            inflate = inflater.inflate(R.layout.local_list_item_ad_view, parent, false);
        }
        return new AudioViewHolder(inflate);
    }

    @Override
    public int getItemViewType(int position) {
        return audioInfos.get(position) == null ? 0 : 1;
    }

    @Override
    public void onBindViewHolder(@NonNull final AudioViewHolder holder, int position) {
        final AudioInfo audioinfo = audioInfos.get(position);
        if (audioinfo == null) {
            AdView adView = (AdView) holder.itemView;
            AdsUtils.INSTANCE.getAdRequest(adView);
        } else {
            holder.audioTitle.setText(audioinfo.getName());
            holder.btnSelect.setChecked(audioinfo.isSelected());
            holder.btnSelect.setOnClickListener((v) -> {
                audioinfo.setSelected(!audioinfo.isSelected());
                holder.btnSelect.setChecked(audioinfo.isSelected());
                if (audioinfo.isSelected()) {
                    onAudioSelectedListener.onAudioSelected(audioinfo);
                } else {
                    onAudioSelectedListener.onAudioDeselected(audioinfo);
                }
            });

            File file = new File(thumbnails, audioinfo.getId());
            myThumbnailUtils.loadAudioThumbnail(file, audioinfo.getPath(), holder.audioIcon);
            holder.audioIcon.setAdjustViewBounds(true);
        }
    }

    @Override
    public int getItemCount() {
        return (audioInfos == null) ? 0 : audioInfos.size();
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {

        TextView audioTitle;
        AppCompatImageView audioIcon;
        RadioButton btnSelect;
        AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            audioIcon = itemView.findViewById(R.id.audio_icon);
            audioTitle = itemView.findViewById(R.id.audio_title);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }

    }

    public interface OnAudioSelectedListener {
        void onAudioSelected(AudioInfo audioMode);

        void onAudioDeselected(AudioInfo audioinfo);
    }

    public void setAudioInfos(List<AudioInfo> audioInfos) {
        this.audioInfos = audioInfos;
        notifyDataSetChanged();
    }
}

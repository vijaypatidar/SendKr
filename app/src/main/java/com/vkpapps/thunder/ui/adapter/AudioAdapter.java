package com.vkpapps.thunder.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;
import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.AudioInfo;
import com.vkpapps.thunder.utils.AdsUtils;
import com.vkpapps.thunder.utils.StorageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * @author VIJAY PATIDAR
 */
public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {
    private List<AudioInfo> audioInfos;
    private OnAudioSelectedListener onAudioSelectedListener;
    private File imageRoot;
    private LayoutInflater inflater;

    public AudioAdapter(List<AudioInfo> audioInfos, @NonNull OnAudioSelectedListener onAudioSelectedListener, @NonNull Context context) {
        this.audioInfos = audioInfos;
        this.onAudioSelectedListener = onAudioSelectedListener;
        imageRoot = new StorageManager(context).getImageDir();
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

            ImageView audioIcon = holder.audioIcon;
            File file = new File(imageRoot, audioinfo.getName().trim());
            if (!file.exists()) {
                try {
                    android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(audioinfo.getPath());
                    byte[] data = mmr.getEmbeddedPicture();
                    // convert the byte array to a bitmap
                    if (data != null) {
                        //destination for saving file
                        FileOutputStream fos = new FileOutputStream(file);
                        // decoding byte array to a bitmap
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (file.exists())
                Picasso.get().load(Uri.fromFile(file)).into(audioIcon);
            audioIcon.setAdjustViewBounds(true);
        }
    }

    @Override
    public int getItemCount() {
        return (audioInfos == null) ? 0 : audioInfos.size();
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {

        TextView audioTitle;
        ImageView audioIcon;
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
}

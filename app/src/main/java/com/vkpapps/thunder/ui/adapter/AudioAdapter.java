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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;
import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.AudioModel;
import com.vkpapps.thunder.utils.AdsUtils;
import com.vkpapps.thunder.utils.StorageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * @author VIJAY PATIDAR
 */
public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {
    private List<AudioModel> audioModels;
    private OnAudioSelectedListener onAudioSelectedListener;
    private File imageRoot;

    public AudioAdapter(List<AudioModel> audioModels, OnAudioSelectedListener onAudioSelectedListener, Context context) {
        this.audioModels = audioModels;
        this.onAudioSelectedListener = onAudioSelectedListener;
        imageRoot = new StorageManager(context).getImageDir();
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate;
        if (viewType == 1) {
            inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_list_item, parent, false);
        } else {
            inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_list_item_ad_view, parent, false);
        }
        return new AudioViewHolder(inflate);
    }

    @Override
    public int getItemViewType(int position) {
        return audioModels.get(position) == null ? 0 : 1;
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        AudioModel audioModel = audioModels.get(position);
        if (audioModel == null) {
            AdView adView = (AdView) holder.itemView;
            AdsUtils.INSTANCE.getAdRequest(adView);
        } else {
            holder.audioTitle.setText(audioModel.getName());
            holder.audioArtist.setText(audioModel.getArtist());
            holder.itemView.setOnClickListener(v -> onAudioSelectedListener.onAudioSelected(audioModel));
            holder.itemView.setOnLongClickListener(v -> {
                onAudioSelectedListener.onAudioLongSelected(audioModel);
                return true;
            });

            ImageView audioIcon = holder.audioIcon;
            File file = new File(imageRoot, audioModel.getName().trim());
            if (!file.exists()) {
                try {
                    android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(audioModel.getPath());
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
        return (audioModels == null) ? 0 : audioModels.size();
    }

    public interface OnAudioSelectedListener {
        void onAudioSelected(AudioModel audioMode);

        void onAudioLongSelected(AudioModel audioModel);
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {
        TextView audioTitle, audioArtist;
        ImageView audioIcon;

        AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            audioIcon = itemView.findViewById(R.id.audio_icon);
            audioTitle = itemView.findViewById(R.id.audio_title);
            audioArtist = itemView.findViewById(R.id.audio_artist);
        }
    }
}
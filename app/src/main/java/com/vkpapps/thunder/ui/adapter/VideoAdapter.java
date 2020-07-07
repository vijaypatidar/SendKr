package com.vkpapps.thunder.ui.adapter;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.CancellationSignal;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.VideoInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author VIJAY PATIDAR
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyHolder> {

    private List<VideoInfo> videoInfos;
    private View view;
    private OnVideoSelectListener onVideoSelectListener;

    public VideoAdapter(List<VideoInfo> videoInfos, View view, @NonNull OnVideoSelectListener onVideoSelectListener) {
        this.videoInfos = videoInfos;
        this.view = view;
        this.onVideoSelectListener = onVideoSelectListener;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_item, parent, false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        final VideoInfo videoInfo = videoInfos.get(position);
        final File file = new File(videoInfo.getPath());

        holder.btnSelected.setVisibility(videoInfo.isSelected() ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> {
            videoInfo.setSelected(!videoInfo.isSelected());
            holder.btnSelected.setVisibility(videoInfo.isSelected() ? View.VISIBLE : View.GONE);
            if (videoInfo.isSelected()) {
                onVideoSelectListener.onVideoSelected(videoInfo);
            } else {
                onVideoSelectListener.onVideoDeselected(videoInfo);
            }
        });
        if (file.exists()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                try {
                    Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(file, new Size(250, 250), new CancellationSignal());
                    holder.picture.setImageBitmap(videoThumbnail);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        holder.btnFullscreen.setOnClickListener(v -> Toast.makeText(v.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return (videoInfos == null) ? 0 : videoInfos.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView picture;
        private AppCompatImageView btnSelected;
        private AppCompatImageButton btnFullscreen;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture);
            btnFullscreen = itemView.findViewById(R.id.btnFullscreen);
            btnSelected = itemView.findViewById(R.id.btnSelect);
        }
    }

    public void notifyDataSetChangedAndHideIfNull() {
        if (videoInfos == null || videoInfos.size() == 0) {
            view.findViewById(R.id.emptyVideo).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.emptyVideo).setVisibility(View.GONE);
            notifyDataSetChanged();
        }
    }

    public interface OnVideoSelectListener {
        void onVideoSelected(@NonNull VideoInfo videoInfo);

        void onVideoDeselected(@NonNull VideoInfo videoInfo);
    }

}

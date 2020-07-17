package com.vkpapps.thunder.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.VideoInfo;
import com.vkpapps.thunder.utils.MyThumbnailUtils;
import com.vkpapps.thunder.utils.StorageManager;

import java.io.File;
import java.util.List;

/**
 * @author VIJAY PATIDAR
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyHolder> {

    private List<VideoInfo> videoInfos;
    private OnVideoSelectListener onVideoSelectListener;
    private MyThumbnailUtils myThumbnailUtils = MyThumbnailUtils.INSTANCE;
    private File thumbnails;

    public VideoAdapter(List<VideoInfo> videoInfos, View view, @NonNull OnVideoSelectListener onVideoSelectListener) {
        this.videoInfos = videoInfos;
        this.thumbnails = new StorageManager(view.getContext()).getThumbnails();
        this.onVideoSelectListener = onVideoSelectListener;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item, parent, false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        final VideoInfo videoInfo = videoInfos.get(position);
        final File file = new File(thumbnails, videoInfo.getId());

        holder.title.setText(videoInfo.getName());
        holder.size.setText(videoInfo.getSize());
        holder.btnSelected.setChecked(videoInfo.isSelected());
        holder.btnSelected.setOnClickListener(v -> {
            videoInfo.setSelected(!videoInfo.isSelected());
            holder.btnSelected.setChecked(videoInfo.isSelected());
            if (videoInfo.isSelected()) {
                onVideoSelectListener.onVideoSelected(videoInfo);
            } else {
                onVideoSelectListener.onVideoDeselected(videoInfo);
            }
        });
        myThumbnailUtils.loadVideoThumbnail(file, videoInfo.getPath(), holder.picture);
        holder.picture.setOnClickListener(v -> {

        });
    }

    @Override
    public int getItemCount() {
        return (videoInfos == null) ? 0 : videoInfos.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView picture;
        private RadioButton btnSelected;
        private AppCompatTextView title, size;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture);
            title = itemView.findViewById(R.id.title);
            size = itemView.findViewById(R.id.size);
            btnSelected = itemView.findViewById(R.id.btnSelect);
        }
    }


    public interface OnVideoSelectListener {
        void onVideoSelected(@NonNull VideoInfo videoInfo);

        void onVideoDeselected(@NonNull VideoInfo videoInfo);
    }

}

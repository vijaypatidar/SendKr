package com.vkpapps.thunder.ui.adapter;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.CancellationSignal;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.PhotoInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author VIJAY PATIDAR
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyHolder> {

    private List<PhotoInfo> photoInfos;
    private View view;

    public VideoAdapter(List<PhotoInfo> photoInfos, View view) {
        this.photoInfos = photoInfos;
        this.view = view;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_item, parent, false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder,final int position) {
        final PhotoInfo photoInfo = photoInfos.get(position);
        final File file = new File(photoInfo.getPath());

        holder.btnSelected.setChecked(photoInfo.isSelected());
        holder.btnSelected.setOnClickListener(v ->{
            photoInfo.setSelected(!photoInfo.isSelected());
            holder.btnSelected.setChecked(photoInfo.isSelected());
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


    }

    @Override
    public int getItemCount() {
        return (photoInfos == null) ? 0 : photoInfos.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView picture;
        private RadioButton btnSelected;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture);
            btnSelected = itemView.findViewById(R.id.btnSelect);
        }
    }

    public void notifyDataSetChangedAndHideIfNull() {
        if (photoInfos==null||photoInfos.size() == 0) {
            view.findViewById(R.id.emptyVideo).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.emptyVideo).setVisibility(View.GONE);
            notifyDataSetChanged();
        }
    }
}

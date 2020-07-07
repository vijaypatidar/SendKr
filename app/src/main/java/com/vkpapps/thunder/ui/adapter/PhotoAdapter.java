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

import com.squareup.picasso.Picasso;
import com.vkpapps.thunder.R;
import com.vkpapps.thunder.analitics.Logger;
import com.vkpapps.thunder.model.AppInfo;
import com.vkpapps.thunder.model.PhotoInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author VIJAY PATIDAR
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyHolder> {

    private List<PhotoInfo> photoInfos;
    private View view;

    public PhotoAdapter(List<PhotoInfo> photoInfos, View view) {
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
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final PhotoInfo photoInfo = photoInfos.get(position);
        final File file = new File(photoInfo.getPath());
        if (file.exists()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                try {
                    Bitmap thumbnail = ThumbnailUtils.createImageThumbnail(file, new Size(512, 512), new CancellationSignal());
                    holder.picture.setImageBitmap(thumbnail);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            Picasso.get().load(file).into(holder.picture);
        }
        holder.btnSelected.setOnClickListener((v) -> {
            photoInfo.setSelected(!photoInfo.isSelected());
            holder.btnSelected.setChecked(photoInfo.isSelected());
        });
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
            btnSelected= itemView.findViewById(R.id.btnSelect);
        }
    }

    public void notifyDataSetChangedAndHideIfNull() {
        if (photoInfos==null||photoInfos.size() == 0) {
            view.findViewById(R.id.emptyPhoto).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.emptyPhoto).setVisibility(View.GONE);
            notifyDataSetChanged();
        }
    }
}

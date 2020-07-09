package com.vkpapps.thunder.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.PhotoInfo;
import com.vkpapps.thunder.utils.MyThumbnailUtils;
import com.vkpapps.thunder.utils.StorageManager;

import java.io.File;
import java.util.List;

/**
 * @author VIJAY PATIDAR
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyHolder> {

    private List<PhotoInfo> photoInfos;
    private OnPhotoSelectListener onPhotoSelectListener;
    private MyThumbnailUtils myThumbnailUtils = MyThumbnailUtils.INSTANCE;
    private File thumbnails;

    private View view;

    public PhotoAdapter(List<PhotoInfo> photoInfos, @NonNull OnPhotoSelectListener onPhotoSelectListener, View view) {
        this.photoInfos = photoInfos;
        this.onPhotoSelectListener = onPhotoSelectListener;
        this.view = view;
        this.thumbnails = new StorageManager(view.getContext()).getThumbnails();
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
        final File file = new File(thumbnails, photoInfo.getId());

        holder.btnSelected.setVisibility(photoInfo.isSelected() ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> {
            photoInfo.setSelected(!photoInfo.isSelected());
            holder.btnSelected.setVisibility(photoInfo.isSelected() ? View.VISIBLE : View.GONE);
            if (photoInfo.isSelected()) {
                onPhotoSelectListener.onPhotoSelected(photoInfo);
            } else {
                onPhotoSelectListener.onPhotoDeselected(photoInfo);
            }
        });

        myThumbnailUtils.loadPhotoThumbnail(file, photoInfo.getPath());
        Picasso.get().load(file).into(holder.picture);

        holder.btnFullscreen.setOnClickListener(v -> Toast.makeText(v.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show());

    }

    @Override
    public int getItemCount() {
        return (photoInfos == null) ? 0 : photoInfos.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView picture;
        private AppCompatImageView btnSelected;
        private AppCompatImageButton btnFullscreen;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture);
            btnSelected = itemView.findViewById(R.id.btnSelect);
            btnFullscreen = itemView.findViewById(R.id.btnFullscreen);
        }
    }

    public interface OnPhotoSelectListener {

        void onPhotoSelected(@NonNull PhotoInfo photoInfo);

        void onPhotoDeselected(@NonNull PhotoInfo photoInfo);
    }

}

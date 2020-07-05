package com.vkpapps.thunder.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.PhotoInfo;

import java.io.File;
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
        File file = new File(photoInfo.getPath());
        if (file.exists()) {
            Picasso.get().load(file).into(holder.picture);
        }
    }

    @Override
    public int getItemCount() {
        return (photoInfos == null) ? 0 : photoInfos.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView picture;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture);
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

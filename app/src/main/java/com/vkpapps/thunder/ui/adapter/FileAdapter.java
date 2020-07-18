package com.vkpapps.thunder.ui.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.FileInfo;
import com.vkpapps.thunder.ui.fragments.FileFragment;
import com.vkpapps.thunder.utils.MyThumbnailUtils;
import com.vkpapps.thunder.utils.StorageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/***
 * @author VIJAY PATIDAR
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {
    private final List<FileInfo> fileInfos = new ArrayList<>();
    private OnFileSelectListener onFileSelectListener;
    private View view;
    private MyThumbnailUtils thumbnailUtils;
    private File thumnails;

    public FileAdapter(OnFileSelectListener onFileSelectListener, View view) {
        this.onFileSelectListener = onFileSelectListener;
        this.view = view;
        this.thumbnailUtils = MyThumbnailUtils.INSTANCE;
        this.thumnails = new StorageManager(view.getContext()).getThumbnails();
    }

    @Override
    public int getItemViewType(int position) {
        if (fileInfos.get(position).getFile().isDirectory()) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public FileAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item_folder, parent, false));
        } else {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item_file, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final FileAdapter.MyViewHolder holder, int position) {
        final FileInfo fileInfo = fileInfos.get(position);
        //common for both type
        holder.name.setText(fileInfo.getFile().getName());

        if (fileInfo.getFile().isDirectory()) {
            holder.info.setText(fileInfo.getFileCount() > 0 ? fileInfo.getFileCount() + " files" : "empty folder");
            holder.itemView.setOnClickListener(v -> {
                Navigation.findNavController(view).navigate(new NavDirections() {
                    @Override
                    public int getActionId() {
                        return R.id.fileFragment;
                    }

                    @NonNull
                    @Override
                    public Bundle getArguments() {
                        Bundle bundle = new Bundle();
                        bundle.putString(FileFragment.FILE_ROOT, fileInfo.getFile().getUri().getPath());
                        bundle.putString(FileFragment.FRAGMENT_TITLE, fileInfo.getName());
                        return bundle;
                    }
                });
            });
        } else {
            holder.info.setText(fileInfo.getSize());
            thumbnailUtils.loadThumbnail(new File(thumnails, fileInfo.getId()),
                    fileInfo.getSource(),
                    fileInfo.getType(),
                    holder.icon
            );
        }

        holder.btnSelected.setChecked(fileInfo.isSelected());
        holder.btnSelected.setOnClickListener(v -> {
            fileInfo.setSelected(!fileInfo.isSelected());
            if (fileInfo.isSelected()) {
                onFileSelectListener.onFileSelected(fileInfo);
            } else {
                onFileSelectListener.onFileDeselected(fileInfo);
            }
            holder.btnSelected.setChecked(fileInfo.isSelected());
        });
    }

    @Override
    public int getItemCount() {
        return fileInfos.size();
    }

    public void setFileInfos(List<FileInfo> fileInfos) {
        synchronized (this.fileInfos) {
            this.fileInfos.clear();
            this.fileInfos.addAll(fileInfos);
            notifyDataSetChanged();
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, info;
        AppCompatImageView icon;
        RadioButton btnSelected;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.fileName);
            info = itemView.findViewById(R.id.info);
            btnSelected = itemView.findViewById(R.id.btnSelect);
        }
    }

    public interface OnFileSelectListener {
        void onFileSelected(@NonNull FileInfo fileInfo);

        void onFileDeselected(@NonNull FileInfo fileInfo);
    }
}

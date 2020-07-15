package com.vkpapps.thunder.ui.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.documentfile.provider.DocumentFile;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.FileInfo;
import com.vkpapps.thunder.ui.fragments.FileFragment;

import java.util.ArrayList;
import java.util.List;

/***
 * @author VIJAY PATIDAR
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {
    private List<FileInfo> fileInfos = new ArrayList<>();
    private View view;

    public FileAdapter(DocumentFile file, View view) {
        for (DocumentFile f : file.listFiles()) {
            fileInfos.add(new FileInfo(f));
        }
        this.view = view;
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
                        return bundle;
                    }
                });
            });
        } else {
            holder.btnSelected.setChecked(fileInfo.isSelected());
            holder.btnSelected.setOnClickListener(v -> {
                fileInfo.setSelected(!fileInfo.isSelected());
            });
        }

    }

    @Override
    public int getItemCount() {
        return fileInfos == null ? 0 : fileInfos.size();
    }

    public void notifyDataSetChangedAndHideIfNull() {
        if (fileInfos == null || fileInfos.size() == 0) {
            view.findViewById(R.id.emptyDirectory).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.emptyDirectory).setVisibility(View.GONE);
            notifyDataSetChanged();
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        AppCompatImageView icon;
        RadioButton btnSelected;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.fileName);
            btnSelected = itemView.findViewById(R.id.btnSelect);
        }
    }
}

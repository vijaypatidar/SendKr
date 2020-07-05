package com.vkpapps.thunder.ui.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.ui.fragments.FileFragment;

import java.io.File;
/***
 * @author VIJAY PATIDAR
 */
public class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String[] files;
    private View view;
    public FileAdapter(File file, View view){
        this.files = file.list();
        this.view = view;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String file = files[position];
        if (holder instanceof MyViewHolder) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            File file1 = new File(file);

            myViewHolder.fileName.setText(file1.getName());
            if (file1.isDirectory()){
                myViewHolder.icon.setImageResource(R.drawable.ic_folder);
                myViewHolder.itemView.setOnClickListener(v -> {
                    Navigation.findNavController(view).navigate(new NavDirections() {
                        @Override
                        public int getActionId() {
                            return R.id.navigation_files;
                        }

                        @NonNull
                        @Override
                        public Bundle getArguments() {
                            Bundle bundle = new Bundle();
                            bundle.putString(FileFragment.FILE_ROOT,file);
                            return bundle;
                        }
                    });
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return files==null?0:files.length;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView fileName;
        AppCompatImageView icon;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            icon = itemView.findViewById(R.id.icon);
        }
    }

    public void notifyDataSetChangedAndHideIfNull() {
        if (files==null||files.length == 0) {
            view.findViewById(R.id.emptyDirectory).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.emptyDirectory).setVisibility(View.GONE);
            notifyDataSetChanged();
        }
    }
}

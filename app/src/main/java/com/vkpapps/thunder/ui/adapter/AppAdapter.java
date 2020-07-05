package com.vkpapps.thunder.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.analitics.Logger;
import com.vkpapps.thunder.model.AppInfo;
import com.vkpapps.thunder.utils.StorageManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/***
 * @author VIJAY PATIDAR
 */
public class AppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<AppInfo> appInfos;
    public AppAdapter(@NotNull ArrayList<AppInfo> appInfos) {
        this.appInfos = appInfos;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,final int position) {
        AppHolder appHolder = (AppHolder) holder;
        final AppInfo appInfo = appInfos.get(position);

        appHolder.appTitle.setText(appInfo.getName());
        appHolder.appIcon.setImageDrawable(appInfo.getIcon());
        appHolder.btnSelected.setSelected(appInfo.isSelected());

        appHolder.itemView.setOnClickListener(v -> {
            StorageManager storageManager = new StorageManager(v.getContext());
            storageManager.copyFile(new File(appInfo.getSource()), new File(storageManager.getDownloadDir(), appInfo.getName() + ".apk"),
                    source -> Logger.d("==================================================done"));
        });

        appHolder.btnSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appInfo.setSelected(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return appInfos==null?0:appInfos.size();
    }

    static class AppHolder extends RecyclerView.ViewHolder {
        TextView appTitle;
        AppCompatImageView appIcon;
        CheckBox btnSelected;

        public AppHolder(@NonNull View itemView) {
            super(itemView);
            appTitle = itemView.findViewById(R.id.appName);
            appIcon = itemView.findViewById(R.id.appIcon);
            btnSelected = itemView.findViewById(R.id.btnSelect);
        }
    }
}

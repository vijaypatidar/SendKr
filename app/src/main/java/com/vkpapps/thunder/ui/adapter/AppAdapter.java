package com.vkpapps.thunder.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.AppInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/***
 * @author VIJAY PATIDAR
 */
public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppHolder> {
    private OnAppSelectListener onAppSelectListener;
    private List<AppInfo> appInfos;

    public AppAdapter(@NotNull ArrayList<AppInfo> appInfos, @NonNull OnAppSelectListener onAppSelectListener) {
        this.appInfos = appInfos;
        this.onAppSelectListener = onAppSelectListener;
    }

    @NonNull
    @Override
    public AppAdapter.AppHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final AppAdapter.AppHolder appHolder, int position) {
        final AppInfo appInfo = appInfos.get(position);
        appHolder.appTitle.setText(appInfo.getName());
        appHolder.appIcon.setImageDrawable(appInfo.getIcon());
        appHolder.btnSelected.setChecked(appInfo.isSelected());
        appHolder.btnSelected.setOnClickListener((v) -> {
            appInfo.setSelected(!appInfo.isSelected());
            if (appInfo.isSelected()) {
                onAppSelectListener.onAppSelected(appInfo);
            } else {
                onAppSelectListener.onAppDeselected(appInfo);
            }
            appHolder.btnSelected.setChecked(appInfo.isSelected());
        });
    }

    @Override
    public int getItemCount() {
        return appInfos == null ? 0 : appInfos.size();
    }

    static class AppHolder extends RecyclerView.ViewHolder {
        TextView appTitle;
        AppCompatImageView appIcon;
        RadioButton btnSelected;

        public AppHolder(@NonNull View itemView) {
            super(itemView);
            appTitle = itemView.findViewById(R.id.appName);
            appIcon = itemView.findViewById(R.id.appIcon);
            btnSelected = itemView.findViewById(R.id.btnSelect);
        }
    }

    public interface OnAppSelectListener {
        void onAppSelected(@NonNull AppInfo appInfo);

        void onAppDeselected(@NonNull AppInfo appInfo);
    }
}

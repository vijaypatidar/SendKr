package com.vkpapps.thunder.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.HistoryInfo;
import com.vkpapps.thunder.utils.MyThumbnailUtils;
import com.vkpapps.thunder.utils.StorageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VIJAY PATIDAR
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private final List<HistoryInfo> historyInfos = new ArrayList<>();
    private File thumbnails;
    private OnHistorySelectListener onHistorySelectListener;
    private LayoutInflater inflater;
    private MyThumbnailUtils myThumbnailUtils = MyThumbnailUtils.INSTANCE;

    public HistoryAdapter(@NonNull Context context, @NonNull OnHistorySelectListener onHistorySelectListener) {
        thumbnails = new StorageManager(context).getThumbnails();
        inflater = LayoutInflater.from(context);
        this.onHistorySelectListener = onHistorySelectListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = inflater.inflate(R.layout.history_list_item, parent, false);
        return new HistoryViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryViewHolder holder, final int position) {
        final HistoryInfo historyInfo = historyInfos.get(position);
        holder.name.setText(historyInfo.getName());
        File icon = new File(thumbnails, historyInfo.getId());
        holder.btnSelect.setChecked(historyInfo.isSelected());
        holder.btnSelect.setOnClickListener(v -> {
            historyInfo.setSelected(!historyInfo.isSelected());
            holder.btnSelect.setChecked(historyInfo.isSelected());
            if (historyInfo.isSelected()) {
                onHistorySelectListener.onHistorySelected(historyInfo);
            } else {
                onHistorySelectListener.onHistoryDeselected(historyInfo);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            AlertDialog.Builder ab = new AlertDialog.Builder(v.getContext());
            ab.setTitle("Delete history");
            ab.setMessage("Remove " + historyInfo.name + " from history.");
            ab.create().show();
            return true;
        });
        myThumbnailUtils.loadThumbnail(icon, historyInfo.getSource(), historyInfo.getType(), holder.logo);
    }

    @Override
    public int getItemCount() {
        return historyInfos.size();
    }

    public void setHistoryInfos(List<HistoryInfo> historyInfos) {
        synchronized (this.historyInfos) {
            this.historyInfos.clear();
            this.historyInfos.addAll(historyInfos);
            notifyDataSetChanged();
        }
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        AppCompatImageView logo;
        RadioButton btnSelect;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            logo = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }

    }

    public interface OnHistorySelectListener {
        void onHistorySelected(@NonNull HistoryInfo historyInfo);

        void onHistoryDeselected(@NonNull HistoryInfo historyInfo);
    }

}

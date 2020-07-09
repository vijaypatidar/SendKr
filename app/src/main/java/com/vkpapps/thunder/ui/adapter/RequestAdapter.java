package com.vkpapps.thunder.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.model.FileType;
import com.vkpapps.thunder.model.RequestInfo;
import com.vkpapps.thunder.model.StatusType;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<RequestInfo> requestInfos = new ArrayList<>();
    private LayoutInflater inflater;
    private ThumbnailUtils thumbnailUtils;
    public RequestAdapter(@NonNull Context context) {
        this.inflater = LayoutInflater.from(context);
        thumbnailUtils = new ThumbnailUtils();
    }

    @Override
    public int getItemViewType(int position) {
        return requestInfos.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DefaultRequestHolder(inflater.inflate(R.layout.default_request_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final RequestInfo requestInfo = requestInfos.get(position);
        if (holder instanceof DefaultRequestHolder) {
            DefaultRequestHolder defaultRequestHolder = (DefaultRequestHolder) holder;
            defaultRequestHolder.name.setText(requestInfo.getName());
            setIconType(defaultRequestHolder.thumbnail, requestInfo.getType());
            updateStatus(defaultRequestHolder.status, requestInfo.getStatus());
        }
    }

    @Override
    public int getItemCount() {
        return requestInfos.size();
    }

    public void setRequestInfos(List<RequestInfo> requestInfos) {
        this.requestInfos = requestInfos;
        notifyDataSetChanged();
    }

    private void updateStatus(AppCompatTextView status, int statusCode) {
        switch (statusCode) {
            case StatusType.STATUS_PENDING:
                status.setText("PENDING");
                break;
            case StatusType.STATUS_ONGOING:
                status.setText("ONGOING");
                break;
            case StatusType.STATUS_COMPLETED:
                status.setText("COMPLETED");
                status.setTextColor(Color.GREEN);
                break;
            case StatusType.STATUS_FAILED:
                status.setText("FAILED");
                status.setTextColor(Color.RED);
                break;
        }
    }

    private void setIconType(AppCompatImageView thumbnail, int type) {
        switch (type) {
            case FileType.FILE_TYPE_APP:
                thumbnail.setImageResource(R.drawable.ic_apps);
                break;
            case FileType.FILE_TYPE_MUSIC:
                thumbnail.setImageResource(R.drawable.ic_default_audio_icon);
                break;
            case FileType.FILE_TYPE_VIDEO:
                thumbnail.setImageResource(R.drawable.ic_video);
                break;
        }
    }

    static public class DefaultRequestHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView name;
        private AppCompatImageView thumbnail;
        private AppCompatTextView status;

        public DefaultRequestHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            status = itemView.findViewById(R.id.status);
        }

    }
}

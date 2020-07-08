package com.vkpapps.thunder.ui.adapter;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<RequestInfo> requestInfos = new ArrayList<>();
    private LayoutInflater inflater;

    public RequestAdapter(@NonNull Context context) {
        this.inflater = LayoutInflater.from(context);
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
            switch (requestInfo.getType()) {
                case FileType.FILE_TYPE_APP:
                    defaultRequestHolder.thumbnail.setImageResource(R.drawable.ic_apps);
                    break;
                case FileType.FILE_TYPE_MUSIC:
                    defaultRequestHolder.thumbnail.setImageResource(R.drawable.ic_default_audio_icon);
                    break;
                case FileType.FILE_TYPE_VIDEO:
                    defaultRequestHolder.thumbnail.setImageResource(R.drawable.ic_video);
                    break;
            }
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

    static public class DefaultRequestHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView name;
        private AppCompatImageView thumbnail;

        public DefaultRequestHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            thumbnail = itemView.findViewById(R.id.thumbnail);
        }
    }

}

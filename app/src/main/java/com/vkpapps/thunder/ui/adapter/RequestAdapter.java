package com.vkpapps.thunder.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

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

    private final List<RequestInfo> requestInfos = new ArrayList<>();
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
            setIconType(defaultRequestHolder.thumbnail, requestInfo.getType());
            updateStatus(defaultRequestHolder.status, requestInfo.getStatus());
        }
    }

    @Override
    public int getItemCount() {
        return requestInfos.size();
    }

    public void setRequestInfos(List<RequestInfo> requestInfos) {
        synchronized (this.requestInfos) {
            this.requestInfos.clear();
            this.requestInfos.addAll(requestInfos);
            notifyDataSetChanged();
        }
    }

    private void updateStatus(final AppCompatImageView status, int statusCode) {
        switch (statusCode) {
            case StatusType.STATUS_PENDING:
                status.setImageResource(R.drawable.ic_pending);
                break;
            case StatusType.STATUS_ONGOING:
                status.setImageResource(R.drawable.ic_status_ongoing);
                RotateAnimation animation = new RotateAnimation(0, -360,
                        Animation.RELATIVE_TO_SELF, .5f,
                        Animation.RELATIVE_TO_SELF, .5f);
                animation.setDuration(1000);
                animation.setRepeatCount(Animation.INFINITE);
                status.setAnimation(animation);
                break;
            case StatusType.STATUS_COMPLETED:
                status.setImageResource(R.drawable.ic_status_completed);
                break;
            case StatusType.STATUS_FAILED:
                status.setImageResource(R.drawable.ic_status_failed);
                break;
        }
    }

    private void setIconType(AppCompatImageView thumbnail, int type) {
        switch (type) {
            case FileType.FILE_TYPE_APP:
                thumbnail.setImageResource(R.drawable.ic_android);
                break;
            case FileType.FILE_TYPE_MUSIC:
                thumbnail.setImageResource(R.drawable.ic_music);
                break;
            case FileType.FILE_TYPE_VIDEO:
                thumbnail.setImageResource(R.drawable.ic_movie);
                break;
            case FileType.FILE_TYPE_PHOTO:
                thumbnail.setImageResource(R.drawable.ic_photo);
                break;
            default:
                thumbnail.setImageResource(R.drawable.ic_file);
                break;
        }
    }

    static public class DefaultRequestHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView name;
        private AppCompatImageView thumbnail;
        private AppCompatImageView status;

        public DefaultRequestHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            status = itemView.findViewById(R.id.status);
        }
    }
}

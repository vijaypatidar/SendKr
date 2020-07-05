package com.vkpapps.thunder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vkpapps.thunder.model.FileRequest;
import com.vkpapps.thunder.service.FileService;

import static com.vkpapps.thunder.service.FileService.LAST_REQUEST;

/***
 * @author VIJAY PATIDAR
 * */
public class FileRequestReceiver extends BroadcastReceiver {

    private OnFileRequestReceiverListener onFileRequestReceiverListener;

    public FileRequestReceiver(OnFileRequestReceiverListener onFileRequestReceiverListener) {
        this.onFileRequestReceiverListener = onFileRequestReceiverListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String name = intent.getStringExtra(FileService.NAME);
        int type = intent.getIntExtra(FileService.FILE_TYPE, FileRequest.FILE_TYPE_MUSIC);
        if (action == null) return;
        switch (action) {
            case FileService.STATUS_SUCCESS:
                onFileRequestReceiverListener.onRequestSuccess(name, intent.getBooleanExtra(LAST_REQUEST, false), type);
                break;
            case FileService.STATUS_FAILED:
                onFileRequestReceiverListener.onRequestFailed(name, type);
                break;
            case FileService.REQUEST_ACCEPTED:
                String clientId = intent.getStringExtra(FileService.CLIENT_ID);
                boolean send = intent.getBooleanExtra(FileService.ACTION_SEND, false);
                onFileRequestReceiverListener.onRequestAccepted(name, send, clientId, type);
                break;
        }
    }

    public interface OnFileRequestReceiverListener {
        void onRequestFailed(String name, int type);

        void onRequestAccepted(String name, boolean send, String clientId, int type);

        void onRequestSuccess(String name, boolean isLast, int type);
    }

}

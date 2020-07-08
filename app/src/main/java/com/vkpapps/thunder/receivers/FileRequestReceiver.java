package com.vkpapps.thunder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vkpapps.thunder.service.FileService;

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
        String rid = intent.getStringExtra(FileService.PARAM_RID);
        if (action == null) return;
        switch (action) {
            case FileService.STATUS_SUCCESS:
                onFileRequestReceiverListener.onRequestSuccess(rid);
                break;
            case FileService.STATUS_FAILED:
                onFileRequestReceiverListener.onRequestFailed(rid);
                break;
            case FileService.REQUEST_ACCEPTED:
                String clientId = intent.getStringExtra(FileService.PARAM_CLIENT_ID);
                onFileRequestReceiverListener.onRequestAccepted(rid, clientId);
                break;
        }
    }

    public interface OnFileRequestReceiverListener {
        void onRequestFailed(String rid);

        void onRequestAccepted(String rid, String cid);

        void onRequestSuccess(String rid);
    }

}

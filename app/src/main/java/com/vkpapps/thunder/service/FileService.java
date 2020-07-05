package com.vkpapps.thunder.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.vkpapps.thunder.model.FileRequest;
import com.vkpapps.thunder.utils.StorageManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/***
 * @author VIJAY PATIDAR
 */
public class FileService extends IntentService {
    public static final String ACTION_SEND = "com.vkpapps.thunder.action.SEND";
    public static final String ACTION_RECEIVE = "com.vkpapps.thunder.action.RECEIVE";
    public static final String STATUS_SUCCESS = "com.vkpapps.thunder.action.SUCCESS";
    public static final String STATUS_FAILED = "com.vkpapps.thunder.action.FAILED";
    public static final String REQUEST_ACCEPTED = "com.vkpapps.thunder.action.ACCEPTED";

    public static final String NAME = "com.vkpapps.thunder.extra.NAME";
    public static final String CLIENT_ID = "com.vkpapps.thunder.extra.CLIENT_ID";
    private static final String IS_HOST = "com.vkpapps.thunder.extra.IS_HOST";
    public static final String LAST_REQUEST = "com.vkpapps.thunder.action.IS_LAST_REQUEST";
    public static final String FILE_TYPE = "com.vkpapps.thunder.action.FILE_TYPE";
    public static String HOST_ADDRESS;

    private File musicRoot;
    private File imageRoot;
    private LocalBroadcastManager localBroadcastManager;
    private static final int MAX_WAIT_TIME = 1800;
    private static final int PORT = 7511;

    public FileService() {
        super("FileService");
    }

    public static void startActionSend(Context context, String name, String clientId, boolean isHost, boolean isLast, int type) {
        Intent intent = new Intent(context, FileService.class);
        intent.setAction(ACTION_SEND);
        intent.putExtra(NAME, name);
        intent.putExtra(CLIENT_ID, clientId);
        intent.putExtra(IS_HOST, isHost);
        intent.putExtra(FILE_TYPE, type);
        intent.putExtra(LAST_REQUEST, isLast);
        context.startService(intent);
    }

    public static void startActionReceive(Context context, String name, String clientId, boolean isHost, int type) {
        Intent intent = new Intent(context, FileService.class);
        intent.setAction(ACTION_RECEIVE);
        intent.putExtra(NAME, name);
        intent.putExtra(CLIENT_ID, clientId);
        intent.putExtra(IS_HOST, isHost);
        intent.putExtra(FILE_TYPE, type);
        context.startService(intent);
    }


    private Socket getSocket(boolean isHost) throws IOException {
        Socket socket;
        if (isHost) {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                serverSocket.setSoTimeout(MAX_WAIT_TIME);
                socket = serverSocket.accept();
            }
        } else {
            socket = new Socket();
            socket.connect(new InetSocketAddress(HOST_ADDRESS, PORT), MAX_WAIT_TIME);
        }
        return socket;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        musicRoot = new StorageManager(this).getSongDir();
        imageRoot = new StorageManager(this).getImageDir();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final String name = intent.getStringExtra(NAME);
            final String clientId = intent.getStringExtra(CLIENT_ID);
            final boolean isHost = intent.getBooleanExtra(IS_HOST, false);
            final boolean isLast = intent.getBooleanExtra(LAST_REQUEST, false);
            final int type = intent.getIntExtra(FILE_TYPE, FileRequest.FILE_TYPE_MUSIC);
            if (ACTION_SEND.equals(action)) {
                handleActionSend(name, clientId, isHost, isLast, type);
            } else if (ACTION_RECEIVE.equals(action)) {
                handleActionReceive(name, clientId, isHost, type);
            }
        }
    }

    private void handleActionReceive(String name, String clientId, boolean isHost, int type) {
        try {
            onAccepted(name, clientId, false, type);
            Socket socket = getSocket(isHost);
            InputStream in = socket.getInputStream();
            File file = new File(type == FileRequest.FILE_TYPE_MUSIC ? musicRoot : new StorageManager(this).getProfiles(), name.trim());
            OutputStream out = new FileOutputStream(file);
            byte[] bytes = new byte[2 * 1024];
            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            in.close();
            out.flush();
            out.close();
            socket.close();
            onSuccess(name, false, type);
            saveCover(name, type);
        } catch (IOException e) {
            onFailed(name, type);
            e.printStackTrace();
        }
    }

    private void handleActionSend(String name, String clientId, boolean isHost, boolean isLast, int type) {
        try {
            onAccepted(name, clientId, true, type);
            File file = new File(type == FileRequest.FILE_TYPE_MUSIC ? musicRoot : new StorageManager(this).getProfiles(), name.trim());
            Socket socket = getSocket(isHost);
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = socket.getOutputStream();
            byte[] bytes = new byte[2 * 1024];
            int count;
            while ((count = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, count);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            socket.close();
            onSuccess(name, isLast, type);
        } catch (IOException e) {
            onFailed(name, type);
            e.printStackTrace();
        }
    }

    private void onSuccess(String name, boolean isLast, int type) {
        Intent intent = new Intent(STATUS_SUCCESS);
        intent.putExtra(NAME, name);
        intent.putExtra(LAST_REQUEST, isLast);
        intent.putExtra(FILE_TYPE, type);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void onFailed(String name, int type) {
        Intent intent = new Intent(STATUS_FAILED);
        intent.putExtra(NAME, name);
        intent.putExtra(FILE_TYPE, type);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void onAccepted(String name, String clientID, boolean send, int type) {
        Intent intent = new Intent(REQUEST_ACCEPTED);
        intent.putExtra(NAME, name);
        intent.putExtra(ACTION_SEND, send);
        intent.putExtra(FILE_TYPE, type);
        intent.putExtra(CLIENT_ID, clientID);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void saveCover(String name, int type) {
        if (FileRequest.FILE_TYPE_MUSIC == type) {
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(new File(musicRoot, name).getAbsolutePath());

                byte[] data = mmr.getEmbeddedPicture();

                if (data != null) {
                    //destination for saving file
                    File file = new File(imageRoot, name);
                    FileOutputStream fos = new FileOutputStream(file);
                    // decoding byte array to a bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

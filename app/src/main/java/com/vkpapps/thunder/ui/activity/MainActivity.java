package com.vkpapps.thunder.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vkpapps.thunder.App;
import com.vkpapps.thunder.R;
import com.vkpapps.thunder.analitics.Logger;
import com.vkpapps.thunder.connection.ClientHelper;
import com.vkpapps.thunder.connection.ServerHelper;
import com.vkpapps.thunder.interfaces.OnClientConnectionStateListener;
import com.vkpapps.thunder.interfaces.OnFileRequestListener;
import com.vkpapps.thunder.interfaces.OnFileRequestPrepareListener;
import com.vkpapps.thunder.interfaces.OnFragmentAttachStatusListener;
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener;
import com.vkpapps.thunder.interfaces.OnUserListRequestListener;
import com.vkpapps.thunder.interfaces.OnUsersUpdateListener;
import com.vkpapps.thunder.model.FileRequest;
import com.vkpapps.thunder.model.User;
import com.vkpapps.thunder.receivers.FileRequestReceiver;
import com.vkpapps.thunder.service.FileService;
import com.vkpapps.thunder.ui.dialog.PrivacyDialog;
import com.vkpapps.thunder.ui.fragments.DashboardFragment;
import com.vkpapps.thunder.ui.fragments.destinations.FragmentDestinationListener;
import com.vkpapps.thunder.utils.IPManager;
import com.vkpapps.thunder.utils.UpdateManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author VIJAY PATIDAR
 */
public class MainActivity extends AppCompatActivity implements  OnNavigationVisibilityListener,
        OnUserListRequestListener, OnFragmentAttachStatusListener, OnFileRequestListener,OnFileRequestPrepareListener,
        FileRequestReceiver.OnFileRequestReceiverListener, OnClientConnectionStateListener {
    private ServerHelper serverHelper;
    private ClientHelper clientHelper;
    private boolean isHost;
    private User user;
    private FileRequestReceiver requestReceiver;
    private NavController navController;
    private OnUsersUpdateListener onUsersUpdateListener;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.nav_view);
        user = App.getUser();
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_app)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener(new FragmentDestinationListener(this));

        init();
        getChoice();
        new UpdateManager(true).checkForUpdate(true, this);

        // check for policy accepted or not
        new PrivacyDialog(this).isPolicyAccepted();
    }

    private void init() {
    }


    private void getChoice() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.choice_alert_dialog, null);
        ab.setView(view);
        ab.setCancelable(false);
        AlertDialog alertDialog = ab.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        view.findViewById(R.id.btnCreateParty).setOnClickListener(v -> {
            setup(true);
            alertDialog.cancel();
        });
        view.findViewById(R.id.btnJoinParty).setOnClickListener(v -> {
            setup(false);
            alertDialog.cancel();
        });

    }

    private void setup(boolean host) {
        isHost = host;
        if (isHost) {
            serverHelper = new ServerHelper(this, user, this);
            serverHelper.start();
            setupReceiver();
        } else {
            new Thread(() -> {
                Socket socket = new Socket();
                try {
                    IPManager ipManager = new IPManager(this);
                    String address = ipManager.hostIp();
                    Logger.d("setup: connection address " + address);
                    FileService.HOST_ADDRESS = address.substring(0, address.lastIndexOf(".") + 1) + "1";
                    socket.connect(new InetSocketAddress(FileService.HOST_ADDRESS, 1203), 5000);
                    clientHelper = new ClientHelper(socket, this, user, this);
                    clientHelper.start();
                    setupReceiver();
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        androidx.appcompat.app.AlertDialog.Builder ab = new androidx.appcompat.app.AlertDialog.Builder(this);
                        ab.setTitle("No host found!");
                        ab.setMessage("There is no host on this wifi");
                        ab.setCancelable(false);
                        ab.setPositiveButton("retry", (dialog, which) -> setup(false));
                        ab.setNegativeButton("Create group", (dialog, which) -> getChoice());
                        ab.create().show();
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }


    @Override
    public void onNavVisibilityChange(boolean visible) {
        if ((navView.getVisibility() == View.VISIBLE) == visible) return;
        Animation animation = AnimationUtils.loadAnimation(this,
                visible ? R.anim.slide_in_from_bottom : R.anim.slide_out_to_bottom
        );
        navView.setAnimation(animation);
        navView.setVisibility(visible?View.VISIBLE:View.GONE);
    }

    @Override
    public void onDownloadRequest(@NotNull String name, @NotNull String id, int type) {
        // only host wil response this method
        if (isHost) {
            // prepare file receive from client
            FileService.startActionReceive(this, name, id, true, type);
            // prepare send request for all other client except the sender of that file
            ArrayList<ClientHelper> clientHelpers = serverHelper.getClientHelpers();
            int N = clientHelpers.size() - 1;
            for (int i = 0; i <= N; i++) {
                ClientHelper chr = clientHelpers.get(i);
                String cid = chr.getUser().getUserId();
                if (!cid.equals(id)) {
                    FileService.startActionSend(this, name, chr.getUser().getUserId(), isHost, i == N, type);
                }
            }
        }
    }

    public void onUploadRequestAccepted(@NotNull String name, @NotNull String id, int type) {
        // receiver requested file or sent by host itself
        Logger.d("======================= "+name+" type - "+type);
        FileService.startActionReceive(this, name, id, isHost, type);
    }

    public void onUploadRequest(@NotNull String name, @NotNull String id, int type) {
        // only host wil response this method
        if (isHost) {
            FileService.startActionSend(this, name, id, true, true, type);
        }
    }

    public void onDownloadRequestAccepted(@NotNull String name, @NotNull String id, int type) {
        //only client need to handle this , not for host
        // send requested file to client sent request
        Logger.d("======================= download req "+name+" type - "+type);
        FileService.startActionSend(this, name, id, isHost, false, type);
    }


    @Override
    public void onRequestFailed(String name, int type) {
        Logger.d("onRequestFailed: " + name + "  type " + type);
    }

    @Override
    public void onRequestAccepted(String name, boolean send, String clientId, int type) {
        Logger.d("onRequestAccepted: " + name + "  " + send);
        FileRequest fileRequest = new FileRequest(send ? FileRequest.UPLOAD_REQUEST_CONFIRM : FileRequest.DOWNLOAD_REQUEST_CONFIRM, name, user.getUserId(), type);
        serverHelper.sendCommandToOnly(fileRequest, clientId);
    }

    @Override
    public void onRequestSuccess(String name, boolean isLastRequest, int type) {
        Logger.d("onRequestSuccess: " + name + "   " + isLastRequest);
        if (type == FileRequest.FILE_TYPE_MUSIC) {

        } else if (type == FileRequest.FILE_TYPE_PROFILE_PIC && onUsersUpdateListener != null) {
            onUsersUpdateListener.onUserUpdated();
        }

    }

    @Override
    public void onClientConnected(@NotNull ClientHelper clientHelper) {
        if (isHost) {
            if (onUsersUpdateListener != null) {
                runOnUiThread(() -> onUsersUpdateListener.onUserUpdated());
            }
            // host user send his/her profile to client
            FileService.startActionSend(this, user.getUserId(), clientHelper.getUser().getUserId(), true, true, FileRequest.FILE_TYPE_PROFILE_PIC);
        } else {
            clientHelper.write(new FileRequest(FileRequest.DOWNLOAD_REQUEST, user.getUserId(), user.getUserId(), FileRequest.FILE_TYPE_PROFILE_PIC));
        }
    }

    @Override
    public void onClientDisconnected(@NotNull ClientHelper clientHelper) {
        //prompt client when disconnect to a party to create or rejoin the party
        if (!isHost) {
            runOnUiThread(this::getChoice);
        } else if (onUsersUpdateListener != null) {
            runOnUiThread(() -> onUsersUpdateListener.onUserUpdated());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (R.id.menu_share == item.getItemId()) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Sound Booster in a free android app for playing music on multiple devices simultaneously to make the sound louder" +
                    ".\nDownload the app now and make party with friends any where any time without mobile data usage. " +
                    "\nhttps://vkp.page.link/soundbooster";
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Sound Booster");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else if (item.getItemId() == R.id.menu_about) {
            navController.navigate(new NavDirections() {
                @Override
                public int getActionId() {
                    return R.id.action_navigation_home_to_aboutFragment;
                }

                @NonNull
                @Override
                public Bundle getArguments() {
                    return null;
                }
            });
        }else if (item.getItemId() == R.id.navigation_setting) {
            navController.navigate(new NavDirections() {
                @Override
                public int getActionId() {
                    return R.id.action_navigation_home_to_navigation_setting;
                }

                @NonNull
                @Override
                public Bundle getArguments() {
                    return null;
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupReceiver() {
        LocalBroadcastManager instance = LocalBroadcastManager.getInstance(this);
        if (requestReceiver != null)
            instance.unregisterReceiver(requestReceiver);
        requestReceiver = new FileRequestReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FileService.STATUS_FAILED);
        intentFilter.addAction(FileService.STATUS_SUCCESS);
        if (isHost) {
            intentFilter.addAction(FileService.REQUEST_ACCEPTED);
        }
        instance.registerReceiver(requestReceiver, intentFilter);
    }

    @NotNull
    @Override
    public List<ClientHelper> onRequestUsers() {
        if (serverHelper != null) {
            return serverHelper.getClientHelpers();
        }
        return new ArrayList<>(Collections.singleton(clientHelper));
    }

    @Override
    public void onFragmentAttached(@NotNull Fragment fragment) {
        if (fragment instanceof DashboardFragment) {
            onUsersUpdateListener = (OnUsersUpdateListener) fragment;
        }
    }

    @Override
    public void onFragmentDetached(@NotNull Fragment fragment) {
        if (fragment instanceof DashboardFragment) {
            onUsersUpdateListener = null;
        }
    }

    @Override
    public void onBackPressed() {
        NavDestination currentDestination = navController.getCurrentDestination();
        if (currentDestination != null && currentDestination.getId() == R.id.navigation_home) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are you want to exit?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                if (isHost) {
                    serverHelper.shutDown();
                } else {
                    clientHelper.shutDown();
                }
                finish();
            });
            builder.setNegativeButton("No", null);
            builder.create().show();
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // request made by local Song fragment
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            navController.navigate(R.id.navigation_home);
        } else {
            Toast.makeText(this, "Storage permission required!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void send(Object o) {
        if (isHost) {
            serverHelper.broadcast(o);
        } else {
            clientHelper.write(o);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void sendFiles(List<FileRequest> requests, int type) {
        for (FileRequest fileRequest:requests){
            if (isHost) {
            ArrayList<ClientHelper> clientHelpers = serverHelper.getClientHelpers();
            int N = clientHelpers.size() - 1;
            for (int i = 0; i <= N; i++) {
                ClientHelper chr = clientHelpers.get(i);
                FileService.startActionSend(this, fileRequest.getFileName(), chr.getUser().getUserId(), isHost, i == N, FileRequest.FILE_TYPE_MUSIC);
            }
        } else {
//            clientHelper.write(new ControlFile(ControlFile.DOWNLOAD_REQUEST, audio.getName(), user.getUserId(), ControlFile.FILE_TYPE_MUSIC));
        }
        }
    }
}

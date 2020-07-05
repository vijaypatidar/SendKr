package com.vkpapps.thunder.utils;

import android.app.Activity;
import android.content.IntentSender;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.vkpapps.thunder.R;


public class UpdateManager {
    private boolean auto;

    public UpdateManager(boolean auto) {
        this.auto = auto;
    }

    public void checkForUpdate(boolean isCheck, Activity context) {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (isCheck) {

                    Snackbar snackbar = Snackbar.make(
                            context.findViewById(R.id.container),
                            "update available",
                            Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Update", view -> checkForUpdate(false, context));
                    snackbar.setActionTextColor(
                            context.getResources().getColor(R.color.colorAccent));
                    snackbar.show();
                } else {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                context,
                                123);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                if (!auto) {
                    Toast.makeText(context, "Already updated", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}

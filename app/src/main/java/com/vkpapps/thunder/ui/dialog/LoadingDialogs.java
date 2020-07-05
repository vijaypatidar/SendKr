package com.vkpapps.thunder.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.vkpapps.thunder.R;
/***
 * @author VIJAY PATIDAR
 */
public class LoadingDialogs {
    private Context context;

    public LoadingDialogs(@NonNull Context context) {
        this.context = context;
    }

    public AlertDialog getLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(LayoutInflater.from(context).inflate(R.layout.loading_dialog,null));
        builder.setCancelable(false);
        return builder.create();
    }
}

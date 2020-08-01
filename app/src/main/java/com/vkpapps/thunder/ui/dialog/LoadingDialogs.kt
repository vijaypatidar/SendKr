package com.vkpapps.thunder.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.vkpapps.thunder.R

/***
 * @author VIJAY PATIDAR
 */
class LoadingDialogs(private val context: Context) {
    val loadingDialog: AlertDialog
        get() {
            val builder = AlertDialog.Builder(context)
            builder.setView(LayoutInflater.from(context).inflate(R.layout.loading_dialog, null))
            builder.setCancelable(false)
            return builder.create()
        }

}
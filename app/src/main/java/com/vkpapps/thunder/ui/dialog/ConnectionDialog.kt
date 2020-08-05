package com.vkpapps.thunder.ui.dialog

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import com.vkpapps.thunder.R

class ConnectionDialog(private val activity: Activity) {
    fun createHotspot() {
        val ab = AlertDialog.Builder(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.alert_create_hotspot, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        view.findViewById<View>(R.id.btnOk).setOnClickListener {
            alertDialog.cancel()
        }
    }

    fun joinHotspotFailed(retry: View.OnClickListener, createGroup: View.OnClickListener) {
        val ab = AlertDialog.Builder(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.alert_join_hotspot_failed, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        view.findViewById<View>(R.id.btnRetry).setOnClickListener {
            alertDialog.cancel()
            retry.onClick(it)
        }
        view.findViewById<View>(R.id.btnCreateGroup).setOnClickListener {
            alertDialog.cancel()
            createGroup.onClick(it)
        }
    }
}
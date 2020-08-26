package com.vkpapps.sendkr.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.squareup.picasso.Picasso
import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.interfaces.OnFailureListener
import com.vkpapps.sendkr.interfaces.OnSuccessListener
import com.vkpapps.sendkr.utils.StorageManager
import java.io.File


class DialogsUtils(private val context: Context) {
    fun choice(createGroup: View.OnClickListener, joinGroup: View.OnClickListener) {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.choice_alert_dialog, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        view.findViewById<View>(R.id.btnCreateGroup).setOnClickListener {
            createGroup.onClick(it)
            alertDialog.cancel()
        }
        view.findViewById<View>(R.id.btnJoinGroup).setOnClickListener {
            joinGroup.onClick(it)
            alertDialog.cancel()
        }
    }

    fun createHotspot() {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_create_hotspot, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        view.findViewById<View>(R.id.btnOk).setOnClickListener {
            alertDialog.cancel()
        }
    }

    fun displayQRCode() {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_display_qr_code, null)
        ab.setView(view)
        ab.setCancelable(true)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        val barCodeImage = view.findViewById<AppCompatImageView>(R.id.barCodeImage)
        val code = File(StorageManager(App.context).userDir, "code.png")
        val picasso = Picasso.get()
        picasso.invalidate(code)
        picasso.load(code).fit().into(barCodeImage)
    }

    fun joinHotspotFailed(retry: View.OnClickListener, createGroup: View.OnClickListener) {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_join_hotspot_failed, null)
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

    fun clearHistoryDialog(clear: View.OnClickListener, cancel: View.OnClickListener?) {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_clear_all_history, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        view.findViewById<View>(R.id.btnClear).setOnClickListener {
            clear.onClick(it)
            alertDialog.cancel()
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            cancel?.onClick(it)
            alertDialog.cancel()
        }
    }

    fun clearSelectedHistoryDialog(message: String, clear: View.OnClickListener) {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_clear_selected_history, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        view.findViewById<AppCompatTextView>(R.id.message).text = message
        view.findViewById<View>(R.id.btnClear).setOnClickListener {
            clear.onClick(it)
            alertDialog.cancel()
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            alertDialog.cancel()
        }
    }

    fun exitAppAlert(exit: View.OnClickListener, cancel: View.OnClickListener?) {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_exit_app, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        view.findViewById<View>(R.id.btnExit).setOnClickListener {
            exit.onClick(it)
            alertDialog.cancel()
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            cancel?.onClick(it)
            alertDialog.cancel()
        }
    }

    fun waitingForReceiver(count: Int) {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_waiting_for_receiver, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        view.findViewById<AppCompatTextView>(R.id.message).text = String.format(context.getString(R.string.waiting_for_receiver_detail), count)
        view.findViewById<View>(R.id.btnOk).setOnClickListener {
            alertDialog.cancel()
        }
    }

    fun alertCameraPermissionRequire(ask: View.OnClickListener, cancel: View.OnClickListener?) {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_camera_permission_need, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        view.findViewById<View>(R.id.btnAsk).setOnClickListener {
            alertDialog.cancel()
            ask.onClick(it)
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            alertDialog.cancel()
            cancel?.onClick(it)
        }
    }

    fun alertGpsProviderRequire(): AlertDialog {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_gps_provider_require, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.findViewById<View>(R.id.btnEnable).setOnClickListener {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
            alertDialog.cancel()
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            alertDialog.cancel()
        }
        return alertDialog
    }

    fun alertEnableWifi(onSuccessListener: OnSuccessListener<String>, onFailureListener: OnFailureListener<String>): AlertDialog {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_enable_wifi, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.findViewById<View>(R.id.btnEnable).setOnClickListener {
            onSuccessListener.onSuccess("enable wifi")
            alertDialog.cancel()
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            onFailureListener.onFailure("close this")
            alertDialog.cancel()
        }
        return alertDialog
    }

    fun alertDisableHotspot(onSuccessListener: OnSuccessListener<String>, onFailureListener: OnFailureListener<String>): AlertDialog {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_disable_hotspot, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.findViewById<View>(R.id.btnTurnOff).setOnClickListener {
            onSuccessListener.onSuccess("disable hotspot")
            alertDialog.cancel()
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            onFailureListener.onFailure("close")
            alertDialog.cancel()
        }
        return alertDialog
    }

    fun alertDisableWifi(onSuccessListener: OnSuccessListener<String>, onFailureListener: OnFailureListener<String>): AlertDialog {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_disable_wifi, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.findViewById<View>(R.id.btnTurnOff).setOnClickListener {
            onSuccessListener.onSuccess("disable wifi")
            alertDialog.cancel()
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            onFailureListener.onFailure("close")
            alertDialog.cancel()
        }
        return alertDialog
    }

    fun alertLoadingDialog(): AlertDialog {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_please_wait, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return alertDialog
    }

    fun closeGroup(close: View.OnClickListener, cancel: View.OnClickListener?) {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_close_group, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        view.findViewById<View>(R.id.btnClose).setOnClickListener {
            close.onClick(it)
            alertDialog.cancel()
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            cancel?.onClick(it)
            alertDialog.cancel()
        }
    }

}
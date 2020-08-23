package com.vkpapps.thunder.ui.dialog

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
import com.vkpapps.thunder.App
import com.vkpapps.thunder.R
import com.vkpapps.thunder.interfaces.OnFailureListener
import com.vkpapps.thunder.interfaces.OnSuccessListener
import com.vkpapps.thunder.utils.StorageManager
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
        Picasso.get().load(File(StorageManager(App.context).userDir, "code.png")).into(barCodeImage)
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

    fun alertGpsProviderRequire() {
        AlertDialog.Builder(context).apply {
            setTitle("GPS Requires")
            setMessage("GPS provider is disabled,please enable it to continue.")
            setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        }.show()
    }

    fun alertEnableWifi(onSuccessListener: OnSuccessListener<String>, onFailureListener: OnFailureListener<String>) {
        AlertDialog.Builder(context).apply {
            setTitle("Enable Wi-Fi")
            setMessage("Wi-Fi is disabled,please enable it to continue.")
            setPositiveButton("enable") { _, _ ->
                onSuccessListener.onSuccess("enable wifi")
            }
            setNegativeButton("Cancel") { dialog, _ ->
                onFailureListener.onFailure("close this")
                dialog.cancel()
            }
        }.show()
    }


}
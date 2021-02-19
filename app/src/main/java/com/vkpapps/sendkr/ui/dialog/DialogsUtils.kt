package com.vkpapps.sendkr.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.interfaces.OnFailureListener
import com.vkpapps.sendkr.interfaces.OnSuccessListener
import com.vkpapps.sendkr.ui.adapter.DirectoryPickerAdapter
import com.vkpapps.sendkr.ui.views.MyAlertView
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
            alertDialog.dismiss()
        }
        view.findViewById<View>(R.id.btnJoinGroup).setOnClickListener {
            joinGroup.onClick(it)
            alertDialog.dismiss()
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
        val code = File(StorageManager.userDir, "code.png")
        val picasso = Picasso.get()
        picasso.invalidate(code)
        picasso.load(code).fit().into(barCodeImage)
    }

    fun joinHotspotFailed(retry: View.OnClickListener, createGroup: View.OnClickListener) {
        val myAlertView = MyAlertView(context)
        val ab = AlertDialog.Builder(context)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(myAlertView) {
            setFeatureImage(R.drawable.ic_baseline_wifi)
            setTitle(R.string.failed_to_join_group)
            setFeatureImageSize(100, 100)
            setDescription(R.string.alert_dialog_connection_failed_detail)
            setPositiveButton(R.string.retry) {
                retry.onClick(it)
                alertDialog.dismiss()
            }
            setNegativeButton(R.string.create_group) {
                createGroup.onClick(it)
                alertDialog.dismiss()
            }
        }
    }

    fun clearHistoryDialog(clear: View.OnClickListener, cancel: View.OnClickListener?) {
        val myAlertView = MyAlertView(context)
        val ab = AlertDialog.Builder(context)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        myAlertView.setTitle(R.string.clear_transferring_history)
        myAlertView.setDescription(R.string.clear_transferring_history_detail)
        myAlertView.setFeatureImage(R.drawable.ic_clear_history)
        myAlertView.setPositiveButton(R.string.clear) {
            clear.onClick(it)
            alertDialog.dismiss()
        }
        myAlertView.setNegativeButton(R.string.cancel) {
            cancel?.onClick(it)
            alertDialog.dismiss()
        }
    }

    fun selectDir(onSelect: OnSuccessListener<DocumentFile>, cancel: View.OnClickListener?) {
        val ab = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_picker_directory, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        val directoryPickerAdapter = DirectoryPickerAdapter(view.findViewById(R.id.currentDir))
        val dirListView = view.findViewById<RecyclerView>(R.id.dirList)
        dirListView.layoutManager = LinearLayoutManager(context)
        dirListView.adapter = directoryPickerAdapter
        view.findViewById<View>(R.id.btnChoose).setOnClickListener {
            directoryPickerAdapter.dirSelected?.run {
                if (this.canWrite()) {
                    onSelect.onSuccess(this)
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(it.context, "Invalid path", Toast.LENGTH_SHORT).show()
                }
            }
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            cancel?.onClick(it)
            alertDialog.dismiss()
        }
    }

    fun clearSelectedHistoryDialog(message: String, clear: View.OnClickListener) {
        val myAlertView = MyAlertView(context)
        val ab = AlertDialog.Builder(context)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        myAlertView.setTitle(R.string.clear_transferring_history)
        myAlertView.setDescription(message)
        myAlertView.setFeatureImage(R.drawable.ic_clear_history)
        myAlertView.setPositiveButton(R.string.clear) {
            clear.onClick(it)
            alertDialog.dismiss()
        }
        myAlertView.setNegativeButton(R.string.cancel) {
            alertDialog.dismiss()
        }
    }

    fun exitAppAlert(exit: View.OnClickListener, cancel: View.OnClickListener?) {
        val myAlertView = MyAlertView(context)
        val ab = AlertDialog.Builder(context)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        myAlertView.setTitle(R.string.exit_app)
        myAlertView.setDescription(R.string.exit_app_message)
        myAlertView.setPositiveButton(R.string.exit) {
            exit.onClick(it)
            alertDialog.dismiss()
        }
        myAlertView.setNegativeButton(R.string.cancel) {
            cancel?.onClick(it)
            alertDialog.dismiss()
        }
    }

    fun waitingForReceiver(count: Int) {
        Logger.d("[DialogUtils][waitingForReceiver]")
        val myAlertView = MyAlertView(context)
        val ab = AlertDialog.Builder(context)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        with(myAlertView) {
            setTitle(R.string.waiting_for_receiver)
            setDescription(String.format(context.getString(R.string.waiting_for_receiver_detail), count))
            setPositiveButton(R.string.ok) {
                alertDialog.dismiss()
            }
        }
    }

    fun alertCameraPermissionRequire(ask: View.OnClickListener, cancel: View.OnClickListener?) {
        val myAlertView = MyAlertView(context)
        val ab = AlertDialog.Builder(context)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        with(myAlertView) {
            setDescription(R.string.camera_permission_required_description)
            setPositiveButton(R.string.ask) {
                alertDialog.dismiss()
                ask.onClick(it)
            }
            setNegativeButton(R.string.cancel) {
                alertDialog.dismiss()
                cancel?.onClick(it)
            }
        }
    }

    fun alertGpsProviderRequire(): AlertDialog {
        val myAlertView = MyAlertView(context)
        val ab = AlertDialog.Builder(context)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(myAlertView) {
            setTitle(R.string.enable_gps)
            setDescription(R.string.enable_gps_description)
            setFeatureImage(R.drawable.ic_location_on)
            setFeatureImageSize(100, 100)
            setPositiveButton(R.string.enable) {
                alertDialog.dismiss()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
                alertDialog.cancel()
            }
            setNegativeButton(R.string.cancel) {
                alertDialog.dismiss()
            }

        }
        return alertDialog
    }

    fun alertEnableWifi(onSuccessListener: OnSuccessListener<String>, onFailureListener: OnFailureListener<String>): AlertDialog {
        val myAlertView = MyAlertView(context)
        val ab = AlertDialog.Builder(context)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(myAlertView) {
            setFeatureImage(R.drawable.ic_baseline_wifi)
            setTitle(R.string.enable_wi_fi)
            setFeatureImageSize(100, 100)
            setDescription(R.string.enable_wifi_description)
            setPositiveButton(R.string.enable) {
                alertDialog.dismiss()
                onSuccessListener.onSuccess("enable wifi")
                alertDialog.cancel()
            }
            setNegativeButton(R.string.cancel) {
                alertDialog.dismiss()
                onFailureListener.onFailure("close this")
                alertDialog.cancel()
            }
        }
        return alertDialog
    }

    fun alertDisableHotspot(onSuccessListener: OnSuccessListener<String>, onFailureListener: OnFailureListener<String>): AlertDialog {
        val myAlertView = MyAlertView(context)
        val ab = AlertDialog.Builder(context)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(myAlertView) {
            setTitle(R.string.turn_off_hotspot)
            setFeatureImage(R.drawable.ic_portable_wifi_off)
            setFeatureImageSize(100, 100)
            setDescription(R.string.turn_off_hotspot_description)
            setPositiveButton(R.string.turn_off) {
                onSuccessListener.onSuccess("disable hotspot")
                alertDialog.cancel()
            }
            setNegativeButton(R.string.cancel) {
                onFailureListener.onFailure("close")
                alertDialog.cancel()
            }
        }
        return alertDialog
    }

    fun alertDisableWifi(onSuccessListener: OnSuccessListener<String>, onFailureListener: OnFailureListener<String>): AlertDialog {
        val myAlertView = MyAlertView(context)
        val ab = AlertDialog.Builder(context)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(myAlertView) {
            setTitle(R.string.disable_wi_fi)
            setFeatureImage(R.drawable.ic_wifi_off)
            setFeatureImageSize(100, 100)
            setDescription(R.string.disable_wifi_description)
            setPositiveButton(R.string.disable) {
                onSuccessListener.onSuccess("disable wifi")
                alertDialog.cancel()
            }
            setNegativeButton(R.string.cancel) {
                onFailureListener.onFailure("close")
                alertDialog.cancel()
            }
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
        val myAlertView = MyAlertView(context)
        val ab = AlertDialog.Builder(context)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(myAlertView) {
            setTitle(R.string.close_leave_group)
            setFeatureImage(R.drawable.ic_warning_image)
            setDescription(R.string.close_group_description)
            setPositiveButton(R.string.Continue) {
                close.onClick(it)
                alertDialog.dismiss()
            }
            setNegativeButton(R.string.cancel) {
                cancel?.onClick(it)
                alertDialog.dismiss()
            }
        }
        alertDialog.show()
    }

}
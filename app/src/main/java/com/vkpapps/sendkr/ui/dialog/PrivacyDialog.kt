package com.vkpapps.sendkr.ui.dialog

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.SpannableString
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.ui.views.MyAlertView
import com.vkpapps.sendkr.utils.KeyValue

/***
 * @author VIJAY PATIDAR
 */
class PrivacyDialog(private val activity: Activity) {
    val isPolicyAccepted: Boolean
        get() {
            if (KeyValue(activity).policy) {
                return true
            } else {
                promptUser()
            }
            return false
        }

    fun promptUser() {
        val myAlertView = MyAlertView(activity)
        val ab = AlertDialog.Builder(activity)
        ab.setView(myAlertView)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        myAlertView.setTitle(R.string.privacy_policy)
        myAlertView.setFeatureImage(R.drawable.ic_policy)
        myAlertView.setFeatureImageSize(100, 100)
        val description = SpannableString(activity.getString(R.string.accept_policy))
        myAlertView.setDescription(description)
        myAlertView.setPositiveButton(R.string.i_agree) {
            KeyValue(activity).policy = true
            alertDialog.dismiss()
        }
        myAlertView.setNegativeButton(R.string.cancel) {
            KeyValue(activity).policy = false
            activity.finish()
            alertDialog.dismiss()
        }
    }

}
package com.vkpapps.sendkr.ui.dialog

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.vkpapps.sendkr.R
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
        val builder = AlertDialog.Builder(activity)
        val view = activity.layoutInflater.inflate(R.layout.dialog_accept_policy, null, false)
        builder.setView(view)
        builder.setCancelable(false)
        val create = builder.create()
        create.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        create.show()
        view.findViewById<TextView>(R.id.message).movementMethod = LinkMovementMethod.getInstance()
        view.findViewById<TextView>(R.id.btnCancel).setOnClickListener {
            KeyValue(activity).policy = false
            activity.finish()
        }
        view.findViewById<TextView>(R.id.btnAgree).setOnClickListener {
            KeyValue(activity).policy = true
            create.hide()
            create.dismiss()
        }
    }

}
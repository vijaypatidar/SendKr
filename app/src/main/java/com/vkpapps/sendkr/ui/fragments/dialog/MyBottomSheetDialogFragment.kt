package com.vkpapps.sendkr.ui.fragments.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vkpapps.sendkr.R

/**
 * @author VIJAY PATIDAR
 *
 * abstract class for creating transparent BottomSheetDialogFragment
 */
abstract class MyBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            this.setOnShowListener {
                (it as BottomSheetDialog)
                        .findViewById<View>(R.id.design_bottom_sheet)
                        ?.apply {
                            setBackgroundColor(Color.TRANSPARENT)
                        }
            }
        }
    }
}
package com.vkpapps.sendkr.ui.fragments.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vkpapps.sendkr.R
import kotlinx.android.synthetic.main.fragment_filter_dialog.*

class FilterDialogFragment(private val sortBy: Int, private val onFilterListener: OnFilterListener) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter_dialog, container, false)
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //set radio button to current state
        when (sortBy) {
            SORT_BY_NAME -> sortByNameAToZ.isChecked = true
            SORT_BY_NAME_Z_TO_A -> sortByNameZToA.isChecked = true
            SORT_BY_LATEST_FIRST -> sortByLatest.isChecked = true
            SORT_BY_OLDEST_FIRST -> sortByOldest.isChecked = true
            SORT_BY_SIZE_ASC -> sortBySize.isChecked = true
            SORT_BY_SIZE_DSC -> sortBySizeDsc.isChecked = true
        }

        btnApply.setOnClickListener {
            val sortBy = when {
                sortByNameZToA.isChecked -> SORT_BY_NAME_Z_TO_A
                sortByLatest.isChecked -> SORT_BY_LATEST_FIRST
                sortByOldest.isChecked -> SORT_BY_OLDEST_FIRST
                sortBySize.isChecked -> SORT_BY_SIZE_ASC
                sortBySizeDsc.isChecked -> SORT_BY_SIZE_DSC
                else -> SORT_BY_NAME
            }
            onFilterListener.onFilterBy(sortBy)
            dismiss()
        }

    }

    companion object {
        const val SORT_BY_NAME = 0
        const val SORT_BY_NAME_Z_TO_A = 1
        const val SORT_BY_LATEST_FIRST = 2
        const val SORT_BY_OLDEST_FIRST = 3
        const val SORT_BY_SIZE_ASC = 4
        const val SORT_BY_SIZE_DSC = 5
    }

    interface OnFilterListener {
        fun onFilterBy(sortBy: Int)
    }
}
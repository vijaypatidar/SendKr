package com.vkpapps.thunder.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vkpapps.thunder.R
import kotlinx.android.synthetic.main.fragment_filter_dialog.*

class FilterDialogFragment : BottomSheetDialogFragment() {

    private var target = -1
    private var sortBy = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            target = this.getInt(PARAM_TARGET, -1)
            sortBy = this.getInt(PARAM_CURRENT_SORT_BY, 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val model = activity?.run {
            ViewModelProvider(this).get(SharedViewModel::class.java)
        }

        //set radio button to current state
        when (sortBy) {
            SORT_BY_NAME -> sortByNameAToZ.isChecked = true
            SORT_BY_NAME_Z_TO_A -> sortByNameZToA.isChecked = true
            SORT_BY_LATEST_FIRST -> sortByLatest.isChecked = true
            SORT_BY_OLDEST_FIRST -> sortByOldest.isChecked = true
        }

        btnApply.setOnClickListener {
            model?.run {
                val sortBy = when {
                    sortByNameZToA.isChecked -> SORT_BY_NAME_Z_TO_A
                    sortByLatest.isChecked -> SORT_BY_LATEST_FIRST
                    sortByOldest.isChecked -> SORT_BY_OLDEST_FIRST
                    else -> SORT_BY_NAME
                }
                model.select(SharedModel(sortBy, target))
            }
            dismiss()
        }

    }

    companion object {
        const val PARAM_TARGET = "PARAM_TARGET_FRAGMENT"
        const val PARAM_CURRENT_SORT_BY = "PARAM_CURRENT_SORT_BY"
        const val SORT_BY_NAME = 0
        const val SORT_BY_NAME_Z_TO_A = 1
        const val SORT_BY_LATEST_FIRST = 2
        const val SORT_BY_OLDEST_FIRST = 3
    }

    data class SharedModel(val sortBy: Int, val target: Int)

    class SharedViewModel : ViewModel() {
        val sortBy = MutableLiveData<SharedModel>(SharedModel(SORT_BY_NAME, 0))

        fun select(sharedModel: SharedModel) {
            sortBy.value = sharedModel
        }
    }
}
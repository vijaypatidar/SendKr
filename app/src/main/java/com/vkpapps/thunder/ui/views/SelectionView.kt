package com.vkpapps.thunder.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger

class SelectionView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val btnSelectAll: AppCompatImageButton
    val btnSendFiles: AppCompatImageButton
    val btnSelectNon: AppCompatImageButton

    init {
        val inflate: View = View.inflate(context, R.layout.selection_options, this)
        btnSelectAll = inflate.findViewById(R.id.btnAll)
        btnSelectNon = inflate.findViewById(R.id.btnNon)
        btnSendFiles = inflate.findViewById(R.id.btnSendFiles)
        visibility = View.GONE
    }

    fun changeVisibility(selectedCount: Int) {
        Logger.d("changeVisibility selectedCount = $selectedCount")
        val show = selectedCount != 0
        if (show && visibility == View.VISIBLE) return
        if (selectedCount == 0) {
            animation = AnimationUtils.loadAnimation(context, R.anim.fragment_fade_exit)
            visibility = View.GONE
        } else {
            animation = AnimationUtils.loadAnimation(context, R.anim.fragment_fade_enter)
            visibility = View.VISIBLE
        }
    }
}
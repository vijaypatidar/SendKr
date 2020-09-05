package com.vkpapps.sendkr.ui.views

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.vkpapps.sendkr.R

class MyAlertView(context: Context) : FrameLayout(context) {
    private val btnPositive: AppCompatButton
    private val btnNegative: AppCompatButton
    private val titleView: AppCompatTextView
    private val description: AppCompatTextView
    private val featureImage: AppCompatImageView

    init {
        val inflate: View = View.inflate(context, R.layout.alert_view, this)
        titleView = inflate.findViewById(R.id.title)
        btnPositive = inflate.findViewById(R.id.btnPositive)
        btnNegative = inflate.findViewById(R.id.btnNegative)
        description = inflate.findViewById(R.id.description)
        featureImage = inflate.findViewById(R.id.featureImage)
    }

    fun setPositiveButton(title: String, onClick: OnClickListener?) {
        btnPositive.text = title
        btnPositive.setOnClickListener(onClick)
    }

    fun setPositiveButton(id: Int, onClick: OnClickListener?) {
        setPositiveButton(context.getString(id), onClick)
    }

    fun setNegativeButton(title: String, onClick: OnClickListener?) {
        btnNegative.text = title
        btnNegative.setOnClickListener(onClick)
        btnNegative.visibility = View.VISIBLE
    }

    fun setNegativeButton(id: Int, onClick: OnClickListener?) {
        setNegativeButton(context.getString(id), onClick)
    }

    fun setTitle(title: String) {
        titleView.text = title
        titleView.visibility = VISIBLE
    }

    fun setTitle(id: Int) {
        setTitle(context.getString(id))
    }

    fun setDescription(text: String) {
        description.text = text
        description.visibility = View.VISIBLE
    }

    fun setDescription(id: Int) {
        setDescription(context.getString(id))
    }

    fun setFeatureImage(id: Int) {
        featureImage.setImageResource(id)
        featureImage.visibility = View.VISIBLE
    }

    fun setFeatureImageSize(width: Int, height: Int) {
        val density = featureImage.layoutParams.height / 200//convert to dp as the initial or default height of imageView value is set to 200dp in xml file
        featureImage.layoutParams.height = height * density
        featureImage.layoutParams.width = width * density
    }

    fun setDescription(text: SpannableString) {
        description.visibility = VISIBLE
        description.movementMethod = LinkMovementMethod.getInstance()
        description.setText(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text.toString(), Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(text.toString())
        }, TextView.BufferType.SPANNABLE)
    }
}
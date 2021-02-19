package com.vkpapps.sendkr.ui.activity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView.ScaleType
import androidx.documentfile.provider.DocumentFile
import com.vkpapps.sendkr.App.Companion.user
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.ui.activity.base.MyAppCompatActivity
import com.vkpapps.sendkr.utils.BitmapUtils
import com.vkpapps.sendkr.utils.UserUtils
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileActivity : MyAppCompatActivity() {
    private var picChange = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.run {
            this.setDisplayHomeAsUpEnabled(true)
            this.elevation = 0f
        }

        userName.setText(user.name)
        if (user.profileByteArray.isNotEmpty()) {
            userPic.setImageBitmap(BitmapUtils.byteArrayToBitmap(user.profileByteArray))
        }

        userPic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {

            }
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), 1)
        }

        btnSave.setOnClickListener { v: View ->
            val name = userName.text.toString().trim { it <= ' ' }
            if (name.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    user.name = name
                    if (picChange) {
                        user.profileByteArray = BitmapUtils.viewToByteArray(userPic)
                    }
                    UserUtils(v.context).setUser(user)
                    withContext(Dispatchers.Main) {
                        userName.clearFocus()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            } else {
                userName.error = "name required!"
            }
        }

        btnMale.setOnClickListener {
            userPic.setImageResource(R.drawable.ic_male_avatar)
            picChange = true
        }
        btnFemale.setOnClickListener {
            userPic.setImageResource(R.drawable.ic_female_avatar)
            picChange = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val selectedImageUri = data.data
                if (null != selectedImageUri) {
                    userPic.setImageURI(selectedImageUri)
                    picChange = true

                    //crop if possible
                    crop(selectedImageUri)
                }
            }
        } else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    val selectedBitmap: Bitmap = data.extras?.getParcelable("data")!!
                    userPic.setImageBitmap(selectedBitmap)
                    userPic.scaleType = ScaleType.FIT_XY
                    picChange = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun crop(selectedImageUri: Uri) {
        try {
            val cropIntent = Intent("com.android.camera.action.CROP")
            val contentUri: Uri = DocumentFile.fromSingleUri(this, selectedImageUri)!!.uri
            cropIntent.setDataAndType(contentUri, "image/*")
            cropIntent.putExtra("crop", "true")
            cropIntent.putExtra("aspectX", 1)
            cropIntent.putExtra("aspectY", 1)
            cropIntent.putExtra("outputX", 280)
            cropIntent.putExtra("outputY", 280)
            cropIntent.putExtra("return-data", true)
            startActivityForResult(Intent.createChooser(cropIntent, "crop with"), 2)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}
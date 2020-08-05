package com.vkpapps.thunder.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.vkpapps.thunder.App
import com.vkpapps.thunder.R
import com.vkpapps.thunder.model.User
import com.vkpapps.thunder.utils.BitmapUtils
import com.vkpapps.thunder.utils.UserUtils
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {
    private var user: User = App.user
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
            userPic.setImageBitmap(BitmapUtils().byteArrayToBitmap(user.profileByteArray))
        }

        userPic.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), 1)
        }

        btnSave.setOnClickListener { v: View ->
            val name = userName.text.toString().trim { it <= ' ' }
            if (name.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    user.name = name
                    if (picChange) {
                        user.profileByteArray = BitmapUtils().viewToByteArray(userPic)
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
                }
            }
        }
    }
}
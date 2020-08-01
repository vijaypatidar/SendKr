package com.vkpapps.thunder.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.vkpapps.thunder.App
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.loader.PrepareDb
import com.vkpapps.thunder.utils.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/***
 * @author VIJAY PATIDAR
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val arrayList = ArrayList<Parcelable>()
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM))?.let {
                    arrayList.add(it)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let { list ->
                    arrayList.addAll(list)
                }
            }
        }
        loadData(arrayList)
    }

    private fun loadData(list: ArrayList<Parcelable>) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("shared", list)
        Logger.d("App =   ${App.databasePrepared}")
        if (PermissionUtils.checkStoragePermission(this) && !App.databasePrepared) {
            CoroutineScope(IO).launch {
                PrepareDb().prepareAll()
                App.databasePrepared = true
                withContext(Main) {
                    startActivity(intent)
                    finish()
                }
            }
        } else {
            startActivity(intent)
            finish()
        }
    }
}
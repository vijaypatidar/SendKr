package com.vkpapps.thunder.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.vkpapps.thunder.R
import kotlinx.android.synthetic.main.activity_create_access_point.*

class CreateAccessPointActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_access_point)
        supportActionBar?.run {
            this.setDisplayHomeAsUpEnabled(true)
            this.elevation = 0f
        }

//        WifiApUtils.turnOnHotspot(this, OnSuccessListener {
//            setResult(RESULT_OK)
//            finish()
//        }, OnFailureListener {
//            Toast.makeText(this,"failed tp create ap",Toast.LENGTH_SHORT).show()
//        })

        btnUseExisting.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
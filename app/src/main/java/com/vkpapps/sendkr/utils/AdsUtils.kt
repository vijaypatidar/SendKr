package com.vkpapps.sendkr.utils

import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

/**
 * @author VIJAY PATIDAR
 */
object AdsUtils {
    var load = false
    fun getAdRequest(adView: AdView?) {
        if (load) {
            adView?.loadAd(AdRequest.Builder().build())
        } else {
            adView?.visibility = View.GONE
        }
    }
}
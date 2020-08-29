package com.vkpapps.sendkr.utils

import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.vkpapps.sendkr.BuildConfig

/**
 * @author VIJAY PATIDAR
 */
object AdsUtils {
    var load = false
    fun getAdRequest(adView: AdView?) {
        if (load) {
            if (!BuildConfig.DEBUG)
                adView?.loadAd(AdRequest.Builder().build())
            else {
                adView?.loadAd(AdRequest.Builder().addTestDevice("1FB5455B3DFB99F776E444EB03250A40").build())
            }
        } else {
            adView?.visibility = View.GONE
        }
    }

    fun getAdRequest(adView: InterstitialAd?) {
        if (!BuildConfig.DEBUG)
            adView?.loadAd(AdRequest.Builder().build())
        else
            adView?.loadAd(AdRequest.Builder().addTestDevice("1FB5455B3DFB99F776E444EB03250A40").build())
    }
}
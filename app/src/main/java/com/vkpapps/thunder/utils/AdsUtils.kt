package com.vkpapps.thunder.utils

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd

/**
 * @author VIJAY PATIDAR
 */
object AdsUtils {
    fun getAdRequest(adView: AdView?) {
        adView?.loadAd(AdRequest.Builder().build())
    }

    fun getAdRequest(adView: InterstitialAd?) {
        adView?.loadAd(AdRequest.Builder().build())
    }
}
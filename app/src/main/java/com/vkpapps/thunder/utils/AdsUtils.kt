package com.vkpapps.thunder.utils

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.vkpapps.thunder.BuildConfig

/**
 * @author VIJAY PATIDAR
 */
object AdsUtils {
    fun getAdRequest(adView: AdView?) {
        if (!BuildConfig.DEBUG) {
            adView?.loadAd(AdRequest.Builder().build())
        }
    }

    fun getAdRequest(adView: InterstitialAd?) {
        if (!BuildConfig.DEBUG) {
            adView?.loadAd(AdRequest.Builder().build())
        }
    }
}
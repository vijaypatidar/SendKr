package com.vkpapps.sendkr.utils;

import android.content.Context;
import android.os.Vibrator;

import com.vkpapps.sendkr.App;

public class VibrateUtils {
    public void vibrate(){
        Vibrator vibrator = (Vibrator) App.Companion.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(150);
    }
}

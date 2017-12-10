package me.ycdev.android.demo.assist.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

public class SystemUtils {
    public static boolean isScreen(Context cxt) {
        PowerManager powerMgr = (PowerManager) cxt.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerMgr.isInteractive();
        } else {
            //noinspection deprecation
            return powerMgr.isScreenOn();
        }
    }

    public static boolean isScreenLocked(Context cxt) {
        KeyguardManager keyguard = (KeyguardManager) cxt.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguard.isKeyguardLocked();
    }
}

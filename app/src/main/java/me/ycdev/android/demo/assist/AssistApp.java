package me.ycdev.android.demo.assist;

import android.app.Application;

import me.ycdev.android.arch.utils.AppLogger;

public class AssistApp extends Application {
    private static final String TAG = "AssistApp";

    @Override
    public void onCreate() {
        super.onCreate();
        AppLogger.i(TAG, "app start...");
    }
}

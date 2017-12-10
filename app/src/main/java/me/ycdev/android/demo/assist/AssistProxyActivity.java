package me.ycdev.android.demo.assist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import me.ycdev.android.arch.utils.AppLogger;

public class AssistProxyActivity extends Activity {
    private static final String TAG = "AssistProxyActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        AppLogger.d(TAG, "onCreate: " + intent);

        if (Intent.ACTION_ASSIST.equals(intent.getAction())) {
            printAssistInfoLogs(intent);
        }

        // start the main Activity
        intent.setClass(this, MainActivity.class);
        intent.setAction(Intent.ACTION_ASSIST);
        startActivity(intent);

        finish();
    }

    @SuppressLint("InlinedApi")
    private void printAssistInfoLogs(Intent intent) {
        String sourcePkgName = intent.getStringExtra(Intent.EXTRA_ASSIST_PACKAGE);
        Bundle sourceContext = intent.getBundleExtra(Intent.EXTRA_ASSIST_CONTEXT);
        String referrer = intent.getStringExtra(Intent.EXTRA_REFERRER);
        int uid = intent.getIntExtra(Intent.EXTRA_ASSIST_UID, -1);
        String inputDeviceId = intent.getStringExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID);
        String keyboardPrefer = intent.getStringExtra(Intent.EXTRA_ASSIST_INPUT_HINT_KEYBOARD);
        AppLogger.d(TAG, "assist request info, sourcePkgName=" + sourcePkgName
                + ", referrer=" + referrer + ", uid=" + uid
                + ", inputDeviceId=" + inputDeviceId + ", keyboardPrefer=" + keyboardPrefer);
        if (sourceContext != null) {
            for (String key : sourceContext.keySet()) {
                AppLogger.d(TAG, "source context, key=" + key + ", value=" + sourceContext.get(key));
            }
        }
    }

    public static void startAssist(Context cxt) {
        Intent intent = new Intent(cxt, AssistProxyActivity.class);
        intent.setAction(Intent.ACTION_ASSIST);
        cxt.startActivity(intent);
    }
}

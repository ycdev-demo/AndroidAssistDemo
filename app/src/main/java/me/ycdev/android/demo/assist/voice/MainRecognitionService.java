package me.ycdev.android.demo.assist.voice;

import android.content.Intent;
import android.speech.RecognitionService;

import me.ycdev.android.arch.utils.AppLogger;

/**
 * Stub recognition service needed to be a complete voice interactor.
 */
public class MainRecognitionService extends RecognitionService {
    private static final String TAG = "MainRecognitionService";

    @Override
    public void onCreate() {
        super.onCreate();
        AppLogger.d(TAG, "onCreate");
    }

    @Override
    protected void onStartListening(Intent recognizerIntent, Callback listener) {
        AppLogger.d(TAG, "onStartListening, intent: %s, listener: %s",
                recognizerIntent, listener);
    }

    @Override
    protected void onCancel(Callback listener) {
        AppLogger.d(TAG, "onCancel, listener: %s", listener);
    }

    @Override
    protected void onStopListening(Callback listener) {
        AppLogger.d(TAG, "onStopListening, listener: %s", listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLogger.d(TAG, "onDestroy");
    }
}

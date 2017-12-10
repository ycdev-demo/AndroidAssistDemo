package me.ycdev.android.demo.assist.voice;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.voice.AlwaysOnHotwordDetector;
import android.service.voice.AlwaysOnHotwordDetector.Callback;
import android.service.voice.AlwaysOnHotwordDetector.EventPayload;
import android.service.voice.VoiceInteractionService;
import android.service.voice.VoiceInteractionSession;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.util.Locale;

import me.ycdev.android.arch.utils.AppLogger;
import me.ycdev.android.demo.assist.AssistProxyActivity;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainInteractionService extends VoiceInteractionService {
    private static final String TAG = "MainInteractionService";

    private AlwaysOnHotwordDetector mHotwordDetector;

    private final Callback mHotwordCallback = new Callback() {
        @Override
        public void onAvailabilityChanged(int status) {
            handleHotwordAvailabilityChange(status);
        }

        @Override
        public void onDetected(@NonNull EventPayload eventPayload) {
            AppLogger.d(TAG, "onDetected");

            // start the assistant
//            AssistProxyActivity.startAssist(getApplication());
            @SuppressLint("InlinedApi") int flags = VoiceInteractionSession.SHOW_WITH_ASSIST
                    | VoiceInteractionSession.SHOW_WITH_SCREENSHOT;
            showSession(null, flags);

            // prepare for next trigger
            startHotwordRecognition();
        }

        @Override
        public void onError() {
            AppLogger.d(TAG, "onError");
        }

        @Override
        public void onRecognitionPaused() {
            AppLogger.d(TAG, "onRecognitionPaused");
        }

        @Override
        public void onRecognitionResumed() {
            AppLogger.d(TAG, "onRecognitionResumed");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        AppLogger.d(TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder result = super.onBind(intent);
        AppLogger.d(TAG, "onBind: " + result);
        return result;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLogger.d(TAG, "onDestroy");
    }

    @Override
    public void onReady() {
        super.onReady();
        AppLogger.d(TAG, "onReady, add hotword listener");
        mHotwordDetector = createAlwaysOnHotwordDetector(
                "OK Google", Locale.forLanguageTag("en-US"), mHotwordCallback);
    }

    @Override
    public void onLaunchVoiceAssistFromKeyguard() {
        AppLogger.d(TAG, "onLaunchVoiceAssistFromKeyguard");
    }

    @Override
    public void setDisabledShowContext(int flags) {
        AppLogger.d(TAG, "setDisabledShowContext");
    }

    @Override
    public int getDisabledShowContext() {
        AppLogger.d(TAG, "getDisabledShowContext");
        return super.getDisabledShowContext();
    }

    @Override
    public void showSession(Bundle args, int flags) {
        AppLogger.d(TAG, "showSession");
        super.showSession(args, flags);
    }

    @Override
    public void onShutdown() {
        AppLogger.d(TAG, "onShutdown");
        super.onShutdown();
    }

    private void handleHotwordAvailabilityChange(int status) {
        AppLogger.d(TAG, "onAvailabilityChanged, status=%s", status);
        switch (status) {
            case AlwaysOnHotwordDetector.STATE_HARDWARE_UNAVAILABLE:
                AppLogger.d(TAG, "STATE_HARDWARE_UNAVAILABLE");
                break;
            case AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNSUPPORTED:
                AppLogger.d(TAG, "STATE_KEYPHRASE_UNSUPPORTED");
                break;
            case AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNENROLLED:
                AppLogger.d(TAG, "STATE_KEYPHRASE_UNENROLLED");
                Intent enroll = mHotwordDetector.createEnrollIntent();
                AppLogger.d(TAG, "Need to enroll with " + enroll);
                break;
            case AlwaysOnHotwordDetector.STATE_KEYPHRASE_ENROLLED:
                AppLogger.d(TAG, "STATE_KEYPHRASE_ENROLLED - starting recognition");
                startHotwordRecognition();
                break;
        }
    }

    private void startHotwordRecognition() {
        if (mHotwordDetector.startRecognition(0)) {
            AppLogger.d(TAG, "startRecognition succeeded");
        } else {
            AppLogger.d(TAG, "startRecognition failed");
        }
    }
}

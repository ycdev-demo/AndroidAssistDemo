package me.ycdev.android.demo.assist.voice;

import android.os.Build;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSessionService;
import android.support.annotation.RequiresApi;

import me.ycdev.android.arch.utils.AppLogger;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainVoiceSessionService extends VoiceInteractionSessionService {
    private static final String TAG = "MainVoiceSessionService";

    @Override
    public void onCreate() {
        super.onCreate();
        AppLogger.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLogger.d(TAG, "onDestroy");
    }

    @Override
    public VoiceInteractionSession onNewSession(Bundle args) {
        AppLogger.d(TAG, "onNewSession, args=%s", args);
        if (args != null) {
            for (String key : args.keySet()) {
                AppLogger.d(TAG, "args, key=%s, value=%s", key, args.get(key));
            }
        }
        return new MainInteractionSession(this);
    }
}

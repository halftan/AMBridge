package com.ecarxfan.ambridge.utils;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

import java.util.List;

public class MediaControlClient {
    public static final String TAG = "MyImpl!" + MediaControlClient.class.getSimpleName();
    private Context mContext = null;

    public MediaControlClient(Context context) {
        mContext = context;
    }

    public void onControlledChanged(String controlledPackageName) {
    }

    public List<Object> getMediaContentTypeList() {
        return null;
    }

    public boolean onPlay(int soundSourceType, String mediaContentTypeId) {
        return false;
    }

    public boolean onPlayByContent(int soundSourceType, String filterContent) {
        return false;
    }

    public boolean onPause(int soundSourceType) {
        return false;
    }

    public boolean onResumeNow() {
        return false;
    }

    public boolean onPauseNow() {
        Log.i(TAG, "PlayPause button clicked!");
        AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_PAUSE));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_PAUSE));
        return true;
    }

    public boolean onPlayByMediaId(int soundSourceType, String uuid) {
        return false;
    }
}

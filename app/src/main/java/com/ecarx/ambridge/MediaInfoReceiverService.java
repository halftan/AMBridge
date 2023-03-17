package com.ecarx.ambridge;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.ecarx.ambridge.control.PlaybackControl;

import java.util.List;

public class MediaInfoReceiverService extends NotificationListenerService {
    public static final String TAG = MediaInfoReceiverService.class.getName();
    public static final String PACKAGE_NAME = "com.apple.android.music";
    private Context mContext;
    private Boolean mRegistered = false;

    public MediaInfoReceiverService() {
        super();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        mContext = getApplicationContext();
        fetchLatestPlaybackInfo();
    }

    @Override
    public void onDestroy() {
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getNotification().category.equals(Notification.CATEGORY_TRANSPORT)) {
            Log.i(TAG, "Music playback by " + sbn.getPackageName());
            Log.i(TAG, sbn.getNotification().toString());
            fetchLatestPlaybackInfo();
        }
    }

    private void fetchLatestPlaybackInfo() {
        MediaSessionManager m = getSystemService(MediaSessionManager.class);
        if (m == null) {
            Log.e(TAG, "Get MediaSessionManager failed");
            return;
        }
        ComponentName component = new ComponentName(getApplicationContext(), MediaInfoReceiverService.class);
        List<MediaController> sessions = m.getActiveSessions(component);
        Log.i(TAG, "Get sessions: count=" + sessions.size());
        for (MediaController session : sessions) {
            Log.i(TAG, "Session info: " + session.getPlaybackInfo().toString());
            Log.i(TAG,
                    "Metadata: " +
                            " artist=" +
                            session.getMetadata().getString(MediaMetadata.METADATA_KEY_ARTIST) +
                            " title=" +
                            session.getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE) +
                            " album=" +
                            session.getMetadata().getString(MediaMetadata.METADATA_KEY_ALBUM));
            Log.i(TAG, "Metadata: " + session.getMetadata().getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI));
            PlaybackControl.Companion.getInstance(mContext).fire(
                    session.getMetadata().getString(MediaMetadata.METADATA_KEY_ARTIST),
                    session.getMetadata().getString(MediaMetadata.METADATA_KEY_ALBUM),
                    session.getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE),
                    session.getMetadata().getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            );
        }
    }
}
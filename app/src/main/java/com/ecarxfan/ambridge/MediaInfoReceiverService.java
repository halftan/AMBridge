package com.ecarxfan.ambridge;

import android.Manifest;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.ecarxfan.ambridge.control.PlaybackControl;

import java.util.List;

public class MediaInfoReceiverService extends NotificationListenerService {
    public static final String TAG = MediaInfoReceiverService.class.getName();
    public static final String PACKAGE_NAME = "com.apple.android.music";
    private Context mContext;
    private MediaMetadata mMetadata;

    public MediaInfoReceiverService() {
        super();
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        mContext = getApplicationContext();

        fetchLatestPlaybackInfo();
    }

    @Override
    public void onDestroy() {
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification noti = sbn.getNotification();
        if (noti == null) return;
        String category = noti.category;
        if (category == null) return;
        if (category.equals(Notification.CATEGORY_TRANSPORT)) {
            Log.e(TAG, "Media playback by " + sbn.getPackageName());
            Log.e(TAG, sbn.getNotification().toString());
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
        Log.e(TAG, "Get sessions: count=" + sessions.size());
        for (MediaController session : sessions) {
            Log.e(TAG, "Session info: " + session.getPlaybackInfo().toString());
            Log.e(TAG, "Session from " + session.getPackageName());
            if (session.getPackageName().contains("music")) {
                MediaMetadata metadata = session.getMetadata();
                if (metadata != null) {
                    mMetadata = metadata;
                    String artist = mMetadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                    String album = mMetadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
                    String title = mMetadata.getString(MediaMetadata.METADATA_KEY_TITLE);
                    String artworkUri = mMetadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI);
                    if (artist == null) artist = "";
                    if (album == null) album = "";
                    if (title == null) title = "";
                    if (artworkUri == null) artworkUri = "";
                    Log.e(TAG,
                            "Metadata: " +
                                    " artist=" + artist +
                                    " title=" + title +
                                    " album=" + album +
                                    " album_art_uri=" + artworkUri
                    );
                    PlaybackControl.Companion.getInstance(mContext).fire(artist, album, title, artworkUri);
                }
            }
        }
    }
}
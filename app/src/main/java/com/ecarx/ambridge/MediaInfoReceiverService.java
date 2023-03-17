package com.ecarx.ambridge;

import android.app.RemoteAction;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteController;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class MediaInfoReceiverService extends NotificationListenerService implements RemoteController.OnClientUpdateListener {
    String TAG = MediaInfoReceiverService.class.getName();
    private RemoteController mRemoteController;
    private Context mContext;
    private Boolean mRegistered = false;

    public MediaInfoReceiverService() {
        super();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        mContext = getApplicationContext();
        mRemoteController = new RemoteController(mContext, this);
        Log.i(TAG, "Registering remote controller");
        if (!((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).registerRemoteController(mRemoteController)) {
            Log.e(TAG, "Register failed!");
        } else {
            Log.i(TAG, "Register done!");
            mRegistered = true;
        }
    }

    @Override
    public void onDestroy() {
        if (mRegistered) {
            ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).unregisterRemoteController(mRemoteController);
        }
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, sbn.getNotification().toString());
        if (sbn.getPackageName().contains("music")) {
            Log.i(TAG, "Music playing by " + sbn.getPackageName());
        }
    }

    @Override
    public void onClientChange(boolean b) {

    }

    @Override
    public void onClientPlaybackStateUpdate(int i) {
        Log.i(TAG, "Playback state changed to = " + i);
    }

    @Override
    public void onClientPlaybackStateUpdate(int i, long l, long l1, float v) {
        Log.i(TAG, "Playback state changed to = " + i + " l=" + l + " l1=" + l1 + " v=" + v);
    }

    @Override
    public void onClientTransportControlUpdate(int i) {

    }

    @Override
    public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor) {
        StringBuilder builder = new StringBuilder();
        builder.append("Metadata updated");
        builder.append(metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, "unknown"));
        builder.append(metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "unknown"));
        Log.i(TAG, builder.toString());
    }
}
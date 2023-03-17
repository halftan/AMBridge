package com.ecarx.sdk.mediacenter;

import android.app.PendingIntent;
import android.net.Uri;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/* loaded from: classes.dex */
public abstract class AbstractMusicPlaybackInfo {
    public static final int LOOP_MODE_ALL = 0;
    public static final int LOOP_MODE_SHUFFLE = 2;
    public static final int LOOP_MODE_SINGLE = 1;
    public static final int PLAYBACK_STATUS_INTERRUPT = 2;
    public static final int PLAYBACK_STATUS_PAUSED = 0;
    public static final int PLAYBACK_STATUS_PLAYING = 1;
    public static final int PLAYBACK_STATUS_PREPARE = 3;
    public static final int RADIO_MODE_CAROUSEL = 1;
    public static final int RADIO_MODE_PLAYING = 0;
    public static final int RADIO_MODE_SCAN = 4;
    public static final int RADIO_MODE_SEEK_NEXT = 3;
    public static final int RADIO_MODE_SEEK_PREV = 2;
    public static final int SOURCE_TYPE_AM = 4;
    public static final int SOURCE_TYPE_AUX = 5;
    public static final int SOURCE_TYPE_BT = 2;
    public static final int SOURCE_TYPE_DAB = 11;
    public static final int SOURCE_TYPE_FM = 3;
    public static final int SOURCE_TYPE_LOCAL = 0;
    public static final int SOURCE_TYPE_NET_NEWS = 9;
    public static final int SOURCE_TYPE_NET_VIDEO = 10;
    public static final int SOURCE_TYPE_ONLINE = 6;
    public static final int SOURCE_TYPE_STATION = 8;
    public static final int SOURCE_TYPE_USB = 1;
    public static final int SOURCE_TYPE_USB2 = 7;
    public static final int TYPE_MEDIA_LIST_NORMAL = 0;
    public static final int TYPE_MEDIA_LIST_RECOMMEND = 1;
    public static final int TYPE_MEDIA_LIST_SCENARIO = 2;
    public static final int TYPE_MEDIA_LIST_VIP = 3;
    public static final int TYPE_NO_MEDIA_LIST = -1;

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    @interface LoopMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    @Deprecated
    /* loaded from: classes.dex */
    @interface MediaListType {
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    @interface PlaybackStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    @interface RadioMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    @Deprecated
    /* loaded from: classes.dex */
    @interface SourceType {
    }

    public abstract String getAlbum();

    public abstract String getAppIcon();

    public abstract String getAppName();

    public abstract String getArtist();

    public abstract Uri getArtwork();

    public abstract String getCurrentLyricSentence();

    public abstract long getDuration();

    public abstract PendingIntent getLaunchIntent();

    public abstract int getLoopMode();

    public abstract Uri getLyric();

    public abstract String getLyricContent();

    public abstract Uri getMediaPath();

    public abstract Uri getNextArtwork();

    public abstract String getPackageName();

    public abstract int getPlaybackStatus();

    public abstract PendingIntent getPlayerIntent();

    public abstract int getPlayingItemPositionInQueue();

    public abstract String getPlayingMediaListId();

    public abstract int getPlayingMediaListType();

    public abstract Uri getPreviousArtwork();

    public abstract String getRadioFrequency();

    public abstract int getRadioMode();

    public abstract String getRadioStationName();

    public abstract int getSourceType();

    public abstract String getTitle();

    public abstract String getUuid();

    public abstract int getVip();

    public abstract boolean isCollected();

    public abstract boolean isDownloaded();

    public abstract boolean isSupportCollect();

    public abstract boolean isSupportDownload();

    public abstract boolean isSupportLoopModeSwitch();

    public abstract boolean isSupportVrCtrlPlayStatus();
}

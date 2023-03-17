package com.ecarx.sdk.mediacenter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public abstract class AbstractMusicClient {
    public static final int CODE_FAILURE = 0;
    public static final int CODE_FAILURE_EXCEED = -2;
    public static final int CODE_FAILURE_NOT_VIP = -3;
    public static final int CODE_FAILURE_UNLOAD = -1;
    public static final int CODE_SUCCESS = 1;
    public static final int FAV_COLLECTED = 1;
    public static final int FAV_COLLECTED_NOT_SUPPORT = -1;
    public static final int FAV_COLLECTED_SUPPORT = 1;
    public static final int FAV_NOT_COLLECTED = -1;
    public static final int LOOP_MODE_ALL = 0;
    public static final int LOOP_MODE_NEXT_MODE = 3;
    public static final int LOOP_MODE_SHUFFLE = 2;
    public static final int LOOP_MODE_SINGLE = 1;
    public static final int OPERATION_COLLECTED = 1;
    public static final int OPERATION_COLLECT_CANCEL = 0;
    public static final int TYPE_CAPABILITY_DEFAULT = -1;
    public static final int TYPE_CAPABILITY_DIM = 2;
    public static final int TYPE_CAPABILITY_SQUARE_KEY = 0;
    public static final int TYPE_CAPABILITY_VR = 1;
    public static final int TYPE_CAPABILITY_WIDGET = 3;
    public static final int TYPE_COLLECTION_IMAGE = 2;
    public static final int TYPE_COLLECTION_MUSIC = 0;
    public static final int TYPE_COLLECTION_NEWS = 4;
    public static final int TYPE_COLLECTION_RADIO = 3;
    public static final int TYPE_COLLECTION_UNKNOWN = -1;
    public static final int TYPE_COLLECTION_VIDEO = 1;
    public static final int TYPE_CONTROL_APP = 4;
    public static final int TYPE_DIM = 3;
    public static final int TYPE_DOWNLOAD_IMAGE = 2;
    public static final int TYPE_DOWNLOAD_MUSIC = 0;
    public static final int TYPE_DOWNLOAD_NEWS = 4;
    public static final int TYPE_DOWNLOAD_RADIO = 3;
    public static final int TYPE_DOWNLOAD_UNKNOWN = -1;
    public static final int TYPE_DOWNLOAD_VIDEO = 1;
    public static final int TYPE_MEDIA_INNER = 5;
    public static final int TYPE_MEDIA_LIST_NORMAL = 0;
    public static final int TYPE_MEDIA_LIST_RECOMMEND = 1;
    public static final int TYPE_MEDIA_LIST_SCENARIO = 2;
    public static final int TYPE_MEDIA_LIST_VIP = 3;
    public static final int TYPE_NO_MEDIA_LIST = -1;
    public static final int TYPE_SQUARE_KEY = 2;
    public static final int TYPE_VR = 1;
    public static final int TYPE_WIDGET = 0;

    @Retention(RetentionPolicy.SOURCE)
            /* loaded from: classes.dex */
    @interface Capability {
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface CollectOperation {
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface CollectState {
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface CollectSupportState {
    }

    @Retention(RetentionPolicy.SOURCE)
            /* loaded from: classes.dex */
    @interface CollectionCode {
    }

    @Retention(RetentionPolicy.SOURCE)
            /* loaded from: classes.dex */
    @interface CollectionType {
    }

    @Retention(RetentionPolicy.SOURCE)
            /* loaded from: classes.dex */
    @interface DownloadType {
    }

    @Retention(RetentionPolicy.SOURCE)
            /* loaded from: classes.dex */
    @interface LoopMode {
    }

    @Retention(RetentionPolicy.SOURCE)
            /* loaded from: classes.dex */
    @interface MediaListType {
    }

    @Retention(RetentionPolicy.SOURCE)
            /* loaded from: classes.dex */
    @interface Operation {
    }

    public abstract int ctrlCollect(int i, boolean z);

    public abstract void ctrlCollect(int i, String str, boolean z);

    public abstract boolean ctrlPauseMediaList(int i);

    public abstract boolean ctrlPlayMediaList(int i);

    public abstract List<ContentInfo> getContentList();

    public abstract long getCurrentProgress();

    public abstract int getCurrentSourceType();

    public abstract int[] getMediaSourceTypeList();

    public abstract MediaListsInfo getMultiMediaList(int[] iArr);

    public abstract MusicPlaybackInfo getMusicPlaybackInfo();

    public abstract List<MediaInfo> getPlaylist(int i);

    public abstract boolean onCancelRecommend(RecommendInfo recommendInfo);

    @Deprecated
    public abstract boolean onCollect(int i, boolean z);

    public abstract boolean onDownload(int i, boolean z);

    public abstract boolean onExit();

    @Deprecated
    public abstract boolean onForward();

    public abstract boolean onLoopModeChange(int i);

    public abstract void onMediaCenterFocusChanged(String str);

    public abstract boolean onMediaForward(boolean z);

    public abstract boolean onMediaQualityChange(int i);

    public abstract boolean onMediaRewind(boolean z);

    public abstract boolean onMediaSelected(int i, String str);

    public abstract boolean onMediaSelected(MediaInfo mediaInfo);

    public abstract boolean onNext();

    public abstract boolean onPause();

    public abstract boolean onPlay();

    public abstract boolean onPlayRecommend(RecommendInfo recommendInfo);

    public abstract boolean onPrevious();

    public abstract boolean onReplay();

    @Deprecated
    public abstract boolean onRewind();

    public abstract boolean onSourceChanged(int i, String str);

    public abstract boolean onSourceSelected(int i);

    public abstract void operationType(int i);

    public abstract boolean selectListMediaPlay(int i, int i2, String str);
}

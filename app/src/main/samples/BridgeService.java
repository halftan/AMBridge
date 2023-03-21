package com.ecarxfan.samples;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.ecarxfan.eas.framework.sdk.ECarXAPIBase;
import com.ecarxfan.eas.sdk.ECarXApiClient;
import com.ecarxfan.eas.sdk.mediacenter.ContentInfo;
import com.ecarxfan.eas.sdk.mediacenter.MediaCenterAPI;
import com.ecarxfan.eas.sdk.mediacenter.MediaListInfo;
import com.ecarxfan.eas.sdk.mediacenter.MediaListsInfo;
import com.ecarxfan.eas.sdk.mediacenter.MusicClient;
import com.ecarxfan.eas.sdk.mediacenter.MusicPlaybackInfo;
import com.ecarxfan.eas.sdk.mediacenter.control.IMediaControlClientAPI;
import com.ecarxfan.eas.sdk.mediacenter.control.MediaControlClient;
import com.ecarxfan.eas.sdk.mediacenter.exception.MediaCenterException;
import com.ecarxfan.eas.sdk.policy.api.IAudioAttributes;
import com.ecarxfan.eas.sdk.policy.api.PolicyAPI;
import com.ecarxfan.eas.sdk.vehicle.api.VehicleAPI;
import com.ecarxfan.eas.sdk.vehicle.api.carinfo.ICarInfo;
import com.ecarxfan.taes.remote.api.policy.bean.GuiConstants;
import com.ecarxfan.wecarflow.bridge.receiver.BootReceiver;
import com.ecarxfan.wecarflow.bridge.semantic.SemanticSearcher;
import com.ecarxfan.wecarflow.bridge.semantic.maker.MusicJsonMaker;
import com.ecarxfan.wecarflow.bridge.utils.Channel;
import com.ecarxfan.wecarflow.bridge.utils.ClickGap;
import com.ecarxfan.wecarflow.bridge.utils.MBLogUtils;
import com.ecarxfan.wecarflow.bridge.wrapper.ContentInfoWrapper;
import com.ecarxfan.wecarflow.bridge.wrapper.MediaInfoWrapper;
import com.ecarxfan.wecarflow.bridge.wrapper.MediaListInfoWrapper;
import com.ecarxfan.wecarflow.bridge.wrapper.MediaListsInfoWrapper;
import com.ecarxfan.wecarflow.contentsdk.ConnectionListener;
import com.ecarxfan.wecarflow.contentsdk.ContentManager;
import com.ecarxfan.wecarflow.contentsdk.ContentSDK;
import com.ecarxfan.wecarflow.contentsdk.bean.AreaContentResponseBean;
import com.ecarxfan.wecarflow.contentsdk.bean.BaseSongItemBean;
import com.ecarxfan.wecarflow.contentsdk.bean.EntryInfo;
import com.ecarxfan.wecarflow.contentsdk.bean.EntryListResponseBean;
import com.ecarxfan.wecarflow.contentsdk.callback.AreaContentResult;
import com.ecarxfan.wecarflow.contentsdk.callback.EntryListResult;
import com.ecarxfan.wecarflow.contentsdk.callback.MediaPlayResult;
import com.ecarxfan.wecarflow.controlsdk.AppState;
import com.ecarxfan.wecarflow.controlsdk.AppStateListener;
import com.ecarxfan.wecarflow.controlsdk.AudioFocusChangeListener;
import com.ecarxfan.wecarflow.controlsdk.BindListener;
import com.ecarxfan.wecarflow.controlsdk.FlowPlayControl;
import com.ecarxfan.wecarflow.controlsdk.MediaChangeListener;
import com.ecarxfan.wecarflow.controlsdk.MediaInfo;
import com.ecarxfan.wecarflow.controlsdk.MessageConstant;
import com.ecarxfan.wecarflow.controlsdk.PlayModeCode;
import com.ecarxfan.wecarflow.controlsdk.PlayStateListener;
import com.ecarxfan.wecarflow.controlsdk.QueryCallback;
import com.ecarxfan.wecarflow.controlsdk.callback.SemanticResult;
import com.ecarxfan.wecarflow.controlsdk.data.LaunchConfig;
import com.ecarxfan.wecarflow.controlsdk.data.LyricRowBean;
import com.ecarxfan.wecarflow.controlsdk.data.NavigationInfo;
import com.ecarxfan.wecarflow.controlsdk.data.SemanticSearchConfig;
import com.ecarxfan.wecarflow.controlsdk.data.UsageInfo;
import com.ecarxfan.wecarflow.controlsdk.utils.LyricChangedListener;
import ecarx.xsf.mediacenter.bean.IMediaContentType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* loaded from: classes2.dex */
public class BridgeService extends Service {
    public static final String COMMAND_HIDE_FLOAT_VIEW = "hide_float_view";
    public static final String COMMAND_SHOW_FLOAT_VIEW = "show_float_view";
    public static final String COMMAND_UPDATE_ENTRY_INFO = "update_entry_info";
    public static final String COMMAND_UPDATE_MEDIA_INFO = "update_media_info";
    public static final String COMMAND_UPDATE_RECOMMEND_LIST = "update_recommend_list";
    private static final long FAST_TIME = 10000;
    private static final int GET_DFB_SUCCESS = 72;
    private static final int GET_ENTRY_INFO_LIST_SUCCESS = 8;
    private static final int GET_ENTRY_LIST = 5;
    private static final int GET_RECOMMEND_LIST = 4;
    private static final int GET_SXTT_SUCCESS = 71;
    public static final String RECOVERY_ACTION = "com.tencent.wecarflow.bridge.RECOVERY";
    private static final int RE_INIT_MEDIA_CENTER = 11;
    private static final int STATUS_CONTENT_SDK_READY = 3;
    private static final int STATUS_CONTROL_SDK_READY = 2;
    private static final int STATUS_MEDIA_CENTER_API_READY = 10;
    public static final String TAG = "BridgeService";
    private static final int UPDATE_PLAY_INFO = 6;
    private static final List<ContentInfo> mContentInfoList = new ArrayList();
    private boolean isPlaying;
    private Context mContext;
    private Object mControlToken;
    private MediaInfo mCurrentMediaInfo;
    private View mFloatView;
    private Handler mHandler;
    private MediaCenterAPI mMediaCenterAPI;
    private IMediaControlClientAPI mMediaControlClientApi;
    private PolicyAPI mPolicyApi;
    private SemanticSearcher mSemanticSearcher;
    private Object mToken;
    private VehicleAPI mVehicleApi;
    private String mCurLineLyric = "";
    private final List<MediaInfo> currentMediaList = new ArrayList();
    private final HashMap<Integer, AreaContentResponseBean> mRecommendMap = new HashMap<>();
    private final HashMap<Integer, MediaListInfo> mRecommendPushMap = new HashMap<>();
    private Config config = new Config();
    private int mFocus = -1;
    private boolean policyApiReady = false;
    private boolean vehicleApiReady = false;
    private BroadcastReceiver mServiceStartedReceiver = new BroadcastReceiver() { // from class: com.tencent.wecarflow.bridge.BridgeService.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            MBLogUtils.i(BridgeService.TAG, "mServiceStartReceiver onReceive " + intent.getAction());
            if ("com.tencent.wecarflow.MEDIA_SERVICE_STARTED".equals(intent.getAction())) {
                if (!ContentManager.getInstance().isConnected()) {
                    MBLogUtils.i(BridgeService.TAG, "Content sdk 添加从新绑定的功能");
                    BridgeService.this.initContentSdk();
                }
                boolean isNeedRecover = false;
                if (Channel.isSmart() || Channel.isFX11()) {
                    MusicPlaybackInfo mediaCenterPlaybackInfo = BridgeService.this.getMediaCenterRecoveryMusicPlaybackInfo();
                    MBLogUtils.d(BridgeService.TAG, "mediaCenterRecoveryMusicPlaybackInfo:  " + mediaCenterPlaybackInfo);
                    if (mediaCenterPlaybackInfo != null) {
                        String packageName = mediaCenterPlaybackInfo.getPackageName();
                        MBLogUtils.d(BridgeService.TAG, "packageName:  " + packageName);
                        isNeedRecover = "com.tencent.wecarflow".equals(packageName);
                    }
                }
                if (BootReceiver.NEED_RECOVER || isNeedRecover) {
                    MBLogUtils.d(BridgeService.TAG, "recover from aqt start");
                    BootReceiver.NEED_RECOVER = false;
                    if (Channel.isSmart() || Channel.isBX11() || Channel.isJikeEF1E() || Channel.isFX11()) {
                        if (BridgeService.this.mMediaCenterAPI == null || BridgeService.this.mToken == null) {
                            BridgeService.this.mHandler.postDelayed(new Runnable() { // from class: com.tencent.wecarflow.bridge.BridgeService.3.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    MBLogUtils.d(BridgeService.TAG, "recover delay");
                                    BridgeService.this.recoverOnSmartOrBx11();
                                }
                            }, GuiConstants.FROM_SYSTEM_REQUEST_INTERVAL);
                        } else {
                            BridgeService.this.recoverOnSmartOrBx11();
                        }
                    }
                }
            }
        }
    };
    private final ECarXApiClient.Callback callback = new ECarXApiClient.Callback() { // from class: com.tencent.wecarflow.bridge.BridgeService.6
        @Override // com.ecarx.eas.sdk.ECarXApiClient.Callback
        public void onAPIReady(boolean flag) {
            MBLogUtils.d(BridgeService.TAG, "MediaCenterApi ready = " + flag);
            if (!flag) {
                BridgeService.this.mHandler.sendEmptyMessageDelayed(11, GuiConstants.FROM_USER_REQUEST_INTERVAL);
                MBLogUtils.w(BridgeService.TAG, "MediaCenterAPI init retry");
                return;
            }
            BridgeService bridgeService = BridgeService.this;
            bridgeService.mToken = bridgeService.mMediaCenterAPI.registerMusic("com.tencent.wecarflow", BridgeService.this.mMusicClient);
            BridgeService.this.mMediaCenterAPI.updateMediaSourceTypeList(BridgeService.this.mToken, new int[]{6});
            BridgeService.this.supportScene();
            BridgeService bridgeService2 = BridgeService.this;
            bridgeService2.mSemanticSearcher = new SemanticSearcher(bridgeService2.getApplicationContext(), BridgeService.this.mMediaCenterAPI, BridgeService.this.mToken);
            BridgeService.this.registerMediaControlClient();
            boolean success = BridgeService.this.mMediaCenterAPI.declareSupportCollectTypes(BridgeService.this.mToken, new int[]{6});
            BridgeService.this.mHandler.sendEmptyMessage(10);
            MBLogUtils.w(BridgeService.TAG, "declareSupportCollectTypes:" + success);
        }
    };
    private BindListener bindListener = new BindListener() { // from class: com.tencent.wecarflow.bridge.BridgeService.9
        @Override // com.tencent.wecarflow.controlsdk.BindListener
        public void onServiceConnected() {
            BridgeService.this.mHandler.sendEmptyMessage(2);
        }

        @Override // com.tencent.wecarflow.controlsdk.BindListener
        public void onBindDied() {
            MBLogUtils.i(BridgeService.TAG, "reset by controlsdk onBindDied");
            BridgeService.this.mMediaCenterAPI.updateCurrentSourceType(BridgeService.this.mToken, -1);
            BridgeService.this.mCurrentMediaInfo = null;
            BridgeService.this.currentMediaList.clear();
            if (BridgeService.this.hasFocus()) {
                BridgeService.this.updatePlayInfo(true);
            }
            BridgeService.this.mFocus = -1;
        }

        @Override // com.tencent.wecarflow.controlsdk.BindListener
        public void onServiceDisconnected() {
            MBLogUtils.i(BridgeService.TAG, "reset by controlsdk disconnected");
            BridgeService.this.mMediaCenterAPI.updateCurrentSourceType(BridgeService.this.mToken, -1);
            BridgeService.this.mCurrentMediaInfo = null;
            BridgeService.this.currentMediaList.clear();
            if (BridgeService.this.hasFocus()) {
                BridgeService.this.updatePlayInfo(true);
            }
            BridgeService.this.mFocus = -1;
        }

        @Override // com.tencent.wecarflow.controlsdk.BindListener
        public void onError(int i) {
        }
    };
    private ConnectionListener connectionListener = new ConnectionListener() { // from class: com.tencent.wecarflow.bridge.BridgeService.11
        @Override // com.tencent.wecarflow.contentsdk.ConnectionListener
        public void onConnected() {
            BridgeService.this.mHandler.sendEmptyMessage(3);
        }

        @Override // com.tencent.wecarflow.contentsdk.ConnectionListener
        public void onDisconnected() {
        }

        @Override // com.tencent.wecarflow.contentsdk.ConnectionListener
        public void onConnectionDied() {
        }
    };
    private final AudioFocusChangeListener audioFocusChangeListener = new AudioFocusChangeListener() { // from class: com.tencent.wecarflow.bridge.BridgeService.14
        @Override // com.tencent.wecarflow.controlsdk.AudioFocusChangeListener
        public void onAudioFocusChange(int focusChange) {
            BridgeService.this.mFocus = focusChange;
            if (BridgeService.this.hasFocus()) {
                BridgeService.this.updatePlayInfo(true);
            }
            MBLogUtils.d(BridgeService.TAG, "audioFocusChangeListener focusChange:" + focusChange);
            if (focusChange == 1) {
                if (BridgeService.this.mToken != null) {
                    boolean success = BridgeService.this.mMediaCenterAPI.requestPlay(BridgeService.this.mToken);
                    MBLogUtils.d(BridgeService.TAG, "audioFocusChangeListener requestPlay:" + success);
                    BridgeService.this.requestControl();
                }
                BridgeService.this.mHandler.sendEmptyMessage(4);
                BridgeService.this.mHandler.sendEmptyMessage(5);
                if ((BootReceiver.NEED_RECOVER && !Channel.isSmart() && !Channel.isBX11() && !Channel.isJikeEF1E() && !Channel.isFX11()) || !Channel.isKX11()) {
                    BootReceiver.NEED_RECOVER = false;
                    if (BridgeService.this.mToken != null) {
                        try {
                            BridgeService.this.mMediaCenterAPI.onMusicRecoveryComplete(BridgeService.this.mToken);
                            MBLogUtils.d(BridgeService.TAG, "recover success");
                        } catch (MediaCenterException e) {
                            MBLogUtils.d(BridgeService.TAG, "recover failed:" + e.toString());
                        }
                    }
                }
                BridgeService.this.registerRecoveryIntent();
                BridgeService.this.updatePlayList();
            }
        }
    };
    private final LyricChangedListener lyricChangedListener = new LyricChangedListener() { // from class: com.tencent.wecarflow.bridge.BridgeService.15
        @Override // com.tencent.wecarflow.controlsdk.utils.LyricChangedListener
        public void onLyricChanged(long l, String s, int i) {
            MBLogUtils.i(BridgeService.TAG, "lyricChanged:" + s);
            if (!BridgeService.this.mCurLineLyric.equals(s)) {
                BridgeService.this.mCurLineLyric = s;
                if (Channel.isSmart() && BridgeService.this.mToken != null) {
                    BridgeService.this.mMediaCenterAPI.updateMusicPlaybackState(BridgeService.this.mToken, BridgeService.this.mMusicPlaybackInfo);
                }
            }
        }

        @Override // com.tencent.wecarflow.controlsdk.utils.LyricChangedListener
        public void onLyricChanged(long l, String s, int i, List<LyricRowBean> list) {
        }
    };
    private final MediaChangeListener mediaChangeListener = new MediaChangeListener() { // from class: com.tencent.wecarflow.bridge.BridgeService.16
        @Override // com.tencent.wecarflow.controlsdk.MediaChangeListener
        public void onMediaChange(MediaInfo mediaInfo) {
            MBLogUtils.d(BridgeService.TAG, "onMediaChange: " + mediaInfo);
            BridgeService.this.mCurrentMediaInfo = mediaInfo;
            if (BridgeService.this.mCurrentMediaInfo == null || (Channel.isJikeEF1E() && MessageConstant.TYPE_VIDEO.equalsIgnoreCase(BridgeService.this.mCurrentMediaInfo.getMediaType()))) {
                BridgeService.this.updatePlayInfo(true);
            }
        }

        @Override // com.tencent.wecarflow.controlsdk.MediaChangeListener
        public void onMediaChange(MediaInfo mediaInfo, NavigationInfo navigationInfo) {
        }

        @Override // com.tencent.wecarflow.controlsdk.MediaChangeListener
        public void onFavorChange(boolean b, String s) {
            MBLogUtils.d(BridgeService.TAG, "favor:" + b + " uuid:" + s);
            if (BridgeService.this.mCurrentMediaInfo != null) {
                MBLogUtils.d(BridgeService.TAG, BridgeService.this.mCurrentMediaInfo.toString());
                String uuid = BridgeService.this.mCurrentMediaInfo.getItemUUID();
                if (!TextUtils.isEmpty(s) && s.contains(uuid)) {
                    BridgeService.this.mCurrentMediaInfo.setFavored(b);
                    BridgeService.this.mHandler.sendEmptyMessage(6);
                    return;
                }
                return;
            }
            MBLogUtils.d(BridgeService.TAG, "mCurrentMediaInfo is null");
        }

        @Override // com.tencent.wecarflow.controlsdk.MediaChangeListener
        public void onModeChange(int i) {
            MBLogUtils.d(BridgeService.TAG, "onModeChange: mode" + i + "media: " + BridgeService.this.mCurrentMediaInfo);
            if (BridgeService.this.mCurrentMediaInfo != null) {
                BridgeService.this.mCurrentMediaInfo.setMediaPlayMode(i);
                BridgeService.this.mHandler.sendEmptyMessage(6);
            }
        }

        @Override // com.tencent.wecarflow.controlsdk.MediaChangeListener
        public void onPlayListChange() {
            MBLogUtils.d(BridgeService.TAG, "onPlayListChange");
            BridgeService.this.updatePlayList();
        }
    };
    private final PlayStateListener playStateListener = new PlayStateListener() { // from class: com.tencent.wecarflow.bridge.BridgeService.18
        @Override // com.tencent.wecarflow.controlsdk.PlayStateListener
        public void onStart() {
            MBLogUtils.d(BridgeService.TAG, "onStart");
            BridgeService.this.isPlaying = true;
            BridgeService.this.mHandler.sendEmptyMessage(6);
        }

        @Override // com.tencent.wecarflow.controlsdk.PlayStateListener
        public void onPause() {
            MBLogUtils.d(BridgeService.TAG, "onPause");
            BridgeService.this.isPlaying = false;
            BridgeService.this.updatePlayInfo(true);
        }

        @Override // com.tencent.wecarflow.controlsdk.PlayStateListener
        public void onStop() {
            MBLogUtils.d(BridgeService.TAG, "onStop");
            BridgeService.this.isPlaying = false;
            BridgeService.this.updatePlayInfo(true);
        }

        @Override // com.tencent.wecarflow.controlsdk.PlayStateListener
        public void onProgress(String s, long current, long l1) {
            if (BridgeService.this.mMediaCenterAPI != null && BridgeService.this.mToken != null && BridgeService.this.mFocus == 1) {
                BridgeService.this.mMediaCenterAPI.updateCurrentProgress(BridgeService.this.mToken, current);
            }
        }

        @Override // com.tencent.wecarflow.controlsdk.PlayStateListener
        public void onBufferingStart() {
        }

        @Override // com.tencent.wecarflow.controlsdk.PlayStateListener
        public void onBufferingEnd() {
        }

        @Override // com.tencent.wecarflow.controlsdk.PlayStateListener
        public void onPlayError(int i, String s) {
        }

        @Override // com.tencent.wecarflow.controlsdk.PlayStateListener
        public void onAudioSessionId(int i) {
        }
    };
    private final MusicPlaybackInfo mMusicPlaybackInfo = new MusicPlaybackInfo() { // from class: com.tencent.wecarflow.bridge.BridgeService.21
        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public PendingIntent getLaunchIntent() {
            PackageManager packageManager = BridgeService.this.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage("com.tencent.wecarflow");
            if (intent == null) {
                MBLogUtils.w(BridgeService.TAG, "爱趣听未安装");
                return null;
            }
            intent.putExtra("intent_from", "jike_dock");
            return PendingIntent.getActivity(BridgeService.this.mContext, 0, intent, 134217728);
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public PendingIntent getPlayerIntent() {
            return getLaunchIntent();
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public String getLyricContent() {
            MBLogUtils.i(BridgeService.TAG, "run getLyricContent:" + BridgeService.this.mCurLineLyric);
            return BridgeService.this.mCurLineLyric;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public String getCurrentLyricSentence() {
            MBLogUtils.i(BridgeService.TAG, "run getCurrentLyricSentence()");
            return super.getCurrentLyricSentence();
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public String getTitle() {
            if (BridgeService.this.mCurrentMediaInfo != null) {
                String mediaName = BridgeService.this.mCurrentMediaInfo.getMediaName();
                if (!TextUtils.isEmpty(mediaName)) {
                    return mediaName;
                }
                return "";
            }
            return "";
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public String getArtist() {
            if (BridgeService.this.mCurrentMediaInfo != null) {
                String mediaAuthor = BridgeService.this.mCurrentMediaInfo.getMediaAuthor();
                String mediaGroupName = BridgeService.this.mCurrentMediaInfo.getMediaGroupName();
                if (!TextUtils.isEmpty(mediaAuthor)) {
                    return mediaAuthor;
                }
                if (!TextUtils.isEmpty(mediaGroupName)) {
                    return mediaGroupName;
                }
                return "";
            }
            return "";
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public String getAlbum() {
            if (BridgeService.this.mCurrentMediaInfo != null) {
                String mediaGroupName = BridgeService.this.mCurrentMediaInfo.getMediaGroupName();
                String mediaName = BridgeService.this.mCurrentMediaInfo.getMediaName();
                return TextUtils.isEmpty(mediaGroupName) ? TextUtils.isEmpty(mediaName) ? "" : mediaName : mediaGroupName;
            }
            return "";
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public String getRadioStationName() {
            if (BridgeService.this.mCurrentMediaInfo != null) {
                String mediaGroupName = BridgeService.this.mCurrentMediaInfo.getMediaGroupName();
                String mediaName = BridgeService.this.mCurrentMediaInfo.getMediaName();
                return TextUtils.isEmpty(mediaGroupName) ? TextUtils.isEmpty(mediaName) ? "" : mediaName : mediaGroupName;
            }
            return "";
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public Uri getArtwork() {
            String artWorkString = "";
            if (BridgeService.this.mCurrentMediaInfo != null) {
                String mediaImage = BridgeService.this.mCurrentMediaInfo.getMediaImage();
                MBLogUtils.d(BridgeService.TAG, "mediaImage:" + mediaImage);
                artWorkString = TextUtils.isEmpty(mediaImage) ? "" : mediaImage;
            }
            MBLogUtils.d(BridgeService.TAG, "artWorkString:" + artWorkString);
            return Uri.parse(artWorkString);
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public int getPlaybackStatus() {
            if (BridgeService.this.mCurrentMediaInfo == null) {
                return 0;
            }
            if (!BridgeService.this.isPlaying) {
                if (BridgeService.this.mFocus == -2) {
                    MBLogUtils.d(BridgeService.TAG, "PLAYBACK_STATUS_INTERRUPT");
                    return 2;
                }
                MBLogUtils.d(BridgeService.TAG, "PLAYBACK_STATUS_PAUSED");
                return 0;
            }
            MBLogUtils.d(BridgeService.TAG, "PLAYBACK_STATUS_PLAYING");
            return 1;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public int getPlayingItemPositionInQueue() {
            if (BridgeService.this.mCurrentMediaInfo != null) {
                return BridgeService.this.mCurrentMediaInfo.getItemIndex();
            }
            return super.getPlayingItemPositionInQueue();
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public int getSourceType() {
            return BridgeService.this.getType();
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public boolean isSupportCollect() {
            if (BridgeService.this.mCurrentMediaInfo != null) {
                return BridgeService.this.mCurrentMediaInfo.isFavorable();
            }
            return false;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public boolean isCollected() {
            if (BridgeService.this.mCurrentMediaInfo != null) {
                return BridgeService.this.mCurrentMediaInfo.isFavored();
            }
            return super.isCollected();
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public int getLoopMode() {
            if (BridgeService.this.mCurrentMediaInfo != null) {
                int playMode = BridgeService.this.mCurrentMediaInfo.getMediaPlayMode();
                MBLogUtils.d(BridgeService.TAG, "playMode:" + playMode);
                return playMode;
            }
            return super.getLoopMode();
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public String getUuid() {
            if (BridgeService.this.mCurrentMediaInfo != null) {
                String itemUUID = BridgeService.this.mCurrentMediaInfo.getItemUUID();
                MBLogUtils.d(BridgeService.TAG, "itemUUID:" + itemUUID);
                return TextUtils.isEmpty(itemUUID) ? "" : itemUUID;
            }
            return "";
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public String getAppName() {
            PackageInfo info = null;
            PackageManager pm = BridgeService.this.getPackageManager();
            try {
                info = pm.getPackageInfo("com.tencent.wecarflow", 1);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                MBLogUtils.d(BridgeService.TAG, "getAppName exception: " + e.getMessage());
            }
            return info == null ? "" : info.applicationInfo.loadLabel(pm).toString();
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public String getPackageName() {
            return "com.tencent.wecarflow";
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public String getAppIcon() {
            return "drawable://ic_launcher_export";
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public long getDuration() {
            if (BridgeService.this.mCurrentMediaInfo != null) {
                if (MessageConstant.TYPE_BROADCAST.equals(BridgeService.this.mCurrentMediaInfo.getMediaType())) {
                    int duration = BridgeService.this.mCurrentMediaInfo.getBroadcastEndTime() - BridgeService.this.mCurrentMediaInfo.getBroadcastStartTime();
                    return duration * 60 * 1000;
                }
                if (!"book".equals(BridgeService.this.mCurrentMediaInfo.getMediaType()) && !"news".equals(BridgeService.this.mCurrentMediaInfo.getMediaType())) {
                    return BridgeService.this.mCurrentMediaInfo.getDuration() * 1000;
                }
                return BridgeService.this.mCurrentMediaInfo.getDuration();
            }
            return super.getDuration();
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public int getVip() {
            return 0;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicPlaybackInfo, com.ecarx.eas.sdk.mediacenter.AbstractMusicPlaybackInfo
        public String getPlayingMediaListId() {
            if (BridgeService.this.mCurrentMediaInfo != null) {
                String songListId = BridgeService.this.mCurrentMediaInfo.getSongListId();
                MBLogUtils.d(BridgeService.TAG, "getPlayingMediaListId:" + songListId);
                return TextUtils.isEmpty(songListId) ? "" : songListId;
            }
            MBLogUtils.d(BridgeService.TAG, "getPlayingMediaListId: null");
            return "";
        }
    };
    private final MediaControlClient mMediaControlClient = new MediaControlClient() { // from class: com.tencent.wecarflow.bridge.BridgeService.22
        @Override // com.ecarx.eas.sdk.mediacenter.control.MediaControlClient, com.ecarx.eas.sdk.mediacenter.control.AbstractMediaControlClient
        public boolean onPauseNow() {
            MBLogUtils.d(BridgeService.TAG, "onPauseNow");
            if (BridgeService.this.isPlaying) {
                FlowPlayControl.getInstance().doPause();
                return true;
            }
            return false;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.control.MediaControlClient, com.ecarx.eas.sdk.mediacenter.control.AbstractMediaControlClient
        public boolean onPause(int soundSourceType) {
            MBLogUtils.d(BridgeService.TAG, "onPause:" + soundSourceType);
            if (soundSourceType == 6) {
                FlowPlayControl.getInstance().doPause();
                return true;
            }
            return super.onPause(soundSourceType);
        }

        @Override // com.ecarx.eas.sdk.mediacenter.control.MediaControlClient, com.ecarx.eas.sdk.mediacenter.control.AbstractMediaControlClient
        public boolean onPlayByContent(int soundSourceType, String filterContent) {
            MBLogUtils.d(BridgeService.TAG, "onPlayByContent:" + soundSourceType + " mediaContentTypeId: " + filterContent);
            if (soundSourceType == 6) {
                if ("103".equals(filterContent)) {
                    MusicJsonMaker jsonMaker = new MusicJsonMaker.Builder().setEmotion("浪漫").setQuery("我想听浪漫的歌").build();
                    SemanticSearchConfig config = new SemanticSearchConfig(false, false);
                    FlowPlayControl.getInstance().semanticSearch(BridgeService.this.mContext.getPackageName(), jsonMaker.makeJson(), config, new QueryCallback<SemanticResult>() { // from class: com.tencent.wecarflow.bridge.BridgeService.22.1
                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onError(int i) {
                        }

                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onSuccess(SemanticResult semanticResult) {
                        }
                    });
                    return true;
                } else if ("92".equals(filterContent)) {
                    MusicJsonMaker jsonMaker2 = new MusicJsonMaker.Builder().setStyle("摇滚").setQuery("我想听摇滚的歌").build();
                    SemanticSearchConfig config2 = new SemanticSearchConfig(false, false);
                    FlowPlayControl.getInstance().semanticSearch(BridgeService.this.mContext.getPackageName(), jsonMaker2.makeJson(), config2, new QueryCallback<SemanticResult>() { // from class: com.tencent.wecarflow.bridge.BridgeService.22.2
                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onError(int i) {
                        }

                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onSuccess(SemanticResult semanticResult) {
                        }
                    });
                    return true;
                } else if ("100".equals(filterContent)) {
                    MusicJsonMaker jsonMaker3 = new MusicJsonMaker.Builder().setEmotion("安静").setQuery("我想听安静的歌").build();
                    SemanticSearchConfig config3 = new SemanticSearchConfig(false, false);
                    FlowPlayControl.getInstance().semanticSearch(BridgeService.this.mContext.getPackageName(), jsonMaker3.makeJson(), config3, new QueryCallback<SemanticResult>() { // from class: com.tencent.wecarflow.bridge.BridgeService.22.3
                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onError(int i) {
                        }

                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onSuccess(SemanticResult semanticResult) {
                        }
                    });
                    return true;
                } else {
                    return true;
                }
            }
            return super.onPlayByContent(soundSourceType, filterContent);
        }

        @Override // com.ecarx.eas.sdk.mediacenter.control.MediaControlClient, com.ecarx.eas.sdk.mediacenter.control.AbstractMediaControlClient
        public boolean onPlay(int soundSourceType, String mediaContentTypeId) {
            MBLogUtils.d(BridgeService.TAG, "onPlay soundSourceType:" + soundSourceType + " mediaContentTypeId: " + mediaContentTypeId);
            if (Channel.isJikeDC1E() || Channel.isJikeEF1E()) {
                if (soundSourceType == 6) {
                    if ("103".equals(mediaContentTypeId)) {
                        MusicJsonMaker jsonMaker = new MusicJsonMaker.Builder().setEmotion("浪漫").setQuery("我想听浪漫的歌").build();
                        SemanticSearchConfig config = new SemanticSearchConfig(false, false);
                        FlowPlayControl.getInstance().semanticSearch(BridgeService.this.mContext.getPackageName(), jsonMaker.makeJson(), config, new QueryCallback<SemanticResult>() { // from class: com.tencent.wecarflow.bridge.BridgeService.22.4
                            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                            public void onError(int i) {
                            }

                            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                            public void onSuccess(SemanticResult semanticResult) {
                            }
                        });
                    } else if ("92".equals(mediaContentTypeId)) {
                        MusicJsonMaker jsonMaker2 = new MusicJsonMaker.Builder().setStyle("摇滚").setQuery("我想听摇滚的歌").build();
                        SemanticSearchConfig config2 = new SemanticSearchConfig(false, false);
                        FlowPlayControl.getInstance().semanticSearch(BridgeService.this.mContext.getPackageName(), jsonMaker2.makeJson(), config2, new QueryCallback<SemanticResult>() { // from class: com.tencent.wecarflow.bridge.BridgeService.22.5
                            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                            public void onError(int i) {
                            }

                            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                            public void onSuccess(SemanticResult semanticResult) {
                            }
                        });
                    } else if ("100".equals(mediaContentTypeId)) {
                        MusicJsonMaker jsonMaker3 = new MusicJsonMaker.Builder().setEmotion("安静").setQuery("我想听安静的歌").build();
                        SemanticSearchConfig config3 = new SemanticSearchConfig(false, false);
                        FlowPlayControl.getInstance().semanticSearch(BridgeService.this.mContext.getPackageName(), jsonMaker3.makeJson(), config3, new QueryCallback<SemanticResult>() { // from class: com.tencent.wecarflow.bridge.BridgeService.22.6
                            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                            public void onError(int i) {
                            }

                            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                            public void onSuccess(SemanticResult semanticResult) {
                            }
                        });
                    }
                    return true;
                }
                return super.onPlay(soundSourceType, mediaContentTypeId);
            } else if (soundSourceType == 6) {
                if ("92".equals(mediaContentTypeId)) {
                    MusicJsonMaker jsonMaker4 = new MusicJsonMaker.Builder().setEmotion("欢快").setQuery("我想听欢快的歌").build();
                    SemanticSearchConfig config4 = new SemanticSearchConfig(false, false);
                    FlowPlayControl.getInstance().semanticSearch(BridgeService.this.mContext.getPackageName(), jsonMaker4.makeJson(), config4, new QueryCallback<SemanticResult>() { // from class: com.tencent.wecarflow.bridge.BridgeService.22.7
                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onError(int i) {
                        }

                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onSuccess(SemanticResult semanticResult) {
                        }
                    });
                } else if ("100".equals(mediaContentTypeId)) {
                    MusicJsonMaker jsonMaker5 = new MusicJsonMaker.Builder().setEmotion("安静").setQuery("我想听安静的歌").build();
                    SemanticSearchConfig config5 = new SemanticSearchConfig(false, false);
                    FlowPlayControl.getInstance().semanticSearch(BridgeService.this.mContext.getPackageName(), jsonMaker5.makeJson(), config5, new QueryCallback<SemanticResult>() { // from class: com.tencent.wecarflow.bridge.BridgeService.22.8
                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onError(int i) {
                        }

                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onSuccess(SemanticResult semanticResult) {
                        }
                    });
                }
                return true;
            } else {
                return super.onPlay(soundSourceType, mediaContentTypeId);
            }
        }
    };
    private final MusicClient mMusicClient = new AnonymousClass23();
    private MediaPlayResult mediaPlayResult = new MediaPlayResult() { // from class: com.tencent.wecarflow.bridge.BridgeService.24
        @Override // com.tencent.wecarflow.contentsdk.callback.MediaPlayResult
        public void success() {
            MBLogUtils.i(BridgeService.TAG, " mediaPlayResult success");
        }

        @Override // com.tencent.wecarflow.contentsdk.callback.MediaPlayResult
        public void failed(int i) {
            MBLogUtils.i(BridgeService.TAG, " mediaPlayResult code:" + i);
            if (i == -2) {
                Toast.makeText(BridgeService.this, "网络异常", 0).show();
                BridgeService.this.updateIfIsPlaying();
            } else if (i == -4 || i == -5) {
                BridgeService.this.updateIfIsPlaying();
            } else {
                Toast.makeText(BridgeService.this, "当前账号无法播放", 0).show();
            }
        }
    };
    private QueryCallback<Integer> favorCallBack = new QueryCallback<Integer>() { // from class: com.tencent.wecarflow.bridge.BridgeService.25
        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
        public void onError(int i) {
        }

        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
        public void onSuccess(Integer integer) {
            int code = integer.intValue();
            if (code == 4005) {
                Toast.makeText(BridgeService.this.mContext, "当前无播放内容~", 0).show();
            } else if (code == 4009) {
                Toast.makeText(BridgeService.this.mContext, "已经收藏了~", 0).show();
            } else if (code == 4006) {
                Toast.makeText(BridgeService.this.mContext, "当前内容不支持收藏~", 0).show();
            }
        }
    };

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mContext = this;
        ForegroundServiceDelegate.startForeground(this, 111);
        IntentFilter serviceStarted = new IntentFilter();
        serviceStarted.addAction("com.tencent.wecarflow.MEDIA_SERVICE_STARTED");
        serviceStarted.addAction("com.tencent.wecarflow.MEDIA_SERVICE_STARTED");
        registerReceiver(this.mServiceStartedReceiver, serviceStarted);
        MBLogUtils.d(TAG, "onCreate");
        this.mHandler = new Handler(getMainLooper()) { // from class: com.tencent.wecarflow.bridge.BridgeService.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int what = msg.what;
                if (what == 2) {
                    MBLogUtils.d(BridgeService.TAG, "STATUS_CONTROL_SDK_READY");
                    BridgeService.this.initFocusStatus();
                    BridgeService.this.updateIfIsPlaying();
                } else if (what == 3) {
                    MBLogUtils.d(BridgeService.TAG, "STATUS_CONTENT_SDK_READY");
                    BridgeService.this.getRecommendList();
                    BridgeService.this.getEntryList();
                } else if (what == 4) {
                    MBLogUtils.d(BridgeService.TAG, "GET_RECOMMEND_LIST");
                    BridgeService.this.getRecommendList();
                } else if (what == 5) {
                    MBLogUtils.d(BridgeService.TAG, "GET_ENTRY_LIST");
                    BridgeService.this.getEntryList();
                } else if (what == 6) {
                    MBLogUtils.d(BridgeService.TAG, "UPDATE_PLAY_INFO");
                    BridgeService.this.updatePlayInfo();
                } else if (what == 8) {
                    MBLogUtils.d(BridgeService.TAG, "GET_ENTRY_INFO_LIST_SUCCESS");
                    BridgeService.this.pushEntryListToWidget(BridgeService.mContentInfoList);
                } else if (what == 10) {
                    MBLogUtils.d(BridgeService.TAG, "STATUS_MEDIA_CENTER_API_READY");
                    BridgeService.this.initControlSdk();
                    BridgeService.this.initContentSdk();
                    BridgeService.this.registerRecoveryIntent();
                } else if (what == 11) {
                    MBLogUtils.d(BridgeService.TAG, "RE_INIT_MEDIA_CENTER");
                    BridgeService.this.initMediaCenterApi();
                } else if (what == 71 || what == 72) {
                    MBLogUtils.d(BridgeService.TAG, "GET_DFB_SUCCESS");
                    BridgeService.this.pushRecommendListToWidget();
                }
            }
        };
        initMediaCenterApi();
        initPsdBluetooth();
        int permission = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission == 0) {
            MBLogUtils.d(TAG, "write external storage permission granted");
        }
    }

    private void initPsdBluetooth() {
        if (Channel.isKX11()) {
            PolicyAPI policyAPI = PolicyAPI.get();
            this.mPolicyApi = policyAPI;
            if (policyAPI != null) {
                policyAPI.init(this, new ECarXApiClient.Callback() { // from class: com.tencent.wecarflow.bridge.BridgeService.2
                    @Override // com.ecarx.eas.sdk.ECarXApiClient.Callback
                    public void onAPIReady(boolean b) {
                        MBLogUtils.d(BridgeService.TAG, "PolicyAPI: onAPIReady:" + b);
                        BridgeService.this.policyApiReady = b;
                        BridgeService bridgeService = BridgeService.this;
                        bridgeService.mVehicleApi = VehicleAPI.get(bridgeService.mContext);
                        if (BridgeService.this.mVehicleApi != null && BridgeService.this.policyApiReady) {
                            BridgeService.this.mVehicleApi.init(BridgeService.this.mContext, new ECarXApiClient.Callback() { // from class: com.tencent.wecarflow.bridge.BridgeService.2.1
                                @Override // com.ecarx.eas.sdk.ECarXApiClient.Callback
                                public void onAPIReady(boolean b2) {
                                    MBLogUtils.d(BridgeService.TAG, "VehicleAPI: onAPIReady:" + b2);
                                    BridgeService.this.vehicleApiReady = b2;
                                    if (BridgeService.this.vehicleApiReady) {
                                        BridgeService.this.registerAudioDeviceCallback();
                                    }
                                }
                            });
                        } else {
                            MBLogUtils.d(BridgeService.TAG, "mVehicleApi ==null");
                        }
                    }
                });
                return;
            } else {
                MBLogUtils.d(TAG, "mPolicyApi = null");
                return;
            }
        }
        MBLogUtils.d(TAG, "no need to init ");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void recoverOnSmartOrBx11() {
        MBLogUtils.d(TAG, "recoverOnSmart");
        try {
            boolean success = this.mMediaCenterAPI.requestPlay(this.mToken);
            this.mMediaCenterAPI.onMusicRecoveryComplete(this.mToken);
            this.mFocus = 1;
            updatePlayList();
            MBLogUtils.d(TAG, "recover on smart/bx11 success:" + success);
        } catch (Exception e) {
            MBLogUtils.d(TAG, "recover on smart/bx11 failed:" + e.getMessage());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initFocusStatus() {
        FlowPlayControl.getInstance().queryAudioFocusState(new QueryCallback<Integer>() { // from class: com.tencent.wecarflow.bridge.BridgeService.4
            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
            public void onError(int i) {
            }

            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
            public void onSuccess(Integer integer) {
                BridgeService.this.mFocus = integer.intValue();
                MBLogUtils.d(BridgeService.TAG, "queryAudioFocusState result:" + integer);
                if (BridgeService.this.mFocus == 1 || BridgeService.this.mFocus == -2) {
                    BridgeService.this.updatePlayList();
                    if (BridgeService.this.mToken != null) {
                        boolean success = BridgeService.this.mMediaCenterAPI.requestPlay(BridgeService.this.mToken);
                        MBLogUtils.d(BridgeService.TAG, "queryAudioFocusState requestPlay:" + success);
                        BridgeService.this.requestControl();
                    }
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestControl() {
        Object obj;
        IMediaControlClientAPI iMediaControlClientAPI = this.mMediaControlClientApi;
        if (iMediaControlClientAPI != null && (obj = this.mControlToken) != null) {
            boolean result = iMediaControlClientAPI.requestControlled(obj);
            MBLogUtils.d(TAG, "registerMediaControlClient:" + result);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerAudioDeviceCallback() {
        MBLogUtils.d(TAG, "registerAudioDeviceCallback");
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        if (Build.VERSION.SDK_INT >= 23) {
            AudioDeviceInfo[] devices = audioManager.getDevices(2);
            AudioDeviceCallback audioDeviceCallback = new AudioDeviceCallback() { // from class: com.tencent.wecarflow.bridge.BridgeService.5
                @Override // android.media.AudioDeviceCallback
                public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
                    super.onAudioDevicesAdded(addedDevices);
                    MBLogUtils.d(BridgeService.TAG, "onAudioDevicesAdded");
                    UsageInfo usageInfo = BridgeService.this.createUsageInfo();
                    if (usageInfo != null) {
                        usageInfo.bluetoothConnectionState = 2;
                        MBLogUtils.d(BridgeService.TAG, "usageInfo:" + usageInfo.toString());
                        FlowPlayControl.getInstance().sendUsageInfo(usageInfo);
                        return;
                    }
                    MBLogUtils.d(BridgeService.TAG, "policyApiReady:" + BridgeService.this.policyApiReady);
                }

                @Override // android.media.AudioDeviceCallback
                public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
                    super.onAudioDevicesRemoved(removedDevices);
                    MBLogUtils.d(BridgeService.TAG, "onAudioDevicesRemoved");
                    UsageInfo usageInfo = BridgeService.this.createUsageInfo();
                    if (usageInfo != null) {
                        usageInfo.bluetoothConnectionState = 0;
                        MBLogUtils.d(BridgeService.TAG, "usageInfo:" + usageInfo.toString());
                        FlowPlayControl.getInstance().sendUsageInfo(usageInfo);
                    }
                }
            };
            if (devices != null && devices.length != 0) {
                audioDeviceCallback.onAudioDevicesAdded(devices);
            }
            audioManager.registerAudioDeviceCallback(audioDeviceCallback, this.mHandler);
        }
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        MBLogUtils.d(TAG, "onStartCommand:" + startId);
        if (intent != null) {
            this.config = new Config(intent);
            String action = intent.getAction();
            if (action != null) {
                char c = 65535;
                switch (action.hashCode()) {
                    case -1861429129:
                        if (action.equals(COMMAND_UPDATE_RECOMMEND_LIST)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 220262751:
                        if (action.equals(COMMAND_UPDATE_MEDIA_INFO)) {
                            c = 0;
                            break;
                        }
                        break;
                    case 575771946:
                        if (action.equals(COMMAND_SHOW_FLOAT_VIEW)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 610042257:
                        if (action.equals(COMMAND_UPDATE_ENTRY_INFO)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1570956741:
                        if (action.equals(COMMAND_HIDE_FLOAT_VIEW)) {
                            c = 4;
                            break;
                        }
                        break;
                }
                if (c == 0) {
                    updatePlayInfo();
                } else if (c == 1) {
                    getEntryList();
                } else if (c == 2) {
                    getRecommendList();
                } else if (c == 3) {
                    showFloatingWindow();
                } else if (c == 4) {
                    hideFloatingWindow();
                }
            }
        }
        ForegroundServiceDelegate.startForeground(this, 111);
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initMediaCenterApi() {
        MediaCenterAPI mediaCenterAPI = MediaCenterAPI.get(this.mContext);
        this.mMediaCenterAPI = mediaCenterAPI;
        if (mediaCenterAPI == null) {
            MBLogUtils.d(TAG, "MediaCenterAPI is null return ");
        } else {
            mediaCenterAPI.init(this, this.callback);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void supportScene() {
        if (Channel.isJikeDC1E() || Channel.isJikeEF1E()) {
            List<IMediaContentType> mediaContentTypes = new ArrayList<>();
            IMediaContentType romantic = new IMediaContentType();
            romantic.setId("103");
            romantic.setName("浪漫");
            IMediaContentType rocket = new IMediaContentType();
            rocket.setId("92");
            rocket.setName("醒神");
            mediaContentTypes.add(romantic);
            mediaContentTypes.add(rocket);
            this.mMediaCenterAPI.updateMediaContentTypeList(this.mToken, mediaContentTypes);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerMediaControlClient() {
        MBLogUtils.d(TAG, "registerMediaControlClient");
        IMediaControlClientAPI mediaControlClientApi = this.mMediaCenterAPI.getMediaControlClientApi();
        this.mMediaControlClientApi = mediaControlClientApi;
        this.mControlToken = mediaControlClientApi.register("com.tencent.wecarflow", this.mMediaControlClient);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerRecoveryIntent() {
        if (this.mMediaCenterAPI.getVersionInt() < 341) {
            MBLogUtils.d(TAG, "registerRecoveryIntent: version is too low");
            return;
        }
        try {
            Intent recoveryBroadcastIntent = new Intent();
            recoveryBroadcastIntent.setAction(RECOVERY_ACTION);
            recoveryBroadcastIntent.setClassName(getPackageName(), BootReceiver.class.getName());
            recoveryBroadcastIntent.setPackage(getPackageName());
            recoveryBroadcastIntent.putExtra("type", "StateRecover");
            boolean success = this.mMediaCenterAPI.registerMusicRecoveryIntent(this.mToken, 1, recoveryBroadcastIntent);
            MBLogUtils.d(TAG, "registerMusicRecoveryIntent:" + success);
        } catch (Exception e) {
            MBLogUtils.d(TAG, "registerMusicRecoveryIntent failed:" + e.getMessage());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public MusicPlaybackInfo getMediaCenterRecoveryMusicPlaybackInfo() {
        try {
            MusicPlaybackInfo mediaCenterPlaybackInfo = this.mMediaCenterAPI.getRecoveryMusicPlaybackInfo(this.mToken);
            return mediaCenterPlaybackInfo;
        } catch (Exception e) {
            MBLogUtils.d(TAG, "getRecoveryMusicPlaybackInfo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void getEntryList() {
        if (!BuildConfig.NEED_RECOMMEND.booleanValue()) {
            MBLogUtils.i(TAG, "No need too getEntryList on this device:jike_dc1e");
        } else if (!hadWriteExternalStoragePermission()) {
            MBLogUtils.e(TAG, "no read external permission to getEntryList");
        } else {
            MBLogUtils.d(TAG, "updateEntryInfo");
            ContentManager.getInstance().getEntryInfoList(21, new EntryListResult() { // from class: com.tencent.wecarflow.bridge.BridgeService.7
                @Override // com.tencent.wecarflow.contentsdk.callback.EntryListResult
                public void success(EntryListResponseBean entryListResponseBean) {
                    ArrayList<ContentInfo> contentInfoList = new ArrayList<>();
                    if (entryListResponseBean == null || entryListResponseBean.corePageListInfo == null) {
                        MBLogUtils.i(BridgeService.TAG, "retry on get entry list failed");
                        return;
                    }
                    MBLogUtils.d(BridgeService.TAG, entryListResponseBean.corePageListInfo.toString());
                    EntryListCache.getInstance().cacheEntryList(entryListResponseBean.corePageListInfo);
                    for (int i = 0; i < entryListResponseBean.corePageListInfo.size(); i++) {
                        if (i <= 2) {
                            EntryInfo entryInfo = entryListResponseBean.corePageListInfo.get(i);
                            ContentInfo contentInfo = new ContentInfoWrapper.Builder(BridgeService.this).setId(entryInfo.getPageId()).setPosition(i).setIntent(PageUtils.getIntentByPageIde(BridgeService.this.mContext, "jili", entryInfo.getPageId(), entryInfo.getSourceInfo(), entryInfo.getSerialNum(), entryInfo.getTitle())).setTitle(entryInfo.getTitle()).build();
                            contentInfoList.add(contentInfo);
                        }
                    }
                    BridgeService.mContentInfoList.clear();
                    BridgeService.mContentInfoList.addAll(contentInfoList);
                    BridgeService.this.mHandler.sendEmptyMessage(8);
                }

                @Override // com.tencent.wecarflow.contentsdk.callback.EntryListResult
                public void failed(int errorCode) {
                    MBLogUtils.w(BridgeService.TAG, "Get entry list failed！");
                    List<EntryInfo> entryList = EntryListCache.getInstance().getEntryList();
                    if (entryList == null) {
                        MBLogUtils.i(BridgeService.TAG, "retry on get entry list failed");
                        return;
                    }
                    ArrayList<ContentInfo> contentInfoList = new ArrayList<>();
                    for (int i = 0; i < entryList.size(); i++) {
                        if (i <= 2) {
                            EntryInfo entryInfo = entryList.get(i);
                            ContentInfo contentInfo = new ContentInfoWrapper.Builder(BridgeService.this).setId(entryInfo.getPageId()).setPosition(i).setIntent(PageUtils.getIntentByPageIde(BridgeService.this.mContext, "jili", entryInfo.getPageId(), entryInfo.getSourceInfo(), entryInfo.getSerialNum(), entryInfo.getTitle())).setTitle(entryInfo.getTitle()).build();
                            contentInfoList.add(contentInfo);
                        }
                    }
                    BridgeService.mContentInfoList.clear();
                    BridgeService.mContentInfoList.addAll(contentInfoList);
                    MBLogUtils.d(BridgeService.TAG, "show entry list cache");
                    BridgeService.this.mHandler.sendEmptyMessage(8);
                }
            });
        }
    }

    private boolean hadWriteExternalStoragePermission() {
        Context context = this.mContext;
        if (context != null) {
            boolean permission = ActivityCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
            MBLogUtils.d(TAG, "permission :" + permission);
            return permission;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pushEntryListToWidget(List<ContentInfo> contentInfoList) {
        if (this.mToken == null) {
            MBLogUtils.d(TAG, "pushEntryInfoToMediaCenter mToken == null");
        } else if (mContentInfoList.size() != 3) {
            MBLogUtils.d(TAG, "mContentInfoList.size()!= 3");
        } else if (hasFocus()) {
            try {
                boolean success = this.mMediaCenterAPI.updateMediaContent(this.mToken, contentInfoList);
                MBLogUtils.d(TAG, "updateMediaContent:" + success);
            } catch (MediaCenterException e) {
                e.printStackTrace();
                MBLogUtils.d(TAG, "pushEntryInfoToMediaCenter exception");
            }
        } else {
            MBLogUtils.d(TAG, "pushEntryInfoToMediaCenter cancel by has no focus");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initControlSdk() {
        FlowPlayControl.getInstance().addPlayStateListener(this.playStateListener);
        FlowPlayControl.getInstance().addMediaChangeListener(this.mediaChangeListener);
        FlowPlayControl.getInstance().addLyricChangedListener(this.lyricChangedListener);
        FlowPlayControl.getInstance().addAudioFocusChangeListener(this.audioFocusChangeListener);
        FlowPlayControl.getInstance().addBindListener(this.bindListener);
        FlowPlayControl.getInstance().addAppStateListener(new AppStateListener() { // from class: com.tencent.wecarflow.bridge.BridgeService.8
            @Override // com.tencent.wecarflow.controlsdk.AppStateListener
            public void onPlayStateChange(String s, MediaInfo mediaInfo) {
            }

            @Override // com.tencent.wecarflow.controlsdk.AppStateListener
            public void onActiveStateChange(String s) {
                if (AppState.ACTIVE_STATE_FG.equals(s)) {
                    MBLogUtils.d(BridgeService.TAG, "push on aqt focus");
                    BridgeService.this.mHandler.sendEmptyMessage(4);
                    BridgeService.this.mHandler.sendEmptyMessage(5);
                }
            }
        });
        if (FlowPlayControl.getInstance().isServiceConnected()) {
            MBLogUtils.d(TAG, "controlsdk is connected");
            this.bindListener.onServiceConnected();
            return;
        }
        MBLogUtils.d(TAG, "controlsdk start ot bind");
        FlowPlayControl.InitParams initParams = new FlowPlayControl.InitParams();
        initParams.setAutoRebind(true);
        FlowPlayControl.getInstance().init(initParams);
        FlowPlayControl.getInstance().bindPlayService(AppContext.getAppContext());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIfIsPlaying() {
        MBLogUtils.i(TAG, "updateIfIsPlaying");
        FlowPlayControl.getInstance().queryPlaying(new QueryCallback<Boolean>() { // from class: com.tencent.wecarflow.bridge.BridgeService.10
            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
            public void onError(int i) {
            }

            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
            public void onSuccess(Boolean aBoolean) {
                BridgeService.this.isPlaying = aBoolean.booleanValue();
                if (aBoolean.booleanValue() || BridgeService.this.hasFocus()) {
                    MBLogUtils.i(BridgeService.TAG, "Update first if is Playing！");
                    FlowPlayControl.getInstance().queryCurrent(new QueryCallback<MediaInfo>() { // from class: com.tencent.wecarflow.bridge.BridgeService.10.1
                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onError(int i) {
                            MBLogUtils.e(BridgeService.TAG, "getCurrentMedia:" + i);
                        }

                        @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                        public void onSuccess(MediaInfo mediaInfo) {
                            BridgeService.this.mCurrentMediaInfo = mediaInfo;
                            BridgeService.this.mHandler.sendEmptyMessage(6);
                        }
                    });
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initContentSdk() {
        if (ContentSDK.getInstance().isConnected()) {
            this.connectionListener.onConnected();
            MBLogUtils.d(TAG, "Content sdk is already init");
            return;
        }
        ContentManager.getInstance().init(AppContext.getAppContext(), this.connectionListener);
        MBLogUtils.d(TAG, "Init content sdk");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void getRecommendList() {
        if (!BuildConfig.NEED_RECOMMEND.booleanValue()) {
            MBLogUtils.i(TAG, "No need too getRecommendList on this device:jike_dc1e");
        } else if (!hadWriteExternalStoragePermission()) {
            MBLogUtils.e(TAG, "no read external permission to getRecommendList");
        } else {
            MBLogUtils.i(TAG, "fetch recommend list");
            ContentManager.getInstance().getAreaContentData(new AreaContentResult() { // from class: com.tencent.wecarflow.bridge.BridgeService.12
                @Override // com.tencent.wecarflow.contentsdk.callback.AreaContentResult
                public void success(AreaContentResponseBean areaContentResponseBean) {
                    if (areaContentResponseBean == null) {
                        MBLogUtils.w(BridgeService.TAG, "success sxtt : null");
                        return;
                    }
                    List<BaseSongItemBean> songList = areaContentResponseBean.getSonglist();
                    if (songList == null) {
                        MBLogUtils.w(BridgeService.TAG, "getRecommendList sxtt response list null");
                        return;
                    }
                    MBLogUtils.i(BridgeService.TAG, "getRecommendList sxtt success：" + songList.toString());
                    BridgeService.this.mRecommendMap.put(1, areaContentResponseBean);
                    ArrayList<com.ecarxfan.eas.sdk.mediacenter.MediaInfo> mediaInfoList = new ArrayList<>();
                    for (int i = 0; i < songList.size(); i++) {
                        mediaInfoList.add(new MediaInfoWrapper(songList.get(i), i));
                    }
                    MediaListInfo mediaListInfo = new MediaListInfoWrapper.Builder().setTitle(areaContentResponseBean.getTitle()).setListId("1").setListType(1).setSourceType(6).setMediaInfoList(mediaInfoList).build();
                    BridgeService.this.mRecommendPushMap.put(1, mediaListInfo);
                    BridgeService.this.mHandler.sendEmptyMessage(71);
                }

                @Override // com.tencent.wecarflow.contentsdk.callback.AreaContentResult
                public void failed(int i) {
                    MBLogUtils.w(BridgeService.TAG, "getRecommendList failed sxtt:" + i);
                }
            }, 1);
            ContentManager.getInstance().getAreaContentData(new AreaContentResult() { // from class: com.tencent.wecarflow.bridge.BridgeService.13
                @Override // com.tencent.wecarflow.contentsdk.callback.AreaContentResult
                public void success(AreaContentResponseBean areaContentResponseBean) {
                    if (areaContentResponseBean == null) {
                        MBLogUtils.w(BridgeService.TAG, "getRecommendList dfb success null！");
                        return;
                    }
                    List<BaseSongItemBean> songList = areaContentResponseBean.getSonglist();
                    if (songList == null) {
                        MBLogUtils.w(BridgeService.TAG, "getRecommendList dfb： songList == null");
                        return;
                    }
                    MBLogUtils.i(BridgeService.TAG, "getRecommendList dfb：" + songList.toString());
                    BridgeService.this.mRecommendMap.put(2, areaContentResponseBean);
                    ArrayList<com.ecarxfan.eas.sdk.mediacenter.MediaInfo> mediaInfoList = new ArrayList<>();
                    for (int i = 0; i < songList.size(); i++) {
                        mediaInfoList.add(new MediaInfoWrapper(songList.get(i), i));
                    }
                    MediaListInfo mediaListInfo = new MediaListInfoWrapper.Builder().setTitle(areaContentResponseBean.getTitle()).setListId("2").setListType(2).setMediaInfoList(mediaInfoList).setSourceType(6).build();
                    BridgeService.this.mRecommendPushMap.put(2, mediaListInfo);
                    BridgeService.this.mHandler.sendEmptyMessage(72);
                }

                @Override // com.tencent.wecarflow.contentsdk.callback.AreaContentResult
                public void failed(int i) {
                    MBLogUtils.w(BridgeService.TAG, "getRecommendList dfb filed:" + i);
                }
            }, 2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pushRecommendListToWidget() {
        if (!hasFocus()) {
            MBLogUtils.d(TAG, "cancel push recommend list by no focus");
        } else if (this.mRecommendPushMap.size() != 2) {
            MBLogUtils.d(TAG, "mediaListInfoWrapper size != 2");
        } else {
            try {
                MediaListsInfoWrapper wrapper = new MediaListsInfoWrapper(new MediaListInfo[0]);
                wrapper.addMediaListInfo(this.mRecommendPushMap.get(1));
                wrapper.addMediaListInfo(this.mRecommendPushMap.get(2));
                boolean success = this.mMediaCenterAPI.updateMultiMediaList(this.mToken, wrapper);
                MBLogUtils.d(TAG, "pushRecommendListToWidget:" + success);
            } catch (MediaCenterException e) {
                e.printStackTrace();
                MBLogUtils.w(TAG, "Push recommend list failed");
            }
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean hasFocus() {
        int i = this.mFocus;
        return i == 1 || i == -2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePlayList() {
        MBLogUtils.d(TAG, "updatePlayList");
        FlowPlayControl.getInstance().getCurrentList(new QueryCallback<List<MediaInfo>>() { // from class: com.tencent.wecarflow.bridge.BridgeService.17
            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
            public void onError(int i) {
                MBLogUtils.d(BridgeService.TAG, "Get playlist failed");
            }

            @Override // com.tencent.wecarflow.controlsdk.QueryCallback
            public void onSuccess(List<MediaInfo> mediaInfoList) {
                if (mediaInfoList == null) {
                    MBLogUtils.d(BridgeService.TAG, "Get playlist success but is null");
                    return;
                }
                MBLogUtils.d(BridgeService.TAG, "Get playlist success");
                BridgeService.this.currentMediaList.clear();
                BridgeService.this.currentMediaList.addAll(mediaInfoList);
                if (BridgeService.this.hasFocus()) {
                    BridgeService.this.mHandler.sendEmptyMessage(6);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePlayInfo() {
        updatePlayInfo(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePlayInfo(boolean force) {
        MBLogUtils.d(TAG, "updatePlayInfo force：" + force);
        Object obj = this.mToken;
        if (obj == null) {
            MBLogUtils.e(TAG, "updatePlayInfo OpenAPI not ready because token is null");
            return;
        }
        if (this.mFocus != -1) {
            boolean result = this.mMediaCenterAPI.requestPlay(obj);
            requestControl();
            MBLogUtils.d(TAG, "updatePlayInfo OpenAPI onAPIReady:" + this.mToken + "  " + result + " mFocus:" + this.mFocus);
        } else {
            MBLogUtils.d(TAG, "No need to push info!");
            if (!force) {
                return;
            }
        }
        int type = getType();
        MBLogUtils.d(TAG, "type:" + type + "force:" + force);
        this.mMediaCenterAPI.updateCurrentSourceType(this.mToken, type);
        if (type != -1 || force) {
            this.mMediaCenterAPI.updateMediaList(this.mToken, new MediaListInfo() { // from class: com.tencent.wecarflow.bridge.BridgeService.19
                @Override // com.ecarx.eas.sdk.mediacenter.MediaListInfo, com.ecarx.eas.sdk.mediacenter.AbstractMediaListInfo
                public int getSourceType() {
                    return 6;
                }

                @Override // com.ecarx.eas.sdk.mediacenter.MediaListInfo, com.ecarx.eas.sdk.mediacenter.AbstractMediaListInfo
                public int getMediaListType() {
                    return 0;
                }

                @Override // com.ecarx.eas.sdk.mediacenter.MediaListInfo, com.ecarx.eas.sdk.mediacenter.AbstractMediaListInfo
                public List<com.ecarxfan.eas.sdk.mediacenter.MediaInfo> getMediaList() {
                    List<com.ecarxfan.eas.sdk.mediacenter.MediaInfo> infoList = BridgeService.this.buildMediaList();
                    String list = "";
                    for (com.ecarxfan.eas.sdk.mediacenter.MediaInfo mediaInfo : infoList) {
                        list = mediaInfo.getTitle() + " ";
                    }
                    MBLogUtils.d(BridgeService.TAG, "size:" + infoList.size() + "content: " + list);
                    return infoList;
                }
            });
            this.mHandler.postDelayed(new Runnable() { // from class: com.tencent.wecarflow.bridge.BridgeService.20
                @Override // java.lang.Runnable
                public void run() {
                    MBLogUtils.d(BridgeService.TAG, "update delay 400");
                    BridgeService.this.mMediaCenterAPI.updateMusicPlaybackState(BridgeService.this.mToken, BridgeService.this.mMusicPlaybackInfo);
                }
            }, 400L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public List<com.ecarxfan.eas.sdk.mediacenter.MediaInfo> buildMediaList() {
        List<com.ecarxfan.eas.sdk.mediacenter.MediaInfo> mediaList = new ArrayList<>();
        MediaInfo mediaInfo = this.mCurrentMediaInfo;
        if (mediaInfo != null && MessageConstant.TYPE_VIDEO.equalsIgnoreCase(mediaInfo.getMediaType())) {
            MBLogUtils.d(TAG, "not push video list");
            return mediaList;
        } else if (this.mCurrentMediaInfo == null) {
            MBLogUtils.d(TAG, "current play info is null,no need push playList.");
            return mediaList;
        } else {
            for (int i = 0; i < this.currentMediaList.size(); i++) {
                MediaInfo bean = this.currentMediaList.get(i);
                mediaList.add(new MediaInfoWrapper(bean, i));
            }
            MBLogUtils.d(TAG, "current list size:" + mediaList.size());
            return mediaList;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getType() {
        int mediaType;
        MediaInfo mediaInfo = this.mCurrentMediaInfo;
        if (mediaInfo == null) {
            return -1;
        }
        String type = mediaInfo.getMediaType();
        if ((Channel.isSmart() || Channel.isJikeEF1E() || Channel.isFX11()) && MessageConstant.TYPE_VIDEO.equalsIgnoreCase(type)) {
            MBLogUtils.d(TAG, "clear widget when play video on smart");
            mediaType = -1;
        } else if (TextUtils.isEmpty(type)) {
            mediaType = -1;
        } else {
            mediaType = 6;
        }
        MBLogUtils.d(TAG, "MusicPlaybackInfo getSourceType " + type + ", result : " + mediaType);
        return mediaType;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.tencent.wecarflow.bridge.BridgeService$23  reason: invalid class name */
    /* loaded from: classes2.dex */
    public class AnonymousClass23 extends MusicClient {
        AnonymousClass23() {
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onPlay() {
            MBLogUtils.d(BridgeService.TAG, "MusicClient onPlay");
            if (ClickGap.needAction()) {
                return FlowPlayControl.getInstance().doPlay() == 2000;
            }
            MBLogUtils.d(BridgeService.TAG, "MusicClient onPlay too fast");
            return false;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onSourceSelected(int sourceType) {
            if (sourceType == 6) {
                FlowPlayControl.getInstance().queryCurrent(new QueryCallback<MediaInfo>() { // from class: com.tencent.wecarflow.bridge.BridgeService.23.1
                    @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                    public void onError(int i) {
                    }

                    @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                    public void onSuccess(MediaInfo mediaInfo) {
                        if (mediaInfo == null || !"song".equalsIgnoreCase(mediaInfo.getMediaType())) {
                            MBLogUtils.d(BridgeService.TAG, "play hot music");
                            SemanticSearchConfig searchConfig = new SemanticSearchConfig(true, false);
                            MusicJsonMaker.Builder builder = new MusicJsonMaker.Builder();
                            builder.setCharts("热歌榜");
                            builder.setQuery("我要听热歌榜");
                            String semanticJson = builder.build().makeJson();
                            FlowPlayControl.getInstance().semanticSearch("", semanticJson, searchConfig, new QueryCallback<SemanticResult>() { // from class: com.tencent.wecarflow.bridge.BridgeService.23.1.1
                                @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                                public void onError(int i) {
                                }

                                @Override // com.tencent.wecarflow.controlsdk.QueryCallback
                                public void onSuccess(SemanticResult semanticResult) {
                                }
                            });
                            return;
                        }
                        MBLogUtils.d(BridgeService.TAG, "continue play");
                        FlowPlayControl.getInstance().launchPlayService(BridgeService.this.mContext, new LaunchConfig(true, true));
                    }
                });
                return true;
            }
            return super.onSourceSelected(sourceType);
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onForward() {
            return super.onForward();
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onRewind() {
            return super.onRewind();
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onMediaForward(boolean isStart) {
            return super.onMediaForward(isStart);
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onMediaRewind(boolean isStart) {
            return super.onMediaRewind(isStart);
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onPause() {
            MBLogUtils.d(BridgeService.TAG, "MusicClient onPause");
            if (ClickGap.needAction()) {
                return FlowPlayControl.getInstance().doPause() == 2000;
            }
            MBLogUtils.d(BridgeService.TAG, "MusicClient onPause too fast");
            return false;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onNext() {
            MBLogUtils.d(BridgeService.TAG, "MusicClient onNext");
            if (ClickGap.needAction()) {
                return FlowPlayControl.getInstance().doNext() == 2000;
            }
            MBLogUtils.d(BridgeService.TAG, "MusicClient onNext click too fast");
            return false;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onPrevious() {
            MBLogUtils.d(BridgeService.TAG, "MusicClient onPrevious");
            if (ClickGap.needAction()) {
                return FlowPlayControl.getInstance().doPre() == 2000;
            }
            MBLogUtils.d(BridgeService.TAG, "MusicClient onPrevious click too fast");
            return false;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onCollect(int type, boolean isCollect) {
            MBLogUtils.d(BridgeService.TAG, "MusicClient onCollect:" + isCollect);
            if (BridgeService.this.mCurrentMediaInfo != null && BridgeService.this.mCurrentMediaInfo.isFavorable()) {
                if (isCollect) {
                    FlowPlayControl.getInstance().addFavor(BridgeService.this.favorCallBack);
                } else {
                    FlowPlayControl.getInstance().cancelFavor(BridgeService.this.favorCallBack);
                }
            } else if (ECarXAPIBase.VERSION_INT >= 342) {
                try {
                    BridgeService.this.mMediaCenterAPI.updateCollectMsg(BridgeService.this.mToken, 1, BridgeService.this.getString(R.string.tips_unable_collect));
                } catch (MediaCenterException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public void onMediaCenterFocusChanged(String currentRequestClient) {
            MBLogUtils.i(BridgeService.TAG, "currentRequestClient:" + currentRequestClient);
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onLoopModeChange(int loopMode) {
            MBLogUtils.d(BridgeService.TAG, "MusicClient onLoopModeChange:" + loopMode);
            if (BridgeService.this.mCurrentMediaInfo == null) {
                return false;
            }
            int supportPlayMode = BridgeService.this.mCurrentMediaInfo.getSupportPlayMode();
            MBLogUtils.d(BridgeService.TAG, "supportPlayMode:" + supportPlayMode);
            if (loopMode == 0) {
                if (PlayModeCode.supportSequentialMode(supportPlayMode)) {
                    MBLogUtils.w(BridgeService.TAG, "change play mode to sequence");
                    return FlowPlayControl.getInstance().setPlayMode(0) == 2000;
                }
                MBLogUtils.w(BridgeService.TAG, "Current list is not support LOOP_MODE_ALL.");
                return false;
            } else if (loopMode == 2) {
                if (PlayModeCode.supportRandomMode(supportPlayMode)) {
                    MBLogUtils.w(BridgeService.TAG, "change play mode to random");
                    return FlowPlayControl.getInstance().setPlayMode(2) == 2000;
                }
                MBLogUtils.w(BridgeService.TAG, "Current list is not support LOOP_MODE_SHUFFLE.");
                return false;
            } else if (loopMode == 1) {
                if (PlayModeCode.supportSingleMode(supportPlayMode)) {
                    MBLogUtils.w(BridgeService.TAG, "change play mode to single");
                    return FlowPlayControl.getInstance().setPlayMode(1) == 2000;
                }
                MBLogUtils.w(BridgeService.TAG, "Current list is not support MODE_LOOP.");
                return false;
            } else {
                return super.onLoopModeChange(loopMode);
            }
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean onMediaSelected(int sourceType, String uuid) {
            MBLogUtils.d(BridgeService.TAG, "MusicClient onMediaSelected:" + uuid);
            try {
                int i = Integer.parseInt(uuid);
                return FlowPlayControl.getInstance().doPlay(i) == 2000;
            } catch (Exception e) {
                return false;
            }
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public MusicPlaybackInfo getMusicPlaybackInfo() {
            MBLogUtils.d(BridgeService.TAG, "MusicClient getMusicPlaybackInfo:" + BridgeService.this.mMusicPlaybackInfo.toString());
            return BridgeService.this.mMusicPlaybackInfo;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean ctrlPauseMediaList(int mediaListType) {
            MBLogUtils.d(BridgeService.TAG, "ctrlPauseMediaList");
            boolean success = FlowPlayControl.getInstance().doPause() == 2000;
            BridgeService.this.updatePlayInfo();
            return success;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean ctrlPlayMediaList(final int mediaListType) {
            MBLogUtils.d(BridgeService.TAG, "ctrlPlayMediaList:" + mediaListType);
            final AreaContentResponseBean areaContentResponseBean = (AreaContentResponseBean) BridgeService.this.mRecommendMap.get(Integer.valueOf(mediaListType));
            if (areaContentResponseBean != null) {
                BridgeService.this.mHandler.post(new Runnable() { // from class: com.tencent.wecarflow.bridge.-$$Lambda$BridgeService$23$8kJ2wJNjyQxxNSd3OHgyp0FvKio
                    @Override // java.lang.Runnable
                    public final void run() {
                        BridgeService.AnonymousClass23.this.lambda$ctrlPlayMediaList$0$BridgeService$23(areaContentResponseBean, mediaListType);
                    }
                });
                return true;
            }
            MBLogUtils.d(BridgeService.TAG, "areaContentResponseBean == null");
            return false;
        }

        public /* synthetic */ void lambda$ctrlPlayMediaList$0$BridgeService$23(AreaContentResponseBean areaContentResponseBean, int mediaListType) {
            ContentManager.getInstance().playAreaContentData(areaContentResponseBean, 0, mediaListType, 0, false, BridgeService.this.mediaPlayResult);
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public int ctrlCollect(int type, boolean isCollect) {
            MBLogUtils.d(BridgeService.TAG, "MusicClient onCollect:" + isCollect);
            if (BridgeService.this.mCurrentMediaInfo != null && BridgeService.this.mCurrentMediaInfo.isFavorable()) {
                if (BridgeService.this.mCurrentMediaInfo.isFavored()) {
                    FlowPlayControl.getInstance().cancelFavor(BridgeService.this.favorCallBack);
                } else {
                    FlowPlayControl.getInstance().addFavor(BridgeService.this.favorCallBack);
                }
            } else if (ECarXAPIBase.VERSION_INT >= 342) {
                try {
                    BridgeService.this.mMediaCenterAPI.updateCollectMsg(BridgeService.this.mToken, 0, BridgeService.this.getString(R.string.tips_unable_collect));
                } catch (MediaCenterException e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public void ctrlCollect(int type, String uuid, boolean isCollect) {
            MBLogUtils.d(BridgeService.TAG, "favor back screen:" + isCollect);
            ctrlCollect(type, isCollect);
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public boolean selectListMediaPlay(final int mediaListType, int sourceType, final String uuid) {
            int position;
            MBLogUtils.d(BridgeService.TAG, "mediaListType:" + mediaListType + " sourceType:" + sourceType + " uuid:" + uuid + " mRecommendMap size:" + BridgeService.this.mRecommendMap.size());
            final AreaContentResponseBean areaContentResponseBean = (AreaContentResponseBean) BridgeService.this.mRecommendMap.get(Integer.valueOf(mediaListType));
            if (areaContentResponseBean == null) {
                MBLogUtils.d(BridgeService.TAG, "selectListMediaPlay:areaContentResponseBean == null");
                return false;
            }
            List<BaseSongItemBean> songlist = areaContentResponseBean.getSonglist();
            if (songlist != null && songlist.size() != 0) {
                for (int i = 0; i < songlist.size(); i++) {
                    BaseSongItemBean baseSongItemBean = songlist.get(i);
                    if (!TextUtils.isEmpty(uuid) && uuid.equals(String.valueOf(baseSongItemBean.getSong_id()))) {
                        int position2 = i;
                        position = position2;
                        break;
                    }
                }
            }
            position = -1;
            MBLogUtils.w(BridgeService.TAG, "position:" + position);
            if (areaContentResponseBean.getSonglist() == null) {
                MBLogUtils.w(BridgeService.TAG, "areaContentResponseBean.getSonglist() == null");
                return false;
            } else if (position < 0 || position >= areaContentResponseBean.getSonglist().size()) {
                MBLogUtils.w(BridgeService.TAG, "Index out of bounds:" + position);
                return false;
            } else {
                final int finalPosition = position;
                final BaseSongItemBean itemBean = areaContentResponseBean.getSonglist().get(position);
                MBLogUtils.d(BridgeService.TAG, "selectListMediaPlay name:" + itemBean.getSong_name());
                BridgeService.this.mHandler.post(new Runnable() { // from class: com.tencent.wecarflow.bridge.BridgeService.23.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BridgeService.this.mMediaCenterAPI != null && BridgeService.this.mToken != null) {
                            BridgeService.this.mCurrentMediaInfo = new MediaInfo();
                            BridgeService.this.mCurrentMediaInfo.setFavored(false);
                            BridgeService.this.mCurrentMediaInfo.setItemUUID(uuid);
                            BridgeService.this.mCurrentMediaInfo.setMediaImage(itemBean.getAlbum_pic_300x300());
                            BridgeService.this.mCurrentMediaInfo.setMediaType(itemBean.getItemType());
                            BridgeService.this.mCurrentMediaInfo.setMediaAuthor(itemBean.getSinger_name());
                            BridgeService.this.mCurrentMediaInfo.setMediaGroupName(itemBean.getSinger_name());
                            BridgeService.this.mCurrentMediaInfo.setMediaName(itemBean.getItemTitle());
                            BridgeService.this.isPlaying = false;
                            BridgeService.this.updatePlayInfo();
                        }
                        ContentManager.getInstance().playAreaContentData(areaContentResponseBean, finalPosition, mediaListType, 0, BridgeService.this.config.isPlayForeground(), BridgeService.this.mediaPlayResult);
                    }
                });
                return true;
            }
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public List<ContentInfo> getContentList() {
            MBLogUtils.i(BridgeService.TAG, "getContentList");
            if (BridgeService.mContentInfoList.size() != 0) {
                return BridgeService.mContentInfoList;
            }
            BridgeService.this.mHandler.sendEmptyMessage(5);
            return super.getContentList();
        }

        @Override // com.ecarx.eas.sdk.mediacenter.MusicClient, com.ecarx.eas.sdk.mediacenter.AbstractMusicClient
        public MediaListsInfo getMultiMediaList(int[] mediaListType) {
            if (BridgeService.this.mRecommendPushMap.size() == 2) {
                MediaListsInfoWrapper wrapper = new MediaListsInfoWrapper(new MediaListInfo[0]);
                wrapper.addMediaListInfo((MediaListInfo) BridgeService.this.mRecommendPushMap.get(1));
                wrapper.addMediaListInfo((MediaListInfo) BridgeService.this.mRecommendPushMap.get(2));
                return wrapper;
            }
            MBLogUtils.i(BridgeService.TAG, "getMultiMediaList return null");
            BridgeService.this.mHandler.post(new Runnable() { // from class: com.tencent.wecarflow.bridge.BridgeService.23.3
                @Override // java.lang.Runnable
                public void run() {
                    BridgeService.this.getRecommendList();
                }
            });
            return super.getMultiMediaList(mediaListType);
        }
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        MediaCenterAPI mediaCenterAPI = this.mMediaCenterAPI;
        if (mediaCenterAPI != null) {
            mediaCenterAPI.release();
        }
        FlowPlayControl.getInstance().removeLyricChangedListener(this.lyricChangedListener);
        FlowPlayControl.getInstance().removePlayStateListener(this.playStateListener);
        FlowPlayControl.getInstance().removeMediaChangeListener(this.mediaChangeListener);
        FlowPlayControl.getInstance().removeAudioFocusChangeListener(this.audioFocusChangeListener);
        FlowPlayControl.getInstance().removeBindListener(this.bindListener);
        ContentManager.getInstance().unregisterConnectionListener(this.connectionListener);
        unregisterReceiver(this.mServiceStartedReceiver);
        this.mHandler.removeCallbacksAndMessages(null);
        MBLogUtils.i(TAG, "onDestroy");
    }

    @Override // android.app.Service, android.content.ComponentCallbacks
    public void onLowMemory() {
        super.onLowMemory();
        MBLogUtils.d(TAG, "onLowMemory");
    }

    @Override // android.app.Service, android.content.ComponentCallbacks2
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        MBLogUtils.d(TAG, "onTrimMemory:" + level);
    }

    private void hideFloatingWindow() {
        if (this.mFloatView != null) {
            WindowManager windowManager = (WindowManager) getSystemService("window");
            windowManager.removeView(this.mFloatView);
        }
    }

    private void showFloatingWindow() {
        if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this)) {
            WindowManager windowManager = (WindowManager) getSystemService("window");
            this.mFloatView = LayoutInflater.from(this).inflate(R.layout.float_view, (ViewGroup) null, false);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.type = 2038;
            layoutParams.format = 1;
            layoutParams.flags = 40;
            layoutParams.width = -2;
            layoutParams.height = -2;
            layoutParams.x = com.ecarxfan.car.wechatsdk.BuildConfig.VERSION_CODE;
            layoutParams.y = com.ecarxfan.car.wechatsdk.BuildConfig.VERSION_CODE;
            windowManager.addView(this.mFloatView, layoutParams);
            this.mFloatView.setOnTouchListener(new FloatingOnTouchListener(layoutParams, windowManager));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes2.dex */
    public class FloatingOnTouchListener implements View.OnTouchListener {
        private WindowManager.LayoutParams layoutParams;
        private WindowManager windowManager;
        private int x;
        private int y;

        public FloatingOnTouchListener(WindowManager.LayoutParams layoutParams, WindowManager windowManager) {
            this.layoutParams = layoutParams;
            this.windowManager = windowManager;
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if (action == 0) {
                this.x = (int) event.getRawX();
                this.y = (int) event.getRawY();
                return false;
            } else if (action == 2) {
                int nowX = (int) event.getRawX();
                int nowY = (int) event.getRawY();
                int movedX = nowX - this.x;
                int movedY = nowY - this.y;
                this.x = nowX;
                this.y = nowY;
                this.layoutParams.x += movedX;
                this.layoutParams.y += movedY;
                this.windowManager.updateViewLayout(view, this.layoutParams);
                return false;
            } else {
                return false;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public UsageInfo createUsageInfo() {
        VehicleAPI vehicleAPI;
        StringBuilder sb = new StringBuilder();
        sb.append("createUsageInfo:mPolicyApi != null:");
        sb.append(this.mPolicyApi != null);
        sb.append(" policyApiReady:");
        sb.append(this.policyApiReady);
        sb.append(" mVehicleApi != null:");
        sb.append(this.mVehicleApi != null);
        sb.append(" vehicleApiReady:");
        sb.append(this.vehicleApiReady);
        MBLogUtils.d(TAG, sb.toString());
        if (this.mPolicyApi != null && this.policyApiReady && (vehicleAPI = this.mVehicleApi) != null && this.vehicleApiReady) {
            ICarInfo carInfo = vehicleAPI.getCarInfo();
            if (carInfo == null) {
                MBLogUtils.d(TAG, "createUsageInfo:carInfo = null");
                return null;
            }
            Display display = carInfo.getDisplay(4);
            if (display == null) {
                MBLogUtils.d(TAG, "createUsageInfo:display =null");
                return null;
            }
            int displayId = display.getDisplayId();
            int usage = this.mPolicyApi.getAudioAttributes().getAudioAttributesUsage(IAudioAttributes.USAGE_MD_MEDIA, display);
            UsageInfo usageInfo = new UsageInfo();
            usageInfo.usage = usage;
            usageInfo.psdId = displayId;
            return usageInfo;
        }
        MBLogUtils.d(TAG, "createUsageInfo failed");
        return null;
    }
}

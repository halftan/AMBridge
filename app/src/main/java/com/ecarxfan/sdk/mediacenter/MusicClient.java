package com.ecarxfan.sdk.mediacenter;

import java.util.List;
/* loaded from: classes.dex */
public class MusicClient extends AbstractMusicClient {
    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onPlay() {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onPause() {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onNext() {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onPrevious() {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onForward() {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onRewind() {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onCollect(int type, boolean isCollect) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onDownload(int type, boolean isDownload) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onLoopModeChange(int loopMode) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onSourceSelected(int sourceType) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onSourceChanged(int sourceType, String preApp) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onMediaSelected(MediaInfo media) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public MusicPlaybackInfo getMusicPlaybackInfo() {
        return null;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public int[] getMediaSourceTypeList() {
        return new int[0];
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public int getCurrentSourceType() {
        return 0;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public long getCurrentProgress() {
        return 0L;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public List<MediaInfo> getPlaylist(int sourceType) {
        return null;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onReplay() {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onPlayRecommend(RecommendInfo recommend) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onCancelRecommend(RecommendInfo recommend) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onMediaSelected(int sourceType, String uuid) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onMediaForward(boolean isStart) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onMediaRewind(boolean isStart) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onMediaQualityChange(int qualityType) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public void onMediaCenterFocusChanged(String currentRequestClient) {
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean onExit() {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean selectListMediaPlay(int mediaListType, int sourceType, String uuid) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public List<ContentInfo> getContentList() {
        return null;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public MediaListsInfo getMultiMediaList(int[] mediaListType) {
        return null;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean ctrlPlayMediaList(int mediaListType) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public boolean ctrlPauseMediaList(int mediaListType) {
        return false;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public int ctrlCollect(int type, boolean isCollect) {
        return 0;
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public void operationType(int type) {
    }

    @Override // com.ecarx.sdk.mediacenter.AbstractMusicClient
    public void ctrlCollect(int type, String uuid, boolean isCollect) {
    }
}

package com.ecarx.ambridge.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;

public class MusicPlaybackInfo {
    private Context mContext = null;
    private String title;
    private String artist;
    private String album;
    private String artworkUri;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setArtworkUri(String artworkUri) {
        this.artworkUri = artworkUri;
    }

    public String getArtworkUri() {
        return artworkUri;
    }

    public MusicPlaybackInfo(Context context) {
        mContext = context;
        this.title = "Unknown track";
        this.artist = "Unknown artist";
        this.album = "Unknown album";
        this.artworkUri = "";
    }

    public PendingIntent getLaunchIntent() {
        return null;
    }

    public String getTitle() {
        return this.title;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getAlbum() {
        return this.album;
    }

    public String getRadioFrequency() {
        return null;
    }

    public String getRadioStationName() {
        return null;
    }

    public long getDuration() {
        return 0L;
    }

    public int getPlayingItemPositionInQueue() {
        return 0;
    }

    public int getSourceType() {
        return 0;
    }

    public Uri getMediaPath() {
        return null;
    }

    public int getPlaybackStatus() {
        return 0;
    }

    public String getLyricContent() {
        return null;
    }

    public Uri getLyric() {
        return null;
    }

    public String getCurrentLyricSentence() {
        return null;
    }

    public Uri getPreviousArtwork() {
        return null;
    }

    public Uri getArtwork() {
        if (artworkUri == null) return null;
        return Uri.parse(artworkUri);
    }

    public Uri getNextArtwork() {
        return null;
    }

    public int getLoopMode() {
        return 0;
    }

    public int getRadioMode() {
        return 0;
    }

    public boolean isSupportCollect() {
        return false;
    }

    public boolean isCollected() {
        return false;
    }

    public boolean isSupportDownload() {
        return false;
    }

    public boolean isDownloaded() {
        return false;
    }

    public String getUuid() {
        return null;
    }

    public String getAppName() {
        return null;
    }

    public String getAppIcon() {
        return null;
    }

    public String getPackageName() {
        return null;
    }

    public boolean isSupportLoopModeSwitch() {
        return true;
    }

    public boolean isSupportVrCtrlPlayStatus() {
        return true;
    }

    public String getPlayingMediaListId() {
        return null;
    }

    public int getVip() {
        return -1;
    }

    public int getPlayingMediaListType() {
        return 0;
    }

    public PendingIntent getPlayerIntent() {
        return null;
    }
}

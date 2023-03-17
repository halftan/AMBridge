package com.ecarx.sdk.mediacenter;

import android.net.Uri;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/* loaded from: classes.dex */
public abstract class AbstractRecommendInfo {
    public static final int RECOMMEND_TYPE_ALBUM = 1;
    public static final int RECOMMEND_TYPE_SINGER = 3;
    public static final int RECOMMEND_TYPE_SINGLE = 0;
    public static final int RECOMMEND_TYPE_SONG_LIST = 2;

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    @interface RecommendType {
    }

    public abstract String getArtist();

    public abstract Uri getArtwork();

    public abstract String getId();

    public abstract int getRecommendType();

    public abstract String getTextDescription();

    public abstract String getTitle();
}

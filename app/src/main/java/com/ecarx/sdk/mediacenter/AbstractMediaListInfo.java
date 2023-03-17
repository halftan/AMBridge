package com.ecarx.sdk.mediacenter;

import android.app.PendingIntent;
import java.util.List;
/* loaded from: classes.dex */
public abstract class AbstractMediaListInfo {
    public abstract List<MediaInfo> getMediaList();

    public abstract String getMediaListId();

    public abstract int getMediaListType();

    public abstract PendingIntent getPendingIntent();

    public abstract int getSourceType();

    public abstract String getTitle();
}

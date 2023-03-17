package com.ecarx.sdk.mediacenter;

import android.app.PendingIntent;
import android.net.Uri;
/* loaded from: classes.dex */
public abstract class AbstractContent {
    public abstract Uri getBackground();

    public abstract String getId();

    public abstract PendingIntent getPendingIntent();

    public abstract String getTitle();
}

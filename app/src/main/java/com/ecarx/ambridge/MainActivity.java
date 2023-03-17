package com.ecarx.ambridge;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.ecarx.ambridge.utils.DexLoader;
import com.ecarx.sdk.mediacenter.MusicClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onPlayPauseClick(View btn) {
        Log.i(this.TAG, "PlayPause button clicked!");
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    }

    public void onNextTrackClick(View btn) {
        Log.i(this.TAG, "Next button clicked!");
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_NEXT));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_NEXT));
    }

    public void onPrevTrackClick(View btn) {
        Log.i(this.TAG, "Previous button clicked!");
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_PREVIOUS));
    }

    public void onStopClick(View btn) {
        Log.i(this.TAG, "Stop button clicked!");
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_STOP));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_STOP));
    }

    public void onGetInfoClick(View btn) {
        Log.i(this.TAG, "GetInfo button clicked!");
        startService(new Intent(this, MediaInfoReceiverService.class));
//        MediaSessionManager m = (MediaSessionManager) getSystemService(MediaSessionManager.class);
//        if (m == null) {
//            Log.e(this.TAG, "Get MediaSessionManager failed");
//            return;
//        }
//        ComponentName component = new ComponentName(this.getApplicationContext(), MediaInfoReceiverService.class);
//        List<MediaController> sessions = m.getActiveSessions(component);
//        Log.i(this.TAG, "Get sessions: count=" + sessions.size());
//        for (MediaController session : sessions) {
//            Log.i(this.TAG, "Session info: " + session.getPlaybackInfo().toString());
//        }
    }

    public void onStopGetInfoClick(View btn) {
        Log.i(this.TAG, "StopGetInfo button clicked!");
        stopService(new Intent(this, MediaInfoReceiverService.class));
    }

    public void onSetDummyPlaybackInfoClick(View btn) {
        Log.i(this.TAG, "SetDummyPlaybackInfo button clicked!");
        try {
            InputStream dexFile = getAssets().open("openapi.jar");
            DexLoader loader = new DexLoader(getApplicationContext());
            DexClassLoader dl = loader.getLoader(dexFile);
            Class clz = dl.loadClass("com.ecarx.BuildConfig");
            Field[] fields = clz.getDeclaredFields();
            for(Field f : fields) {
                String inst = "";
                Log.i(TAG, "Field " + f.toString());
                Class fclz = f.getType();
                Object fval = null;
                try {
                    Object fobj = fclz.newInstance();
                    fval = fclz.cast(f.get(fobj));
                } catch (InstantiationException e) {
                    Log.d(TAG, "Cannot instantiate " + fclz.getName());
                    switch (fclz.getName()) {
                        case "boolean":
                            fval = f.getBoolean(true);
                            break;
                        case "int":
                            fval = f.getInt(1);
                            break;
                        case "float":
                            fval = f.getFloat(1.0);
                            break;
                        case "long":
                            fval = f.getLong(1L);
                            break;
                    }
                    if (fclz.getName() == "boolean") {
                    }
                }
                Log.i(TAG, "Value " + fval.toString());
            }
            Class mediaCenterAPI = dl.loadClass("com.ecarx.sdk.mediacenter.MediaCenterAPIImpl");
            Class musicClient = dl.loadClass("com.ecarx.sdk.mediacenter.MusicClient");
            Method methodGet = mediaCenterAPI.getMethod("get", Context.class);
            Method methodRegisterMusic = mediaCenterAPI.getMethod("registerMusic", String.class, musicClient);
            Object mAPI = methodGet.invoke(null, getApplicationContext());
            Log.i(TAG, "API: " + mAPI.getClass().getName());

            Object mToken = methodRegisterMusic.invoke(mAPI, "com.apple.android.music", musicClient.newInstance());
            Log.i(TAG, "Token " + mToken.toString());
        } catch (IOException e) {
            Log.e(TAG, "Error reading dex file", e);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not found!", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Cannot get string value!", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Cannot load method get", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Cannot invoke method!", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "Cannot instantiate class!", e);
        }
    }
}
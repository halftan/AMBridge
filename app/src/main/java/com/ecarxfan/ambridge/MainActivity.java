package com.ecarxfan.ambridge;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ecarxfan.ambridge.control.PlaybackControl;
import com.ecarxfan.ambridge.utils.MediaCenterHelper;
import com.ecarxfan.ambridge.utils.SDKLoader;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();
    private PlaybackControl playbackControl = null;
    private PlaybackControl.PlaybackEventListener listener = null;
    private MediaCenterHelper mHelper = null;
    private SDKLoader mDLib = null;

    private TextView tAlbum = null;
    private TextView tArtist = null;
    private TextView tTitle = null;
    private ImageView imArtwork = null;

    private Boolean hasPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tAlbum = (TextView) findViewById(R.id.text_album);
        tArtist = (TextView) findViewById(R.id.text_artist);
        tTitle = (TextView) findViewById(R.id.text_title);
        imArtwork = (ImageView) findViewById(R.id.image_artwork);
        tAlbum.setText("");
        tArtist.setText("");
        tTitle.setText("");

        String[] permissions = new String[]{
                Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
        };
        if (checkSelfPermission(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE) == PackageManager.PERMISSION_GRANTED) {
            this.hasPermission = true;
        } else {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
                            Manifest.permission.INTERNET,
                    }, 1
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    this.hasPermission = true;
                }
            }
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, "Download failed");
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playbackControl.unRegister(listener);
    }

    private HashMap<Integer, Long>lastClickMap = new HashMap<Integer, Long>();

    private boolean isDoubleClick(View btn) {
        Long currentTime = System.currentTimeMillis();
        Long lastClick = (Long) lastClickMap.getOrDefault(btn.hashCode(), 0L);
        if (lastClick == null) lastClick = 0L;
        if (currentTime - lastClick > 800L) {
            lastClickMap.put(btn.hashCode(), currentTime);
            return false;
        }
        Log.e(TAG, "Double Click detected!");
        return true;
    }

    public void onPlayPauseClick(View btn) {
        Log.e(TAG, "PlayPause button clicked!");
        if (isDoubleClick(btn)) {
            return;
        }
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    }

    public void onNextTrackClick(View btn) {
        Log.e(TAG, "Next button clicked!");
        if (isDoubleClick(btn)) {
            return;
        }
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_NEXT));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_NEXT));
    }

    public void onPrevTrackClick(View btn) {
        Log.e(TAG, "Previous button clicked!");
        if (isDoubleClick(btn)) {
            return;
        }
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_PREVIOUS));
    }

    public void onStopClick(View btn) {
        Log.e(TAG, "Stop button clicked!");
        if (isDoubleClick(btn)) {
            return;
        }
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_STOP));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_STOP));
    }

    public void onGetInfoClick(View btn) {
        Log.e(TAG, "GetInfo button clicked!");
        if (isDoubleClick(btn)) {
            return;
        }

        playbackControl = PlaybackControl.Companion.getInstance(getApplicationContext());
        if (listener == null) {
            listener = new PlaybackControl.PlaybackEventListener() {
                @Override
                public void updateTrackMetadata(@NonNull String artist, @NonNull String album, @NonNull String title, @NonNull String artworkUri) {
                    Log.e(TAG, "Updating metadata");
                    tAlbum.setText(album);
                    tArtist.setText(artist);
                    tTitle.setText(title);
                    if (!artworkUri.isEmpty()) {
                        if (artworkUri.startsWith("content://")) {
                            imArtwork.setImageURI(Uri.parse(artworkUri));
                        } else {
                            DownloadImageTask task = new DownloadImageTask(imArtwork);
                            task.execute(artworkUri);
                        }
                    }
                }
            };
            playbackControl.register(listener);
        }
        startService(new Intent(this, MediaInfoReceiverService.class));
        playbackControl.requestUpdate();
    }

    public void onStopGetInfoClick(View btn) {
        Log.e(TAG, "StopGetInfo button clicked!");
        if (isDoubleClick(btn)) {
            return;
        }
        stopService(new Intent(this, MediaInfoReceiverService.class));
        playbackControl.unRegister(listener);
    }

    public void onCallAppleMusicClick(View btn) {
        Log.e(TAG, "CallAppleMusic button clicked!");
        if (isDoubleClick(btn)) {
            return;
        }
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.apple.android.music");
        if (intent == null) {
            Toast.makeText(getApplicationContext(), "Apple Music not found!", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }

    private void initCarAPI() {
        if (mDLib != null) {
            Log.e(TAG, "initCarAPI called twice!");
            return;
        }
        try {
//            InputStream dexFile = getAssets().open("openapi.jar");
            Class t = getClassLoader().loadClass("com.ecarxfan.ambridge.utils.MediaCenterHelper");
            Log.e(TAG, "Loaded helper class " + t.getName());
            mDLib = new SDKLoader(getApplicationContext(), getClassLoader());
//            dexFile.close();
            String className = "com.ecarx.BuildConfig";
            SDKLoader.DexClass clz = mDLib.getClass(className);
            Field[] fields = clz.getDeclaredFields();
            for(Field f : fields) {
                Log.e(TAG, "Field " + f.toString());
                Class<?> fclz = f.getType();
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
                        default:
                            fval = "Unsupported value";
                    }
                }
                Log.e(TAG, "Value " + fval.toString());
            }
            Log.e(TAG, "Initializing car API using MediaCenterHelper");
            mHelper = new MediaCenterHelper(getApplicationContext(), mDLib);
//        } catch (IOException e) {
//            Log.e(TAG, "Error reading dex file", e);
//        } catch (ClassNotFoundException e) {
//            Log.e(TAG, "Class not found!", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Cannot get string value!", e);
//        } catch (NoSuchMethodException e) {
//            Log.e(TAG, "Cannot load method get", e);
//        } catch (InvocationTargetException e) {
//            Log.e(TAG, "Cannot invoke method!", e);
//        } catch (InstantiationException e) {
//            Log.e(TAG, "Cannot instantiate class!", e);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Cannot load class!", e);
        }
    }

    public void onInitCarApiClick(View btn) {
        Log.e(TAG, "SetDummyPlaybackInfo button clicked!");
        if (isDoubleClick(btn)) {
            return;
        }
        Log.e(TAG, "initMediaCenterApi called");
        initCarAPI();
        mHelper.initMediaCenterApi();
    }
}
package com.ecarx.ambridge;

import android.content.Context;
import android.content.Intent;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ecarx.ambridge.control.PlaybackControl;
import com.ecarx.ambridge.utils.DexLib;
import com.ecarx.ambridge.utils.MediaCenterHelper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();
    private PlaybackControl playbackControl = null;
    private PlaybackControl.PlaybackEventListener listener = null;
    private MediaCenterHelper mHelper = null;
    private DexLib mDLib = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tAlbum = (TextView) findViewById(R.id.text_album);
        TextView tArtist = (TextView) findViewById(R.id.text_artist);
        TextView tTitle = (TextView) findViewById(R.id.text_title);
        ImageView imArtwork = (ImageView) findViewById(R.id.image_artwork);

        playbackControl = PlaybackControl.Companion.getInstance(getApplicationContext());
        listener = new PlaybackControl.PlaybackEventListener() {
            @Override
            public void updateTrackMetadata(@NonNull String artist, @NonNull String album, @NonNull String title, @NonNull String artworkUri) {
                Log.e(TAG, "Updating metadata");
                tAlbum.setText(album);
                tArtist.setText(artist);
                tTitle.setText(title);
                DownloadImageTask task = new DownloadImageTask(imArtwork);
                task.execute(artworkUri);
            }
        };
        playbackControl.register(listener);
        tAlbum.setText("");
        tArtist.setText("");
        tTitle.setText("");
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

    public void onPlayPauseClick(View btn) {
        Log.i(TAG, "PlayPause button clicked!");
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    }

    public void onNextTrackClick(View btn) {
        Log.i(TAG, "Next button clicked!");
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_NEXT));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_NEXT));
    }

    public void onPrevTrackClick(View btn) {
        Log.i(TAG, "Previous button clicked!");
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_PREVIOUS));
    }

    public void onStopClick(View btn) {
        Log.i(TAG, "Stop button clicked!");
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_STOP));
        audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_STOP));
    }

    public void onGetInfoClick(View btn) {
        Log.i(TAG, "GetInfo button clicked!");
        startService(new Intent(this, MediaInfoReceiverService.class));

    }

    public void onStopGetInfoClick(View btn) {
        Log.i(TAG, "StopGetInfo button clicked!");
        stopService(new Intent(this, MediaInfoReceiverService.class));
    }

    public void onCallAppleMusicClick(View btn) {
        Log.i(TAG, "CallAppleMusic button clicked!");
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.apple.android.music");
        if (intent == null) {
            Toast.makeText(getApplicationContext(), "Apple Music not found!", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }

    private void initCarAPI() {
        if (mDLib != null) {
            Log.i(TAG, "initCarAPI called twice!");
            return;
        }
        try {
            InputStream dexFile = getAssets().open("openapi.jar");
            mDLib = new DexLib(dexFile, getApplicationContext());
            dexFile.close();
            String className = "com.ecarx.BuildConfig";
            DexLib.DexClass clz = mDLib.getClass(className);
            Field[] fields = clz.getDeclaredFields();
            for(Field f : fields) {
                Log.i(TAG, "Field " + f.toString());
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
                Log.i(TAG, "Value " + fval.toString());
            }
            Log.i(TAG, "Initializing car API using MediaCenterHelper");
            mHelper = new MediaCenterHelper(getApplicationContext(), mDLib);
        } catch (IOException e) {
            Log.e(TAG, "Error reading dex file", e);
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
        }
    }

    public void onInitCarApiClick(View btn) {
        Log.i(TAG, "SetDummyPlaybackInfo button clicked!");
        Log.i(TAG, "initMediaCenterApi called");
        initCarAPI();
        mHelper.initMediaCenterApi();
    }
}
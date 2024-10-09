package com.penkov.vikstv.ui;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.penkov.vikstv.R;
import com.penkov.vikstv.core.ChannelInfo;
import com.penkov.vikstv.web.Listener.ChannelVideoUrlListener;
import com.penkov.vikstv.web.Scrapper.ChannelVideoUrlScrapper;

import java.io.IOException;

public class VideoActivity extends AppCompatActivity
{
    // Tag to use with logcat
    public static final String TAG = "VideoActivity";

    // Key to transfer channel info
    public static final String CHANNEL = "channel_info";

    // Local variables
    private ChannelInfo mChannelInfo = null;
    private ExoPlayer mExoPlayer = null;
    private WifiManager.WifiLock mWifiLock = null;

    private SurfaceView mVideoSurfaceView = null;
    private ImageView mVideoLoadingImage = null;
    private TextView mVideoLoadingText = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Basic view stuff
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set wifi wake locks
        this.mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "my_wifi_lock");

        this.mWifiLock.acquire();

        // Set local values
        this.mChannelInfo = getIntent().getParcelableExtra(CHANNEL);
        this.mVideoSurfaceView = findViewById(R.id.channelVideoSurfaceView);
        this.mVideoLoadingImage = findViewById(R.id.channelVideoLoadingImage);
        this.mVideoLoadingText = findViewById(R.id.channelVideoLoadingText);

        // this.mVideoLoadingImage.setVisibility(View.INVISIBLE);

        // Check channel info is valid
        if (this.mChannelInfo == null) {
            Log.w(TAG, "VideoActivity called onCreate without video's channel info");
            finish();
            return;
        }

        // Load video link
        new ChannelVideoUrlScrapper(
                this,
                this.mChannelInfo.getChannelReference(),
                new ChannelVideoUrlListener()
                {
                    @Override
                    public void onResult(@NonNull String url) {
                        runOnUiThread(() -> onVideoUrlLoaded(url));
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        runOnUiThread(() -> onVideoUrlError(exception));
                    }
                }
        ).load();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // Release the wifi lock
        if (this.mWifiLock != null) {
            this.mWifiLock.release();
            this.mWifiLock = null;
        }

        // Release the media player
        if (this.mExoPlayer != null) {
            this.mExoPlayer.release();
            this.mExoPlayer = null;
        }
    }


    /**
     * Called when video url is available.
     *
     * @param url the url of the video.
     */
    private void onVideoUrlLoaded(String url)
    {
        this.mExoPlayer = new ExoPlayer.Builder(this).build();
        this.mExoPlayer.setVideoSurfaceView(mVideoSurfaceView);

        MediaItem mediaItem = MediaItem.fromUri(url);

        this.mExoPlayer.setMediaItem(mediaItem);
        this.mExoPlayer.prepare();
        this.mExoPlayer.setPlayWhenReady(true);

        this.mExoPlayer.addListener(
                new Player.Listener() {
                    @Override
                    public void onIsLoadingChanged(boolean isLoading) {
                        Player.Listener.super.onIsLoadingChanged(isLoading);

                        if (isLoading)
                            mVideoLoadingImage.setVisibility(View.VISIBLE);
                        else
                            mVideoLoadingImage.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onPlayerError(@NonNull PlaybackException error)
                    {
                        Toast.makeText(
                                VideoActivity.this,
                                error.toString(),
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    }

                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        Player.Listener.super.onIsPlayingChanged(isPlaying);

                        if (!isPlaying)
                            mVideoLoadingImage.setVisibility(View.VISIBLE);
                        else
                            mVideoLoadingImage.setVisibility(View.INVISIBLE);
                    }
                }
        );

    }


    /**
     * TODO: add description.
     *
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    private boolean onMediaPlayerError(MediaPlayer mp, int what, int extra)
    {
        Toast.makeText(
                this,
                what + " : " + extra,
                Toast.LENGTH_SHORT
        ).show();

        return false;
    }


    /**
     * Called when video url scrapping was unsuccessful.
     *
     * @param e exception representing what happened.
     */
    private void onVideoUrlError(Exception e)
    {
        Toast.makeText(
                this,
                "Video Loading Error: " + e.toString(),
                Toast.LENGTH_LONG
        ).show();
    }
}
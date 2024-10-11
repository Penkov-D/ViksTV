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
import com.penkov.vikstv.core.ChannelProgram;
import com.penkov.vikstv.web.Listener.ChannelProgramListener;
import com.penkov.vikstv.web.Listener.ChannelVideoUrlListener;
import com.penkov.vikstv.web.Scrapper.ChannelProgramScrapper;
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
    private TextView mProgramsText = null;

    private boolean mProggramShown = false;


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
        this.mProgramsText = findViewById(R.id.channelProgramsTextView);

        this.mVideoSurfaceView.setOnClickListener(this::toggleProgramView);

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

        // Load the programs
        new ChannelProgramScrapper(
                this.mChannelInfo.getChannelReference(),
                new ChannelProgramListener() {
                    @Override
                    public void onResult(@NonNull ChannelProgram[] channelPrograms) {
                        runOnUiThread(() -> onProgramsLoaded(channelPrograms));
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        runOnUiThread(() -> onProgramsError(exception));
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
     * Called whenever the program view is need to be toggled.
     *
     * @param v the view from onClick was called (equal mVideoSurfaceView)
     */
    private void toggleProgramView(View v)
    {
        // Toggle the view
        this.mProggramShown = !this.mProggramShown;

        // Set the textview visibility accordingly
        this.mProgramsText.setVisibility(this.mProggramShown ? View.VISIBLE : View.INVISIBLE);
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
        this.mExoPlayer.addListener(new ExoPlayerListener());

        MediaItem mediaItem = MediaItem.fromUri(url);

        this.mExoPlayer.setMediaItem(mediaItem);
        this.mExoPlayer.prepare();
        this.mExoPlayer.setPlayWhenReady(true);
    }


    /**
     * Called when video url scrapping was unsuccessful.
     *
     * @param e exception representing what happened.
     */
    private void onVideoUrlError(@NonNull Exception e)
    {
        // Show the url error message
        Toast.makeText(
                this,
                "Video Loading Error: " + e.toString(),
                Toast.LENGTH_LONG
        ).show();

        // Finish the video activity
        finish();
    }


    private void onProgramsLoaded(@NonNull ChannelProgram[] programs)
    {
        StringBuilder sb = new StringBuilder();

        for (ChannelProgram program : programs)
            sb.append(program.getTime()).append(" | ").append(program.getName()).append('\n');

        this.mProgramsText.setText(sb.toString());
    }


    private void onProgramsError(@NonNull Exception e)
    {
        this.mProgramsText.setText(e.getMessage());
    }


    /**
     * ExoPlayer listener on various events
     */
    private class ExoPlayerListener implements Player.Listener
    {
        @Override
        public void onIsLoadingChanged(boolean isLoading)
        {
            // Set the dot to be seen when loading
            mVideoLoadingImage.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);

            // Do the default behaviour
            Player.Listener.super.onIsLoadingChanged(isLoading);
        }

        @Override
        public void onPlayerError(@NonNull PlaybackException error)
        {
            // Print the error message
            Toast.makeText(
                    VideoActivity.this,
                    error.toString(),
                    Toast.LENGTH_SHORT
            ).show();

            // Finish the video player
            finish();
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying)
        {
            // Set the dot to hidden when playing
            mVideoLoadingImage.setVisibility(isPlaying ? View.INVISIBLE : View.VISIBLE);

            // Do the default behaviour
            Player.Listener.super.onIsPlayingChanged(isPlaying);
        }
    }
}
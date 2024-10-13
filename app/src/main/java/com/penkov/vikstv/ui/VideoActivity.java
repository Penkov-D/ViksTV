package com.penkov.vikstv.ui;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.penkov.vikstv.R;
import com.penkov.vikstv.core.ChannelInfo;
import com.penkov.vikstv.core.ChannelProgram;
import com.penkov.vikstv.web.Listener.ListenerChannelProgram;
import com.penkov.vikstv.web.Listener.ListenerChannelVideoURL;
import com.penkov.vikstv.web.Scraper.ScraperChannelProgram;
import com.penkov.vikstv.web.Scraper.ScraperChannelVideoURL;

public class VideoActivity extends AppCompatActivity
{
    // Tag to use with logcat
    public static final String TAG = "VideoActivity";

    // Key to transfer channel info
    public static final String CHANNEL = "channel_info";

    // Local variables
    private ChannelInfo mChannelInfo = null;
    private ExoPlayer mExoPlayer = null;
    private String mVideoURL = null;
    private WifiManager.WifiLock mWifiLock = null;

    private SurfaceView mVideoSurfaceView = null;
    private ImageView mVideoLoadingImage = null;
    private TextView mVideoLoadingText = null;
    private RecyclerView mProgramsRecycler = null;
    private ProgramItemAdapter mProgramAdapter = null;

    private boolean mProgramShown = false;
    private boolean mProgramSet = false;

    private boolean mActivityPaused = false;


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
        this.mProgramsRecycler = findViewById(R.id.channelPrograms_recycleView);

        this.mVideoSurfaceView.setOnClickListener(this::toggleProgramView);

        // this.mVideoLoadingImage.setVisibility(View.INVISIBLE);

        // Check channel info is valid
        if (this.mChannelInfo == null) {
            Log.w(TAG, "VideoActivity called onCreate without video's channel info");
            finish();
            return;
        }

        // Load video link
        new ScraperChannelVideoURL(
                this,
                this.mChannelInfo.getChannelReference(),
                new ListenerChannelVideoURL()
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
        new ScraperChannelProgram(
                this.mChannelInfo.getChannelReference(),
                new ListenerChannelProgram() {
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
    protected void onPause()
    {
        super.onPause();

        // Release the media player
        if (this.mExoPlayer != null) {
            this.mExoPlayer.release();
            this.mExoPlayer = null;
        }

        this.mActivityPaused = true;
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        // Check if this was called after a pause
        if (!this.mActivityPaused) return;
        this.mActivityPaused = false;

        // Restart the media player
        if (this.mVideoURL != null)
            onVideoUrlLoaded(null);

        // If no URL probably a bug
        else finish();
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


    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        // In case of DPAD_CENTER, toggle the program list.
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)
        {
            // Toggle only when key is up, to suppress double toggle.
            if (event.getAction() == KeyEvent.ACTION_UP)
                toggleProgramView(this.mVideoSurfaceView);

            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    /**
     * Called whenever the program view is need to be toggled.
     *
     * @param v the view from onClick was called (equal mVideoSurfaceView)
     */
    private void toggleProgramView(View v)
    {
        // Open the program list only if they are set.
        if (!this.mProgramSet) return;

        // Toggle the view
        this.mProgramShown = !this.mProgramShown;

        // Set the textview visibility accordingly
        this.mProgramsRecycler.setVisibility(this.mProgramShown ? View.VISIBLE : View.GONE);
        this.mProgramAdapter.updateView(this.mProgramsRecycler);

        if (!this.mProgramShown)
            this.mVideoSurfaceView.requestFocus();
    }


    /**
     * Called when video url is available.
     * If the url is null, called with the previous link.
     *
     * @param url the url of the video.
     */
    private void onVideoUrlLoaded(@Nullable String url)
    {
        // Release the previous player (in case of reload)
        if (this.mExoPlayer != null)
            this.mExoPlayer.release();

        // Load previous url if needed
        if (url == null) url = this.mVideoURL;
        else this.mVideoURL = url;

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


    @Override
    public void onBackPressed()
    {
        if (this.mProgramShown)
            toggleProgramView(this.mVideoSurfaceView);

        else
            super.onBackPressed();
    }


    private void onProgramsLoaded(@NonNull ChannelProgram[] programs)
    {
        // Call the recycler view adapter
        this.mProgramAdapter = new ProgramItemAdapter(programs);

        this.mProgramsRecycler.setLayoutManager(new LinearLayoutManager(this));
        this.mProgramsRecycler.setAdapter(this.mProgramAdapter);

        if (programs.length > 0)
            this.mProgramSet = true;
    }


    private void onProgramsError(@NonNull Exception e)
    {
        Toast.makeText(
                this,
                "Error loading programs: " + e.toString(),
                Toast.LENGTH_SHORT
        ).show();
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
            //finish();

            // Try again to load the video
            onVideoUrlLoaded(null);
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
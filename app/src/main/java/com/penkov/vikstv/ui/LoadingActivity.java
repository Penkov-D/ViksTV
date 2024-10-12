package com.penkov.vikstv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.penkov.vikstv.R;
import com.penkov.vikstv.core.ChannelInfo;
import com.penkov.vikstv.web.Listener.ChannelListListener;
import com.penkov.vikstv.web.Scrapper.ChannelListScrapper;

public class LoadingActivity extends AppCompatActivity
{
    // Layout components
    private TextView loadingTextView;
    private TextView errorTextView;
    private ViewGroup loadingContainer;
    private ImageButton tryAgainButton;
    private ImageView loadingImageView;

    // Scrapping object
    private final ChannelListScrapper channelListScrapper
            = new ChannelListScrapper(new ChannelListHandler());


    /**
     * Called when the activity first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Basic view stuff
        super.onCreate(savedInstanceState);

        // For testing light theme on device
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);

        // Set local view elements
        this.loadingTextView = findViewById(R.id.loadingTextView);
        this.errorTextView = findViewById(R.id.loadingErrorTextView);
        this.tryAgainButton = findViewById(R.id.refreshButton);
        this.loadingImageView = findViewById(R.id.loadingImageView);
        this.loadingContainer = findViewById(R.id.loadingContainerLayout);

        // Start the loading icon rotation
        Animation rotationAnimation = AnimationUtils.loadAnimation(
                this, R.anim.loading_image_rotation);
        this.loadingImageView.startAnimation(rotationAnimation);

        // Load the channels
        loadContent();
    }


    /**
     * Manage the UI and activate the scrapper for loading the channel list.
     */
    private void loadContent()
    {
        // Show loading views, hide loading button.
        this.loadingContainer.setVisibility(View.VISIBLE);
        this.tryAgainButton.setVisibility(View.INVISIBLE);

        // Start the actual loading
        this.channelListScrapper.load();
    }


    /**
     * Called when the refresh / reload button is pressed.
     *
     * @param view the view clicked on - equal to {@code this.tryAgainButton}
     */
    public void refreshButtonClick(View view) {
        loadContent();
    }


    /**
     * Called when all the channels are loaded.
     *
     * @param channelInfos list of loaded channels.
     */
    private void onResult (@NonNull ChannelInfo[] channelInfos)
    {
        // Start the MainActivity
        startActivity(
                new Intent(
                        // Create intent to open MainActivity
                        this,
                        MainActivity.class
                ).addFlags(
                        // Make this activity to disappear from stacktrace
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK
                ).putParcelableArrayListExtra(
                        // Set the intent all of the channels
                        MainActivity.CHANNELS,
                        channelListScrapper.getChannelsArrayList()
                )
        );

        // When MainActivity closes -
        //   this activity would automatically close the app.
        finish();
    }


    /**
     * Called when exception occurred while loading the channels.
     *
     * @param exception describing the problem occurred while loading.
     */
    private void onError (@NonNull Exception exception)
    {
        // Hide loading views, show loading button.
        this.loadingContainer.setVisibility(View.INVISIBLE);
        this.tryAgainButton.setVisibility(View.VISIBLE);

        // Print the error information
        this.errorTextView.setText(
                String.format(
                        "%s\n --------- \n%s",
                        exception.getClass().getSimpleName(),
                        exception.getMessage()));
    }


    /**
     * Class the serves as listener wrapper implementation -
     *   to call the inner methods.
     */
    private class ChannelListHandler extends ChannelListListener
    {
        @Override
        public void onResult(@NonNull ChannelInfo[] channelInfos)
        {
            // Run the local onResult method, making sure UI functions available.
            LoadingActivity.this.runOnUiThread(
                    () -> LoadingActivity.this.onResult(channelInfos)
            );
        }

        @Override
        public void onError(@NonNull Exception exception)
        {
            // Run the local onError method, making sure UI functions available.
            LoadingActivity.this.runOnUiThread(
                    () -> LoadingActivity.this.onError(exception)
            );
        }
    }

}
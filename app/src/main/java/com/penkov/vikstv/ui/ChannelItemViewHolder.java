package com.penkov.vikstv.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.penkov.vikstv.R;
import com.penkov.vikstv.core.ChannelInfo;
import com.penkov.vikstv.web.Listener.ListenerChannelIcon;
import com.penkov.vikstv.web.Scraper.ScraperChannelIcon;

public class ChannelItemViewHolder
        extends RecyclerView.ViewHolder
{
    // TAG to use with logcat
    private static final String TAG = "ChannelItemViewHolder";

    // Adapter created this ViewHolder
    private final ChannelItemAdapter mChannelAdapter;

    // Channel this item represents
    private ChannelInfo mChannelInfo = null;

    // card items
    private final ImageView mChannelIconImageView;
    private final TextView mChannelNameTextView;
    private Bitmap mChannelIcon = null;

    // Default parameters
    private static final int DEFAULT_TEXT = R.string.null_channel_name;
    private static final int DEFAULT_IMAGE = R.mipmap.ic_launcher;


    /**
     * Channel Item View Holder constructor.
     *
     * @param itemView the view constructed from this channel.
     */
    public ChannelItemViewHolder(@NonNull ChannelItemAdapter adapter, @NonNull View itemView)
    {
        super(itemView);

        this.mChannelAdapter = adapter;

        // Set onClick and onFocusChange listener
        itemView.setOnClickListener(this::onClick);
        itemView.setOnFocusChangeListener(this::onFocusChange);

        // Set local items
        this.mChannelIconImageView = itemView.findViewById(R.id.imageViewChannelIcon);
        this.mChannelNameTextView = itemView.findViewById(R.id.textViewChannelName);
    }


    /**
     * Set the desired channelInfo. Null channel info will lead for null text and the default icon.
     *
     * @param channelInfo the information about the channel, or null for default card.
     */
    public void setChannel(@Nullable ChannelInfo channelInfo)
    {
        // Store this channel info.
        this.mChannelInfo = channelInfo;

        if (mChannelIcon != null) {
            mChannelIcon.recycle();
            mChannelIcon = null;
        }

        // Set the channel default parameters
        mChannelNameTextView.setText(DEFAULT_TEXT);
        mChannelIconImageView.setImageResource(DEFAULT_IMAGE);

        if (channelInfo != null) {
            loadImage(channelInfo);
            mChannelNameTextView.setText(channelInfo.getChannelName());
        }
    }


    /**
     * Called when the card is focused or unfocused.
     *
     * @param view     the view that is focused
     * @param hasFocus if the view is focused, or unfocused
     */
    private void onFocusChange(View view, boolean hasFocus)
    {
        // Inform the adapter about the focus change
        mChannelAdapter.informFocused(getAdapterPosition(), hasFocus);
    }


    /**
     * Called when the card is clicked, activates the VideoActivity.
     *
     * @param view the view that called this method.
     */
    private void onClick(View view)
    {
        // If no channel info - do notting
        if (mChannelInfo == null)
            return;

        // Start the activity
        Context context = view.getContext();

        context.startActivity(
                new Intent(
                        context,
                        VideoActivity.class
                ).putExtra(
                        VideoActivity.CHANNEL,
                        mChannelInfo
                )
        );
    }


    /**
     * Load icon for the channel card.
     *
     */
    private void loadImage(@NonNull ChannelInfo channelInfo)
    {
        // Scrap the channel icon
        new ScraperChannelIcon(
                channelInfo.getChannelIconReference(),
                new ListenerChannelIcon()
                {
                    @Override
                    public void onResult(@NonNull Bitmap icon)
                    {
                        if (mChannelInfo == channelInfo)
                            updateIcon(icon);
                        else
                            icon.recycle();
                    }

                    @Override
                    public void onError(@NonNull Exception exception)
                    {
                        iconLoadingError(exception);
                    }
                }
        ).load();
    }


    /**
     * Update the channel icon, and manage resources.
     *
     * @param icon bitmap to set for the channel card.
     */
    private void updateIcon(@NonNull Bitmap icon)
    {
        // Clean the previous icon
        if (mChannelIcon != null)
            mChannelIcon.recycle();

        // Set the new icon
        mChannelIcon = icon;
        mChannelIconImageView.setImageBitmap(icon);
    }


    /**
     * Default process to make when icon loading failed.
     *
     * @param e exception describing what got wrong.
     */
    private void iconLoadingError(@NonNull Exception e)
    {
        // Post the error over logcat
        Log.w(TAG, String.format(
                "Could not load icon of channel: \"%s\"",
                mChannelInfo.getChannelName()), e);
    }
}

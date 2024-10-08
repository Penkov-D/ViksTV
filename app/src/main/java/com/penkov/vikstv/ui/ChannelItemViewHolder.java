package com.penkov.vikstv.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.penkov.vikstv.R;
import com.penkov.vikstv.core.ChannelInfo;
import com.penkov.vikstv.web.Listener.ChannelIconListener;
import com.penkov.vikstv.web.Scrapper.ChannelIconScrapper;

public class ChannelItemViewHolder
        extends RecyclerView.ViewHolder
{
    // TAG to use with logcat
    private static final String TAG = "ChannelItemViewHolder";

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
    public ChannelItemViewHolder(@NonNull View itemView)
    {
        super(itemView);

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
     * Load icon for the channel card.
     *
     */
    private void loadImage(@NonNull ChannelInfo channelInfo)
    {
        setIsRecyclable(false);
        new ChannelIconScrapper(
                channelInfo.getChannelIconReference(),
                new ChannelIconListener() {
                    @Override
                    public void onResult(@NonNull Bitmap bitmap)
                    {
                        if (mChannelInfo != channelInfo) {
                            bitmap.recycle();
                            return;
                        }

                        if (mChannelIcon != null)
                            mChannelIcon.recycle();

                        mChannelIcon = bitmap;
                        mChannelIconImageView.setImageBitmap(bitmap);
                        setIsRecyclable(true);
                    }

                    @Override
                    public void onError(@NonNull Exception exception)
                    {
                        Log.w(TAG,
                                String.format(
                                        "Could not load icon of channel: \"%s\"",
                                        channelInfo.getChannelName()),
                                exception);
                        setIsRecyclable(true);
                    }
                }
        ).load();
    }
}

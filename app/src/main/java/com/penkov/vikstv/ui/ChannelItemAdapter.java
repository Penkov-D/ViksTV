package com.penkov.vikstv.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.penkov.vikstv.R;
import com.penkov.vikstv.core.ChannelInfo;

import java.util.ArrayList;

public class ChannelItemAdapter
        extends RecyclerView.Adapter<ChannelItemViewHolder>
{
    // Channel list
    private final ArrayList<ChannelInfo> mChannels;


    /**
     * Channel adapter constructor.
     *
     * @param channels list of channels to display.
     */
    public ChannelItemAdapter(@NonNull ArrayList<ChannelInfo> channels) {
        this.mChannels = channels;
    }


    /**
     * Called when RecyclerView needs a new {@code ViewHolder} of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ChannelItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // Inflate the custom view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.view_channel_item, parent, false);

        // Wrap the view with the view holder
        return new ChannelItemViewHolder(view);
    }


    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@code ViewHolder.itemView} to reflect the item at the given
     * position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ChannelItemViewHolder holder, int position)
    {
        holder.setChannel(mChannels.get(position));
    }


    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount()
    {
        return this.mChannels.size();
    }
}

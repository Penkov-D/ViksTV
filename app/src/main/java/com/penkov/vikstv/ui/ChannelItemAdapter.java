package com.penkov.vikstv.ui;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.penkov.vikstv.R;
import com.penkov.vikstv.core.ChannelInfo;

import java.util.ArrayList;

public class ChannelItemAdapter
        extends RecyclerView.Adapter<ChannelItemViewHolder>
{
    // Channel list
    private final ArrayList<ChannelInfo> mChannels;

    // Item focused
    private static final int NO_FOCUS = -1;
    private int positionFocused = NO_FOCUS;
    private boolean mNeedScroll = false;

    // The recycler view of this adapter
    private RecyclerView mRecyclerView = null;


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
        return new ChannelItemViewHolder(this, view);
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


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);

        this.mRecyclerView = recyclerView;
        this.mRecyclerView.addOnScrollListener(new scrollContinuer());
    }

    /**
     * Set which item is now focused.
     *
     * @param position  the item adapter position
     * @param haveFocus true if the item got focus, false if it lost focus.
     */
    public void informFocused(int position, boolean haveFocus) {
        // If item got focus, mark it
        if (haveFocus)
            this.positionFocused = position;

            // If the focused item have no focus,
            //   then no item in focus.
        else if (this.positionFocused == position)
            this.positionFocused = NO_FOCUS;

        // Scroll to the position
        if (this.positionFocused == NO_FOCUS)
            return;

        if (this.mRecyclerView == null)
            return;

        // Smooth scroll if no scroll registered
        if (this.mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE)
            this.mRecyclerView.smoothScrollToPosition(this.positionFocused);

        // Wait for the scroll to end
        else this.mNeedScroll = true;
    }


    private class scrollContinuer extends RecyclerView.OnScrollListener
    {
        // TODO: comment

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
        {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState != RecyclerView.SCROLL_STATE_IDLE) return;
            if (positionFocused == NO_FOCUS) return;
            if (!mNeedScroll) return;

            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager == null) return;

            if (!(layoutManager instanceof LinearLayoutManager)) return;
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

            LinearSmoothScroller smoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext())
                    {
                        @Override
                        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                            return 4.0f;
                        }
                    };

            smoothScroller.setTargetPosition(positionFocused);
            layoutManager.startSmoothScroll(smoothScroller);
        }
    }
}

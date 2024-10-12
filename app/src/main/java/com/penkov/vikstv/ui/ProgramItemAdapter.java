package com.penkov.vikstv.ui;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.penkov.vikstv.R;
import com.penkov.vikstv.core.ChannelProgram;

import java.util.Calendar;

public class ProgramItemAdapter
        extends RecyclerView.Adapter<ProgramItemViewHolder>
{
    // TAG to use with logcat
    private static final String TAG = "ProgramItemAdapter";

    // The programs list
    private final ChannelProgram[] mChannelPrograms;
    private final ProgramItemViewHolder.ShowTime[] showTimes;

    private int mShowingPosition = 0;
    private View mShowingView = null;


    public ProgramItemAdapter(@NonNull ChannelProgram[] programs)
    {
        this.mChannelPrograms = programs;
        this.showTimes = new ProgramItemViewHolder.ShowTime[programs.length];

        timelineShows();
    }


    @NonNull
    @Override
    public ProgramItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // Inflate the custom view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.view_channel_program, parent, false);

        // Wrap the view with the view holder
        return new ProgramItemViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ProgramItemViewHolder holder, int position)
    {
        holder.setProgram(mChannelPrograms[position], showTimes[position]);

        if (position == this.mShowingPosition)
            this.mShowingView = holder.itemView;
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.post(() -> recyclerView.scrollToPosition(this.mShowingPosition));
    }

    @Override
    public int getItemCount()
    {
        return mChannelPrograms.length;
    }


    @SuppressLint("NotifyDataSetChanged")
    public void updateView(@NonNull RecyclerView recyclerView)
    {
        timelineShows();
        notifyDataSetChanged();

        // Scroll to showing program
        if (this.mChannelPrograms.length > 0)
        {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (!(layoutManager instanceof LinearLayoutManager)) return;
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

            // Scroll and leave on passed channel
            final int scrollPosition = Math.max(0, this.mShowingPosition - 1);
            recyclerView.post(() -> linearLayoutManager.scrollToPositionWithOffset(scrollPosition, 0));

            // Set the current program focus
            if (this.mShowingView != null)
                recyclerView.post(() -> mShowingView.requestFocus());
        }
    }


    /**
     * Set the programs state.
     */
    private void timelineShows()
    {
        // Can work only if there are programs...
        if (mChannelPrograms.length == 0)
            return;

        // Get current time
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minutes = Calendar.getInstance().get(Calendar.MINUTE);

        int time = hour * 60 + minutes;

        // Fix day leap
        if (mChannelPrograms[0].getCompactTime() > time)
            time += 24 * 60;

        // Found the desired channel.
        boolean found = false;
        // If the program time is of next day.
        boolean isNextDay = false;

        // First show default is shown.
        this.showTimes[0] = ProgramItemViewHolder.ShowTime.SHOW_PASSED;

        // Iterate the shows
        for (int i = 1; i < mChannelPrograms.length; i++)
        {
            // Check if day passed.
            isNextDay |= mChannelPrograms[i].getCompactTime()
                    < mChannelPrograms[i - 1].getCompactTime();

            if (found)
                // If current show found, all the rest are future shows.
                this.showTimes[i] = ProgramItemViewHolder.ShowTime.SHOW_FUTURE;

            else if (time >= mChannelPrograms[i].getCompactTime(isNextDay))
                // If the time is smaller, this show probably ended.
                this.showTimes[i] = ProgramItemViewHolder.ShowTime.SHOW_PASSED;

            else
            {
                // Show is in the future, hence previous show is playing
                found = true;

                this.mShowingPosition = i - 1;
                this.showTimes[i - 1] = ProgramItemViewHolder.ShowTime.SHOW_SHOWING;

                // current show is in the future
                this.showTimes[i] = ProgramItemViewHolder.ShowTime.SHOW_FUTURE;
            }
        }

        // If no show found, it must be the last
        if (!found)
        {
            this.showTimes[this.showTimes.length - 1] = ProgramItemViewHolder.ShowTime.SHOW_SHOWING;
            this.mShowingPosition = this.showTimes.length - 1;
        }
    }

}

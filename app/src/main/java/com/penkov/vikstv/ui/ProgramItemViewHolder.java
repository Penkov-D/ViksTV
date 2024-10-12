package com.penkov.vikstv.ui;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.penkov.vikstv.R;
import com.penkov.vikstv.core.ChannelProgram;

public class ProgramItemViewHolder
        extends RecyclerView.ViewHolder
{
    // The program this view is responsible for
    private ChannelProgram mProgram = null;

    // View values
    private final TextView mTimeTextView;
    private final TextView mProgramTextView;
    private final MaterialCardView mProgramCardView;

    // Default values
    private static final String DEFAULT_TIME = "--:--";
    private static final String DEFAULT_TEXT = "";


    /**
     * Construct new program view holder.
     *
     * @param itemView the view that this view holder manages.
     */
    public ProgramItemViewHolder(@NonNull View itemView)
    {
        super(itemView);

        // Set the view variables
        this.mTimeTextView = itemView.findViewById(R.id.textViewProgramTime);
        this.mProgramTextView = itemView.findViewById(R.id.textViewProgramDescription);
        this.mProgramCardView = itemView.findViewById(R.id.cardViewProgram);

        setDefault();
    }


    /**
     * Set the default characteristics of the program.
     */
    private void setDefault()
    {
        // Set default text
        this.mTimeTextView.setText(DEFAULT_TIME);
        this.mProgramTextView.setText(DEFAULT_TEXT);
    }


    /**
     * Set the program this view represents.
     * If the program is null, then default behavior is set.
     *
     * @param program the program to show.
     */
    public void setProgram(@Nullable ChannelProgram program, @NonNull ShowTime showTime)
    {
        // Store the program
        this.mProgram = program;

        // Set view attributes accordingly
        setDefault();
        if (program == null)
            return;

        this.mTimeTextView.setText(program.getTime());
        this.mProgramTextView.setText(program.getName());

        setColor(showTime);
    }


    /**
     * Set the color of the item based on time
     */
    private void setColor(@NonNull ShowTime showTime)
    {
        Resources res = itemView.getResources();

        switch (showTime)
        {
            case SHOW_PASSED:
                this.mProgramCardView.setCardBackgroundColor(
                        res.getColor(R.color.program_background_past));
                this.mProgramTextView.setTypeface(null, Typeface.NORMAL);
                this.mTimeTextView.setTypeface(null, Typeface.NORMAL);
                break;

            case SHOW_SHOWING:
                this.mProgramCardView.setCardBackgroundColor(
                        res.getColor(R.color.program_background_present));
                this.mProgramTextView.setTypeface(null, Typeface.BOLD);
                this.mTimeTextView.setTypeface(null, Typeface.BOLD);
                break;

            case SHOW_FUTURE:
                this.mProgramCardView.setCardBackgroundColor(
                        res.getColor(R.color.program_background_future));
                this.mProgramTextView.setTypeface(null, Typeface.BOLD);
                this.mTimeTextView.setTypeface(null, Typeface.BOLD);
                break;
        }
    }


    /**
     * The show current process
     */
    public static enum ShowTime
    {
        SHOW_PASSED,
        SHOW_SHOWING,
        SHOW_FUTURE,
    }
}

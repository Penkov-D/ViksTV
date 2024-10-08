package com.penkov.vikstv.ui;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.penkov.vikstv.R;
import com.penkov.vikstv.core.ChannelInfo;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    // Channel transferring key
    public static final String CHANNELS = "channel_list";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Basic view stuff
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the channel list
        ArrayList<ChannelInfo> channels = getIntent().getParcelableArrayListExtra(CHANNELS);

        // If the channel list is null - the activity wasn't called rightfully.
        if (channels == null) {
            finish();
            return;
        }

        // Start the UI
        loadChannels(channels);
    }


    /**
     * Call to define the UI of the channels (the RecyclerView).
     *
     * @param channels list of channels available.
     */
    private void loadChannels (@NonNull ArrayList<ChannelInfo> channels)
    {
        // Calculate number of columns for recycler view.
        Configuration configuration = getResources().getConfiguration();
        final int screenWidthDp = configuration.screenWidthDp;
        final int columns = screenWidthDp / (96 + 32 + 8);

        // Call the recycler view adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerView_channelList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, columns));
        recyclerView.setAdapter(new ChannelItemAdapter(channels));
    }
}
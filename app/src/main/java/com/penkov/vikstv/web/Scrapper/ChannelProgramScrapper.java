package com.penkov.vikstv.web.Scrapper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.penkov.vikstv.core.ChannelProgram;
import com.penkov.vikstv.core.ProgramTimeException;
import com.penkov.vikstv.web.Listener.ChannelProgramListener;
import com.penkov.vikstv.web.WebParsingException;
import com.penkov.vikstv.web.base.GeneralScrapper;

import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChannelProgramScrapper
    extends GeneralScrapper<ChannelProgram[], ChannelProgramListener>
{
    // TAG to use with logcat
    public static final String TAG = "ChannelProgramScrapper";

    /**
     * Create new scrapper.
     * This class responsible to retrieve {@code Result} from the server.
     *
     * @param URL the url to connect to.
     */
    public ChannelProgramScrapper(@NonNull String URL) {
        super(URL);
    }


    /**
     * Create new scrapper.
     * This class responsible to retrieve {@code Result} from the server.
     *
     * @param URL      the url to connect to.
     * @param listener register listener on creation.
     *                 Equal to call {@code registerListener()}.
     */
    public ChannelProgramScrapper(@NonNull String URL, @NonNull ChannelProgramListener listener) {
        super(URL, listener);
    }


    /**
     * Process the webpage response to find the desired piece of data.
     *
     * @param response the loaded webpage.
     * @return Result scrapped from the webpage.
     * @throws WebParsingException on parsing conflict.
     */
    @Override
    protected ChannelProgram[] processPage(@NonNull Connection.Response response)
            throws WebParsingException, IOException
    {
        // Get the programs table.
        Element body = response.parse().body();
        Element programsTable = body.getElementsByClass("epg_prog").first();

        // If the table wasn't found.
        if (programsTable == null)
            throw new WebParsingException("No program table was found!");

        // Get the program entries
        Elements programEntries = programsTable.getElementsByTag("li");
        List<ChannelProgram> channelProgramList =
                new ArrayList<ChannelProgram>(programEntries.size());

        // Parse the programs
        for (Element programEntry : programEntries)
        {
            // Parse the program
            ChannelProgram program = parseProgram(programEntry);

            // Add the program to the list
            if (program != null)
                channelProgramList.add(program);
        }

        // Convert list to array
        return channelProgramList.toArray(new ChannelProgram[0]);
    }


    /**
     * Scrap channel program entry from designated element.
     *
     * @param programElement the element to scrap the channel from.
     * @return object representing the program, or null on parsing error.
     */
    private @Nullable ChannelProgram parseProgram(@NonNull Element programElement)
    {
        // Get the program time and name elements.
        Element divTime = programElement.getElementsByClass("time").first();
        Element divName = programElement.getElementsByClass("prname2").first();

        // If the failed, can't make the channel program.
        if (divTime == null || divName == null) {
            Log.w(TAG, "Some information about the program is missing");
            return null;
        }

        // Get the text representing the time and name.
        final String time = divTime.text();
        final String name = divName.text();

        // Create the channel program object.
        ChannelProgram program = null;

        // Try to create the channel object.
        try {
            program = new ChannelProgram(name, time);
        }
        catch (ProgramTimeException e) {
            Log.w(TAG, "Creating program yield a error : ", e);
        }

        return program;
    }

}

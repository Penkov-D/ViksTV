package com.penkov.vikstv.web.Scraper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.penkov.vikstv.core.ChannelProgram;
import com.penkov.vikstv.core.ProgramTimeException;
import com.penkov.vikstv.web.Listener.ListenerChannelProgram;
import com.penkov.vikstv.web.WebParsingException;
import com.penkov.vikstv.web.base.GeneralScraper;

import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScraperChannelProgram
    extends GeneralScraper<ChannelProgram[], ListenerChannelProgram>
{
    // TAG to use with logcat.
    public static final String TAG = ScraperChannelProgram.class.getSimpleName();

    // Constant variables used in the scraping.
    private static final String DIV_PROGRAMS = "epg_prog";
    private static final String LIST_TAG = "li";


    /**
     * Create new scraper.
     * This class responsible to retrieve the channel programs from the server.
     *
     * @param URL the url to connect to.
     */
    public ScraperChannelProgram(@NonNull String URL) {
        super(URL);
    }


    /**
     * Create new scrapper.
     * This class responsible to retrieve the channel programs from the server.
     *
     * @param URL      the url to connect to.
     * @param listener register listener on creation. <br>
     *                 Equal to call {@code registerListener()}.
     */
    public ScraperChannelProgram(@NonNull String URL, @NonNull ListenerChannelProgram listener) {
        super(URL, listener);
    }


    @Override
    protected ChannelProgram[] processPage(@NonNull Connection.Response response)
            throws WebParsingException, IOException
    {
        // Get the programs table.
        Element body = response.parse().body();
        Element programsTable = body.getElementsByClass(DIV_PROGRAMS).first();

        // Check if the program table was found.
        if (programsTable == null)
            throw new WebParsingException("No program table was found.");

        // Get the program entries.
        Elements programEntries = programsTable.getElementsByTag(LIST_TAG);
        List<ChannelProgram> channelProgramList = new ArrayList<>(programEntries.size());

        // Parse the programs.
        for (Element programEntry : programEntries)
        {
            // Parse the program.
            ChannelProgram program = parseProgram(programEntry);

            // Add the program to the list.
            if (program != null)
                channelProgramList.add(program);
        }

        // Convert list to array.
        return channelProgramList.toArray(new ChannelProgram[0]);
    }


    private static final String CLASS_TIME = "time";
    private static final String CLASS_NAME = "prname2";


    /**
     * Scrap channel program entry from designated element.
     *
     * @param programElement the element to scrap the channel from.
     * @return object representing the program, or null on parsing error.
     */
    private @Nullable ChannelProgram parseProgram(@NonNull Element programElement)
    {
        // Get the program time and name elements.
        Element divTime = programElement.getElementsByClass(CLASS_TIME).first();
        Element divName = programElement.getElementsByClass(CLASS_NAME).first();

        // If one element failed, can't make the channel program.
        if (divTime == null || divName == null) {
            Log.w(TAG, "Some information about the program is missing.");
            return null;
        }

        // Get the text representing the time and name.
        final String time = divTime.text();
        final String name = divName.text();

        ChannelProgram program = null;

        // Try to create the channel object.
        try {
            program = new ChannelProgram(name, time);
        }
        catch (ProgramTimeException e) {
            Log.w(TAG, "Creating program yield a error.", e);
        }

        return program;
    }

}

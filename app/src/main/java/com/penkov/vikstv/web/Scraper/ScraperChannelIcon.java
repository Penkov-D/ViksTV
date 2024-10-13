package com.penkov.vikstv.web.Scraper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.penkov.vikstv.web.Listener.ListenerChannelIcon;
import com.penkov.vikstv.web.WebParsingException;
import com.penkov.vikstv.web.base.GeneralScraper;

import org.jsoup.Connection;

public class ScraperChannelIcon
    extends GeneralScraper<Bitmap, ListenerChannelIcon>
{
    // TAG to use with logcat.
    public static final String TAG = ScraperChannelIcon.class.getSimpleName();


    /**
     * Create new scraper.
     * This class responsible to retrieve channel icon from the server.
     *
     * @param URL the url to connect to.
     */
    public ScraperChannelIcon(@NonNull String URL) {
        super(URL);
    }


    /**
     * Create new scrapper.
     * This class responsible to retrieve channel icon from the server.
     *
     * @param URL      the url to connect to.
     * @param listener register listener on creation. <br>
     *                 Equal to call {@code registerListener()}.
     */
    public ScraperChannelIcon(
            @NonNull String URL, @NonNull ListenerChannelIcon listener)
    {
        super(URL, listener);
    }


    @Override
    protected Bitmap processPage(@NonNull Connection.Response response)
            throws WebParsingException
    {
        // Read the images as stream, and pass it to the bitmap factory.
        Bitmap icon = BitmapFactory.decodeStream(response.bodyStream());

        if (icon == null)
            throw new WebParsingException("Couldn't decode the channel icon.");

        return icon;
    }
}

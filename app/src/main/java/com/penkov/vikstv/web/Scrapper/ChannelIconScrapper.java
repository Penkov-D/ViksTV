package com.penkov.vikstv.web.Scrapper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.penkov.vikstv.web.Listener.ChannelIconListener;
import com.penkov.vikstv.web.WebParsingException;
import com.penkov.vikstv.web.base.GeneralScrapper;

import org.jsoup.Connection;

import java.io.IOException;

public class ChannelIconScrapper
    extends GeneralScrapper<Bitmap, ChannelIconListener>
{
    // TAG to use with logcat
    public static final String TAG = "ChannelIconScrapper";

    /**
     * Create new scrapper.
     * This class responsible to retrieve {@code Result} from the server.
     *
     * @param URL the url to connect to.
     */
    public ChannelIconScrapper(@NonNull String URL) {
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
    public ChannelIconScrapper(
            @NonNull String URL, @NonNull ChannelIconListener listener) {
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
    protected Bitmap processPage(@NonNull Connection.Response response)
            throws WebParsingException, IOException
    {
        // Read the images as stream, and pass it to the bitmap factory.
        return BitmapFactory.decodeStream(response.bodyStream());
    }
}

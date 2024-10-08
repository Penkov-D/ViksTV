package com.penkov.vikstv.web.Scrapper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.penkov.vikstv.core.ChannelInfo;
import com.penkov.vikstv.web.Listener.ChannelListListener;
import com.penkov.vikstv.web.WebParsingException;
import com.penkov.vikstv.web.base.GeneralScrapper;

import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelListScrapper
    extends GeneralScrapper<ChannelInfo[], ChannelListListener>
{
    // TAG to use with logcat
    public static final String TAG = "ChannelListScrapper";

    // The url to get the channel list
    public static final String URL = "http://ip.viks.tv";

    // Constants used by the parser
    private static final String LINK_TAG = "a";
    private static final String LINK_ATTR = "href";
    private static final String ICON_TAG = "img";
    private static final String ICON_ATTR = "src";


    /**
     * Create new channel list scrapper.
     * This class responsible to retrieve all the channels available from the server.
     */
    public ChannelListScrapper() {
        super(URL);
    }


    /**
     * Create new channel list scrapper.
     * This class responsible to retrieve all the channels available from the server.
     *
     * @param listener register listener on creation.
     *                 Equal to call {@code registerListener()}.
     */
    public ChannelListScrapper(@NonNull ChannelListListener listener) {
        super(URL, listener);
    }


    /**
     * Try and convert the webpage to list of all the channels.
     *
     * @param response the webpage to process.
     * @throws WebParsingException when the parsing didn't succeed from any reason.
     */
    protected ChannelInfo[] processPage(@NonNull Connection.Response response)
            throws WebParsingException, IOException
    {
        // Extract all channels by element
        Elements channelsDiv = response.parse().getElementsByClass("all_tv");
        if (channelsDiv.isEmpty()) {
            Log.e(TAG, "Scrapping didn't find channels list.");
            throw new WebParsingException("No channels found, parsing error?");
        }

        // Store all the channels, in dynamic size, in case some fail.
        List<ChannelInfo> channelInfoList = new ArrayList<>(channelsDiv.size());

        // Iterate the channels objects
        for (Element channelElement : channelsDiv)
        {
            // Parse the channel
            ChannelInfo channel = parseChannel(channelElement);

            // Add the new channel to the list
            if (channel != null)
                channelInfoList.add(channel);
        }

        // If everything worked right, store the channels as array.
        return channelInfoList.toArray(new ChannelInfo[0]);
    }


    /**
     * Parse from HTML element, the channel information.
     *
     * @param channelElement the element to scrap the channel from.
     * @return ChannelInfo presenting the channel, or null on parsing error.
     */
    private @Nullable ChannelInfo parseChannel(@NonNull Element channelElement)
    {

        // The element should contain only the channel name.
        final String channelName = channelElement.text();


        // Get the link that should direct to the channel page.
        Element linkElement = channelElement.getElementsByTag(LINK_TAG).first();
        if (linkElement == null || !linkElement.hasAttr(LINK_ATTR)) {
            Log.v(TAG, "Found element without link: " + channelElement.outerHtml());
            return null;
        }

        final String channelPage = URL + linkElement.attr(LINK_ATTR);


        // Get the link that should direct to the channel icon.
        Element imageElement = channelElement.getElementsByTag(ICON_TAG).first();
        if (imageElement == null || !imageElement.hasAttr(ICON_ATTR)) {
            Log.v(TAG, "Found element without icon: " + channelElement.outerHtml());
            return null;
        }

        final String channelIcon = URL + imageElement.attr(ICON_ATTR);


        // Add the new channel to the list
        return new ChannelInfo(channelName, channelPage, channelIcon);
    }


    /**
     * Get the channels list, but this time as {@code ArrayList}.
     *
     * @return non-null list of channels.
     */
    public @NonNull ArrayList<ChannelInfo> getChannelsArrayList()
    {
        // Store the channels pointer, as the object may change mid-running.
        ChannelInfo[] channels = this.getResult();

        // If no channels are loaded, return empty list.
        if (channels == null)
            return new ArrayList<>();

        // Convert the array to a list.
        return new ArrayList<>(Arrays.asList(channels));
    }
}

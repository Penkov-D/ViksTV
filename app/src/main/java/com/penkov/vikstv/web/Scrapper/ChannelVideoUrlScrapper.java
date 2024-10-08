package com.penkov.vikstv.web.Scrapper;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;

import com.penkov.vikstv.R;
import com.penkov.vikstv.web.Listener.ChannelVideoUrlListener;
import com.penkov.vikstv.web.WebParsingException;
import com.penkov.vikstv.web.base.GeneralScrapper;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelVideoUrlScrapper
    extends GeneralScrapper<String, ChannelVideoUrlListener>
{
    // TAG to use with logcat
    public static final String TAG = "ChannelVideoUrlScrapper";

    // Constants that used in parsing
    private static final String TAG_SCRIPT = "script";
    private static final String SCRIPT_CONST = "Playerjs";

    private static final String KODK_PATTERN = "var kodk=\"([^\"]*)\";";
    private static final String KOS_PATTERN = "var kos=\"([^\"]*)\";";
    private static final String PLAYERJS_PATTERN =
            "var player=new Playerjs\\(\\{id:\"preroll\",file:\"([^\"]*)\"\\}\\);";

    private final String[] keys;


    /**
     * Create new channel list scrapper.
     * This class responsible to retrieve all the channels available from the server.
     *
     * @param URL the channel page url.
     */
    public ChannelVideoUrlScrapper(
            @NonNull Context context, @NonNull String URL)
    {
        super(URL);
        this.keys = loadKeys(context);
    }


    /**
     * Create new channel list scrapper.
     * This class responsible to retrieve all the channels available from the server.
     * Assigns automatically a listener. Equal to call {@code registerListener()}.
     *
     * @param URL the channel page url.
     * @param listener the listener to assign.
     */
    public ChannelVideoUrlScrapper(@NonNull Context context,
                                   @NonNull String URL,
                                   @NonNull ChannelVideoUrlListener listener)
    {
        super(URL, listener);
        this.keys = loadKeys(context);
    }


    /**
     * Load the keys from inside the resources.
     * They keys are store in "res/values/secrets.xml",
     * which is excluded from this repository.
     *
     * @param context the context of the application.
     * @return string array of the keys.
     */
    private String[] loadKeys(@NonNull Context context)
    {
        // Default return value (in case not found) new empty array.
        String[] keys = new String[0];

        try {
            // Get the keys from resources.
            Resources res = context.getResources();
            keys = res.getStringArray(R.array.keys);
        }
        catch (Resources.NotFoundException e) {
            // In case of error, print error log.
            Log.e(TAG, "Keys not found !");
        }

        return keys;
    }


    /**
     * Extract the obfuscated video url from the page.
     *
     * @param response the loaded webpage.
     * @return String representing the video url decoded.
     * @throws WebParsingException if there was parsing conflict.
     * @throws IOException in case there was general error.
     */
    @Override
    protected String processPage(@NonNull Connection.Response response)
            throws WebParsingException, IOException
    {
        // Get the HTML body of the page
        Document document = response.parse();

        // Script that controls the video
        String videoScript = null;

        // Search for the script that manages the video player
        for (Element element : document.getElementsByTag(TAG_SCRIPT))
        {
            // Check if the script responsible for the video.
            if (element.toString().contains(SCRIPT_CONST)) {
                videoScript = element.toString();
                break;
            }
        }


        // If no script - no url
        if (videoScript == null)
            throw new WebParsingException("Couldn't find script responsible for the video.");

        // Part of the url used in the process
        final String kodk = getRegexObject(videoScript, KODK_PATTERN);
        final String kos =  getRegexObject(videoScript, KOS_PATTERN);

        // Obfuscated key
        String obf_key = getRegexObject(videoScript, PLAYERJS_PATTERN);


        // Create the keys used inside the
        final String[] keys_base64 = new String[keys.length];

        for (int i = 0; i < keys.length; i++)
            keys_base64[i] = "F" + Base64.getEncoder().encodeToString(keys[i].getBytes());


        // Decode the string twice
        obf_key = decodePlayerJS(obf_key, keys_base64);
        obf_key = decodePlayerJS(obf_key, keys_base64);

        // Replace the the placeholders with the constants found earlier.
        return obf_key
                .replace("{v1}", kodk)
                .replace("{v2}", kos);
    }


    /**
     * Decode the key string - the core function from the PlayerJS library.
     * <p>
     * Return after me: Base64 *clap* is *clap* not *clap* an *clap* encryption.
     *
     * @param key      the original key to decode.
     * @param keys_b64 list of keys used to decode.
     * @return string of decoded key.
     * @throws WebParsingException If part of the decoding failed.
     */
    private @NonNull String decodePlayerJS (@NonNull String key, @NonNull String[] keys_b64)
            throws WebParsingException
    {
        // Remove first two characters
        key = key.substring(2);

        // Remove the keys
        for (int i = keys_b64.length - 1; i >= 0; i--)
            key = key.replace(keys_b64[i], "");

        // Decode the base64 string
        try {
            key = new String(Base64.getDecoder().decode(key));
        }
        catch (IllegalArgumentException e) {
            // If decoding was unsuccessful
            throw new WebParsingException("Decoding the key didn't yield base64 string");
        }

        return key;
    }


    /**
     * Return text that is captured by regular expression.
     *
     * @param text    The text to search within.
     * @param pattern The pattern to search inside the text.
     * @return The group that the patterns captures.
     * @throws WebParsingException If no capture was made.
     */
    private @NonNull String getRegexObject(@NonNull String text, @NonNull String pattern)
        throws WebParsingException
    {
        // Regex the text
        Matcher matcher = Pattern.compile(pattern).matcher(text);

        // If no match, throw parsing error
        if (!matcher.find() || matcher.groupCount() != 1)
            throw new WebParsingException("No text matches pattern: " + pattern);

        // Pattern is set to more then one or no groups at all
        if (matcher.groupCount() != 1)
            throw new WebParsingException("Pattern must capture only one group: " + pattern);

        return Objects.requireNonNull(matcher.group(1));
    }
}

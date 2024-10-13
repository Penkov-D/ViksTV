package com.penkov.vikstv.web.base;

import android.util.Log;

import androidx.annotation.NonNull;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

public class BackgroundScraper
{
    // TAG to use on logcat.
    public static final String TAG = BackgroundScraper.class.getSimpleName();

    // Thread to make the web communication in the background.
    // Also, android doesn't allow network communication on GUI thread.
    private final Thread backgroundHTTPScrapper;

    // The URL to make the connections on.
    private final String URL;

    // Event listener
    private final BackgroundScraperListener scraperListener;


    /**
     * Create new background scrapper, and initiate the web request.
     * All communication with this class are from now on over the {@code BackgroundScraperListener}.
     *
     * @param URL           The address to get the webpage from.
     * @param eventListener The event handler to manage the events from this server.
     */
    public BackgroundScraper(@NonNull String URL, @NonNull BackgroundScraperListener eventListener)
    {
        // Save local variables
        this.scraperListener = eventListener;
        this.URL = URL;

        // Create and start the background thread.
        this.backgroundHTTPScrapper = new Thread(this::backgroundScrapper);
        this.backgroundHTTPScrapper.start();
    }


    /**
     * Check if the scraping thread finished (the network communication is done).
     *
     * @return true if the scraping thread is dead, false otherwise.
     */
    public synchronized boolean isFinished() {
        return !this.backgroundHTTPScrapper.isAlive();
    }


    /**
     * The core function that is called in a parallel thread.
     */
    private void backgroundScrapper()
    {
        Log.v(TAG, "Starting web connection.");

        try {
            // Retrieve the webpage.
            Connection.Response response = Jsoup
                    .connect(URL)
                    .ignoreContentType(true)
                    .method(Connection.Method.GET)
                    .execute();
            Log.v(TAG, "Successfully got the web page.");

            // Call the listener with the webpage.
            this.scraperListener.onDocument(response);
            Log.v(TAG, "Finished processing the document.");
        }

        // Exception if the URL is not right.
        catch (MalformedURLException e) {
            Log.w(TAG, "Exception in URL.", e);
            this.scraperListener.onError(e);
        }

        // Exception when the connection occurred, but the server return anomalous status.
        catch (HttpStatusException e) {
            Log.w(TAG, "Exception in HTTP return status.", e);
            this.scraperListener.onError(e);
        }

        // Exception when the media type of the document is not supported (usually not HTML page).
        catch (UnsupportedMimeTypeException e) {
            Log.w(TAG, "Exception in Mime type.", e);
            this.scraperListener.onError(e);
        }

        // Exception when the server didn't answer the network request.
        catch (SocketTimeoutException e) {
            Log.w(TAG, "Exception socket timeout.", e);
            this.scraperListener.onError(e);
        }

        // General exception.
        catch (IOException e) {
            Log.w(TAG, "Exception in IO.", e);
            this.scraperListener.onError(e);
        }
    }
}

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

public class BackgroundScrapper
{
    // TAG to use on logcat
    public static final String TAG = "BackgroundScrapper";

    // Thread to make the web communication in the background.
    // Also, android doesn't allow network communication on GUI thread.
    private final Thread backgroundHTTPScrapper;

    // The URL to make the connections on
    private final String URL;

    // Event listener
    private final BackgroundScrapperListener backgroundHTTPEvent;


    /**
     * Create new background scrapper, and initiate the web request.
     * All communication with this class are from now on over the {@code BackgroundScrapperEvent}
     *
     * @param URL           The address to get the webpage from.
     * @param eventListener The event handler to manage the events from this server.
     */
    public BackgroundScrapper(@NonNull String URL, @NonNull BackgroundScrapperListener eventListener)
    {
        // Make local variables
        this.backgroundHTTPEvent = eventListener;
        this.URL = URL;

        // Create and start the background thread.
        this.backgroundHTTPScrapper = new Thread(this::backgroundScrapper);
        this.backgroundHTTPScrapper.start();
    }


    /**
     * Check if the thread is finished - meaning the network communication is done,
     * and also the listener process was done.
     *
     * @return true if the thread is dead, false otherwise.
     */
    public synchronized boolean isFinished() {
        return !this.backgroundHTTPScrapper.isAlive();
    }


    /**
     * The core function that is called in parallel thread.
     */
    private void backgroundScrapper()
    {
        Log.v(TAG, "Starting web connection");

        try {
            // Retrieve the webpage
            Connection.Response response = Jsoup
                    .connect(URL)
                    .ignoreContentType(true)
                    .method(Connection.Method.GET)
                    .execute();
            Log.v(TAG, "Successfully got the web page");

            // Call the listener with the webpage
            this.backgroundHTTPEvent.onDocument(response);
            Log.v(TAG, "Finished processing the document");
        }
        // Exception if the URL is not right
        catch (MalformedURLException e) {
            Log.e(TAG, "Error in URL name!", e);
            this.backgroundHTTPEvent.onError(e);
        }
        // Exception when the connection occurred, but the server return anomalous status number.
        catch (HttpStatusException e) {
            Log.e(TAG, "Error HTTP status!", e);
            this.backgroundHTTPEvent.onError(e);
        }
        // Exception when the media type of the document is not supported (not HTML page usually).
        catch (UnsupportedMimeTypeException e) {
            Log.e(TAG, "Error in Mime Type!", e);
            this.backgroundHTTPEvent.onError(e);
        }
        // Exception when the server didn't answer the network request.
        catch (SocketTimeoutException e) {
            Log.e(TAG, "Error socket timeout!", e);
            this.backgroundHTTPEvent.onError(e);
        }
        // General exception.
        catch (IOException e) {
            Log.e(TAG, "Error in IO!", e);
            this.backgroundHTTPEvent.onError(e);
        }
    }
}

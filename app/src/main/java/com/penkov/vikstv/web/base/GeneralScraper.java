package com.penkov.vikstv.web.base;

import android.util.Log;

import androidx.annotation.NonNull;

import com.penkov.vikstv.web.WebParsingException;

import org.jsoup.Connection;

import java.io.IOException;

public abstract class GeneralScraper<Result, ListenerClass extends GeneralListener<Result>>
{
    // TAG to use on logcat.
    public static final String TAG = GeneralScraper.class.getSimpleName();

    // The URL to connect to.
    private final String mURL;

    // Flag whether a scraper is running.
    private boolean mBackgroundThreadRunning = false;

    // Holds the listener.
    private ListenerClass mListener = null;
    private final Object mListenerLock = new Object();

    // Hold the result for caching.
    private Result mResult = null;


    /**
     * Create new scraper.
     * This class responsible to retrieve {@code Result} from the server.
     *
     * @param URL the web page to connect to.
     */
    public GeneralScraper(@NonNull String URL)
    {
        this.mURL = URL;
    }


    /**
     * Create new scraper.
     * This class responsible to retrieve {@code Result} from the server.
     *
     * @param URL      the web page to connect to.
     * @param listener register listener on creation. <br>
     *                 Equal to call {@code registerListener()}.
     */
    public GeneralScraper(@NonNull String URL, @NonNull ListenerClass listener)
    {
        this.registerListener(listener);
        this.mURL = URL;
    }


    /**
     * Check if the thread is running.
     * To initiate the thread, call {@code load()}.
     *
     * @return true if the thread is currently running, false otherwise.
     */
    public final boolean isRunning() {
        return this.mBackgroundThreadRunning;
    }


    /**
     * Start network request to get {@code Result}.
     * If a listener is register up until the request is done and processed,
     * then he will be informed.
     * <p>
     * Calling this method while a request is still working, will result in no operation.
     * The {@code isRunning()} method should help determining that.
     */
    public final void load()
    {
        // If the scrapping running, do notting.
        if (this.mBackgroundThreadRunning) {
            Log.i(TAG, "Try to initiate new thread while one is already on.");
            return;
        }

        // Set the flag.
        this.mBackgroundThreadRunning = true;
        Log.v(TAG, "Starting the background thread.");

        // Run the network request.
        new BackgroundScraper(this.mURL, new BackgroundListener());
    }


    /**
     * Class that manages the background listener.
     */
    private final class BackgroundListener
            implements BackgroundScraperListener
    {
        /**
         * Called when the document is loaded successfully.
         *
         * @param response the web page asked from the scrapper.
         */
        @Override
        public void onDocument(@NonNull Connection.Response response)
        {
            try {
                // Parse the response.
                Log.v(TAG, "Start parsing the page.");

                mResult = processPage(response);
                callListenerOnResult(mResult);

                // Clear the flag.
                Log.v(TAG, "Server is shutting down after successful scrap.");
                mBackgroundThreadRunning = false;
            }
            catch (Exception e) {
                // maybe processChannels() will throw an error.
                // To not duplicate code, call the onError() method.
                Log.v(TAG, "Error while parsing the page.");
                this.onError(e);
            }
        }

        /**
         * Called on general error from the network communication.
         *
         * @param exception describes the problem.
         */
        @Override
        public void onError(@NonNull Exception exception)
        {
            // Inform the listener about an error.
            callListenerOnError(exception);

            // Clear the flag.
            Log.v(TAG, "Server is shutting down after unsuccessful scrap.");
            mBackgroundThreadRunning = false;
        }
    }


    /**
     * Process the webpage response to find the desired piece of data.
     *
     * @param response the loaded webpage.
     * @return Result scraped from the webpage.
     *
     * @throws WebParsingException on parsing conflict.
     */
    protected abstract Result processPage(@NonNull Connection.Response response)
            throws WebParsingException, IOException;


    /**
     * Return the last retrieved result.
     * This method does not create the loading operation itself,
     * for that call the {@code load()} method.
     *
     * @return Last {@code Result} loaded, or null if no {@code Result} yet loaded.
     */
    public final Result getResult() {
        return this.mResult;
    }


    /**
     * Register a listener over this scrapper.
     * If a listener is already persists, it will be replace by the new one.
     * Changing listener before its execution guarantee that the previous
     * one will not be called.
     *
     * @param listener the new listener object to set.
     */
    public final void registerListener (@NonNull ListenerClass listener)
    {
        synchronized (this.mListenerLock)
        {
            // Set the listener
            Log.v(TAG, "Set new listener.");
            this.mListener = listener;
        }
    }


    /**
     * Remove the listener from this scrapper (if presented).
     * If no listener was registered, notting is done.
     * This method guarantee that after it execution the previous listener will not be called.
     */
    public final void removeListener ()
    {
        synchronized (this.mListenerLock)
        {
            // Remove the listener
            Log.v(TAG, "Remove listener.");
            mListener = null;
        }
    }


    /**
     * Calls the listener with result over the {@code onResult()} method
     * if a listener present, otherwise do notting.
     *
     * @param result the parameter to pass to {@code onResult()}.
     */
    protected final void callListenerOnResult(@NonNull Result result)
    {
        synchronized (this.mListenerLock)
        {
            if (this.mListener != null)
            {
                // Call the listener.
                Log.v(TAG, "Call listener onResult.");
                this.mListener.onResult(result);
            }
        }
    }


    /**
     * Calls the listener with exception over the {@code onError()} method
     * if a listener present, otherwise do notting.
     *
     * @param exception the parameter to pass to {@code onError()}.
     */
    protected final void callListenerOnError(@NonNull Exception exception)
    {
        synchronized (this.mListenerLock)
        {
            if (this.mListener != null)
            {
                // Call the listener.
                Log.v(TAG, "Call listener on Error.");
                this.mListener.onError(exception);
            }
        }
    }
}

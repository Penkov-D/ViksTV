package com.penkov.vikstv.web.base;

import androidx.annotation.NonNull;

import org.jsoup.Connection;

public interface BackgroundScraperListener
{
    /**
     * Called when the document is loaded successfully.
     *
     * @param response the web page asked from the scraper.
     */
    void onDocument(@NonNull Connection.Response response);

    /**
     * Called on general error from the network communication.
     * When this method is called, don't expect {@code onDocument()} to be called.
     * <p>
     * The exception can be:
     * <ul>
     *     <li> {@code MalformedURLException} </li>
     *     <li> {@code HttpStatusException} </li>
     *     <li> {@code UnsupportedMimeTypeException} </li>
     *     <li> {@code SocketTimeoutException} </li>
     *     <li> {@code IOException} </li>
     * </ul>
     *
     * @param exception describes the problem.
     */
    void onError(@NonNull Exception exception);
}

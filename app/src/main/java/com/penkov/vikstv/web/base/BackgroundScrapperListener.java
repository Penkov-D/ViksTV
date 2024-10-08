package com.penkov.vikstv.web.base;

import androidx.annotation.NonNull;

import org.jsoup.Connection;

public interface BackgroundScrapperListener
{
    /**
     * Called when the document is loaded successfully.
     *
     * @param response the web page asked from the scrapper.
     */
    void onDocument(@NonNull Connection.Response response);

    /**
     * Called on general error from the network communication.
     * When this method is called, don't expect {@code onDocument()}
     * to be called.
     * <p>
     * The exception can be: {@code MalformedURLException}, {@code HttpStatusException},
     *   {@code UnsupportedMimeTypeException}, {@code SocketTimeoutException} or {@code IOException}.
     *
     * @param exception exception that describes the problem
     */
    void onError(@NonNull Exception exception);
}

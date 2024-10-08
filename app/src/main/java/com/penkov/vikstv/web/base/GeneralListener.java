package com.penkov.vikstv.web.base;

import androidx.annotation.NonNull;

public interface GeneralListener<Result>
{
    /**
     * Called when the parsing is done successfully.
     *
     * @param result the result of the parsing whatever type it is.
     */
    void onResult(@NonNull Result result);

    /**
     * Called on general error from the network communication.
     * When this method is called, don't expect {@code onResult()}
     * to be called.
     * <p>
     * The exception can be: {@code MalformedURLException}, {@code HttpStatusException},
     *   {@code UnsupportedMimeTypeException}, {@code SocketTimeoutException}, {@code IOException},
     *   or {@code WebParsingException}
     *
     * @param exception exception that describes the problem.
     */
    void onError(@NonNull Exception exception);
}

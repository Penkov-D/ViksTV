package com.penkov.vikstv.web.base;

import androidx.annotation.NonNull;

public interface GeneralListener<Result>
{
    /**
     * Called when the parsing is done successfully.
     *
     * @param result the result of the parsing.
     */
    void onResult(@NonNull Result result);

    /**
     * Called on general error from the network communication.
     * When this method is called, don't expect {@code onResult()} to be called.
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

package com.penkov.vikstv.web;

/**
 * Error indication unexpected data when processing a web page.
 */
public class WebParsingException extends Exception
{
    public WebParsingException () {
        super ();
    }

    public WebParsingException (String message) {
        super (message);
    }

    public WebParsingException (Throwable cause) {
        super (cause);
    }

    public WebParsingException (String message, Throwable cause) {
        super (message, cause);
    }
}

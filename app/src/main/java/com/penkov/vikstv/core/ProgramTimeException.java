package com.penkov.vikstv.core;

/**
 * Error indication that text representing program time is invalid.
 */
public class ProgramTimeException extends Exception
{
    public ProgramTimeException () {
        super ();
    }

    public ProgramTimeException (String message) {
        super (message);
    }

    public ProgramTimeException (Throwable cause) {
        super (cause);
    }

    public ProgramTimeException (String message, Throwable cause) {
        super (message, cause);
    }
}
package com.penkov.vikstv.core;

import androidx.annotation.NonNull;

public class ChannelProgram
{
    // The channel program name. Note: it can be non-english.
    private final @NonNull String mProgramName;

    // The channel program time, in format "HH:MM"
    private final @NonNull String mProgramTime;

    // The channel program time divided to hours and minutes.
    private final int mProgramTimeHour;
    private final int mProgramTimeMinute;


    /**
     * Create class representing program information.
     *
     * @param programName the program name.
     * @param programTime the program time.
     * @throws ProgramTimeException if the program time is not in the right format.
     */
    public ChannelProgram(@NonNull String programName, @NonNull String programTime)
            throws ProgramTimeException
    {
        // Set texts
        this.mProgramName = programName;
        this.mProgramTime = programTime;

        // Check if time is in the right format.
        if (!isTimeFormatValid(programTime))
            throw new ProgramTimeException("Time is not in right format.");

        // Set numbers
        this.mProgramTimeHour = getTimeHours(programTime);
        this.mProgramTimeMinute = getTimeSeconds(programTime);
    }


    /**
     * Check if string is in format of a time string. ("HH:MM")
     *
     * @param programTime the string in question.
     * @return true if the string is valid time, false otherwise.
     */
    private boolean isTimeFormatValid (@NonNull String programTime)
    {
        // Check string in possible length
        if (programTime.length() != 5)
            return false;

        // Check middle character is semicolon
        if (programTime.charAt(2) != ':')
            return false;

        // Check first two characters are digits
        if (!Character.isDigit(programTime.charAt(0)) ||
            !Character.isDigit(programTime.charAt(1)))
            return false;

        // Check last two characters are digits
        if (!Character.isDigit(programTime.charAt(3)) ||
            !Character.isDigit(programTime.charAt(4)))
            return false;

        return true;
    }


    /**
     * Given program time in text format, return the hours number.
     *
     * @param programTime string representing the program time.
     * @return integer presenting the hour of the program.
     * @throws ProgramTimeException in case the string is not representing program time.
     */
    private int getTimeHours (@NonNull String programTime)
            throws ProgramTimeException
    {
        try {
            return Integer.parseInt(programTime.substring(0, 2));
        }
        catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new ProgramTimeException("Program time is invalid", e);
        }
    }


    /**
     * Given program time in text format, return the minutes number.
     *
     * @param programTime string representing the program time.
     * @return integer presenting the minutes of the program.
     * @throws ProgramTimeException in case the string is not representing program time.
     */
    private int getTimeSeconds (@NonNull String programTime)
            throws ProgramTimeException
    {
        try {
            return Integer.parseInt(programTime.substring(3, 5));
        }
        catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new ProgramTimeException("Program time is invalid", e);
        }
    }


    /**
     * Get the program name.
     *
     * @return string presenting the program name.
     */
    @NonNull
    public String getName() {
        return mProgramName;
    }


    /**
     * Get the program time.
     *
     * @return string presenting the program name.
     */
    @NonNull
    public String getTime() {
        return mProgramTime;
    }


    /**
     * Get the program time hour.
     *
     * @return integer presenting the program hour.
     */
    public int getHour() {
        return mProgramTimeHour;
    }


    /**
     * Get the program time minute.
     *
     * @return integer presenting the program minute.
     */
    public int getMinute() {
        return mProgramTimeMinute;
    }
}

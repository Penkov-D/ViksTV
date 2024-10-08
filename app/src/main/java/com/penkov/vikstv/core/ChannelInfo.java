package com.penkov.vikstv.core;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class ChannelInfo implements Parcelable
{
    // The channel name. Note: it can be non-english. (e.g. "TET")
    private final @NonNull String mChannelName;

    // The channel page reference. (e.g. "http://ip.viks.tv/447-tet_11.html")
    private final @NonNull String mChannelReference;

    // The channel icon reference. (e.g. "http://ip.viks.tv/posts/2022-08/1659824773_tet.png")
    private final @NonNull String mChannelIconReference;


    /**
     * Create decorator class about basic information on a channel,
     * including the channel name, url to the channel page,
     * and an url to the channel icon.
     *
     * @param channelName The channel name.
     * @param channelReference The channel address (url) to the channel page.
     * @param channelIconReference The channel icon address (url).
     */
    public ChannelInfo(@NonNull String channelName,
                       @NonNull String channelReference,
                       @NonNull String channelIconReference)
    {
        this.mChannelName = channelName;
        this.mChannelReference = channelReference;
        this.mChannelIconReference = channelIconReference;
    }

    /**
     * Get the channel name. e.g. {@code History 2}.
     *
     * @return string representing the channel name.
     */
    public @NonNull String getChannelName() {
        return this.mChannelName;
    }

    /**
     * Get the address to the channel page. e.g {@code http://ip.viks.tv/601-history_5.html}
     *
     * @return string representing the channel page url.
     */
    public @NonNull String getChannelReference() {
        return this.mChannelReference;
    }

    /**
     * Get the address to the channel icon. e.g. {@code http://ip.viks.tv/posts/2019-12/1576493915_history_2.png}
     *
     * @return string representing the channel icon url.
     */
    public @NonNull String getChannelIconReference() {
        return this.mChannelIconReference;
    }


    /* ****************************** Parcelable ****************************** */

    /**
     * Constructor for parcelable objects.
     * Not a must, but a standard and easy solution and implementation.
     *
     * @param in the parcel object to construct the object from.
     */
    protected ChannelInfo(Parcel in)
    {
        mChannelName = Objects.requireNonNull(in.readString());
        mChannelReference = Objects.requireNonNull(in.readString());
        mChannelIconReference = Objects.requireNonNull(in.readString());
    }

    /**
     * Flatten this object to parcelable.
     *
     * @param dest The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     * May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mChannelName);
        dest.writeString(mChannelReference);
        dest.writeString(mChannelIconReference);
    }

    /**
     * Describe special stuff from this object.
     *
     * @return flag.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Interface that must be implemented and provided as a public CREATOR field
     * that generates instances of your Parcelable class from a Parcel.
     */
    public static final Creator<ChannelInfo> CREATOR = new Creator<ChannelInfo>()
    {
        /**
         * Create a new instance of the Parcelable class, instantiating it from the given
         * Parcel whose data had previously been written by Parcelable. writeToParcel().
         *
         * @param in The Parcel to read the object's data from.
         * @return Returns a new instance of the Parcelable class.
         */
        @Override
        public ChannelInfo createFromParcel(Parcel in) {
            return new ChannelInfo(in);
        }

        /**
         * Create a new array of the Parcelable class.
         *
         * @param size Size of the array.
         * @return Returns an array of the Parcelable class, with every entry initialized to null.
         */
        @Override
        public ChannelInfo[] newArray(int size) {
            return new ChannelInfo[size];
        }
    };

}

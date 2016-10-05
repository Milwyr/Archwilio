package com.milton.archwilio.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument;
import com.google.android.gms.location.places.Place;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Milton on 02/03/2016.
 */
@DynamoDBDocument
public class AttractionRecord extends ItineraryItem {
    public static final String TABLE_NAME = "AttractionRecord";
    public static final String COLUMN_DURATION = "Duration";

    private int duration;

    public AttractionRecord() {
        super();
    }

    public AttractionRecord(Place place, Date startDateTime, Date endDateTime, String note) {
        super(place, startDateTime, endDateTime, note);
    }

    public AttractionRecord(long id, Place place, Date startDateTime, Date endDateTime, String note) {
        super(id, place, startDateTime, endDateTime, note);
    }

    @DynamoDBAttribute(attributeName = COLUMN_DURATION)
    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;

        // Set the end date time to be the start date time plus duration
        if (super.getStartDateTime() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(super.getStartDateTime());
            calendar.add(Calendar.HOUR, duration);
            super.setEndDateTime(calendar.getTime());
        }
    }

    //region Code to implement Parcelable
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(this.duration);
    }

    public static final Parcelable.Creator<AttractionRecord> CREATOR = new Parcelable.Creator<AttractionRecord>() {
        @Override
        public AttractionRecord createFromParcel(Parcel source) {
            try {
                return new AttractionRecord(source);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        @Override
        public AttractionRecord[] newArray(int size) {
            return new AttractionRecord[size];
        }
    };

    private AttractionRecord(Parcel in) throws ClassNotFoundException {
        super(in);
        this.duration = in.readInt();
    }
    //endregion
}
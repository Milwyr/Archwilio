package com.milton.archwilio.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument;
import com.google.android.gms.location.places.Place;
import com.milton.archwilio.common.Utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Milton on 02/03/2016.
 */
@DynamoDBDocument
public class HotelRecord extends ItineraryItem {
    //region Constants
    public static final String TABLE_NAME = "HotelRecord";
    public static final String COLUMN_NUMBER_OF_GUESTS = "NumberOfGuests";
    public static final String COLUMN_TOTAL_PRICE = "TotalPrice";
    public static final String COLUMN_TRIP_ID = "TripId"; // foreign key
    //endregion

    private int numberOfGuests;
    private double totalPrice;

    //region Constructors
    public HotelRecord() {
        super();
        this.numberOfGuests = 1;
        this.totalPrice = 0;
    }

    public HotelRecord(Place place, Date startDateTime, Date endDateTime, String note) {
        super(-1, place, startDateTime, endDateTime, note);
    }

    public HotelRecord(long id, Place place, Date startDateTime, Date endDateTime, String note) {
        super(id, place, startDateTime, endDateTime, note);
        this.numberOfGuests = 1;
        this.totalPrice = 0;
    }
    //endregion

    //region Getter methods
    /**
     * The start time of hotel record is always set to 23:59,
     * as hotel is normally the last item in the itinerary.
     *
     * @return A date object of the check in date of the hotel
     */
    @Override
    public Date getStartDateTime() {
        Date startDate = super.getStartDateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE), 23, 59);
        return calendar.getTime();
    }

    @Override
    @DynamoDBAttribute(attributeName = COLUMN_START_DATE)
    public String getStartDateTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                Utility.DATE_TIME_FORMAT_PATTERN, Locale.ENGLISH);
        return sdf.format(this.getStartDateTime());
    }

    @Override
    public Date getEndDateTime() {
        Date endDate = super.getEndDateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE), 1, 1);
        return calendar.getTime();

    }

    @Override
    @DynamoDBAttribute(attributeName = COLUMN_END_DATE)
    public String getEndDateTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                Utility.DATE_TIME_FORMAT_PATTERN, Locale.ENGLISH);
        return sdf.format(this.getEndDateTime());
    }

    @DynamoDBAttribute(attributeName = COLUMN_NUMBER_OF_GUESTS)
    public int getNumberOfGuests() {
        return this.numberOfGuests;
    }

    @DynamoDBAttribute(attributeName = COLUMN_TOTAL_PRICE)
    public double getTotalPrice() {
        return this.totalPrice;
    }
    //endregion

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    //region Code to implement Parcelable
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(this.numberOfGuests);
        out.writeDouble(this.totalPrice);
    }

    public static final Parcelable.Creator<HotelRecord> CREATOR = new Parcelable.Creator<HotelRecord>() {
        @Override
        public HotelRecord createFromParcel(Parcel source) {
            try {
                return new HotelRecord(source);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        @Override
        public HotelRecord[] newArray(int size) {
            return new HotelRecord[size];
        }
    };

    private HotelRecord(Parcel in) throws ClassNotFoundException {
        super(in);
        this.numberOfGuests = in.readInt();
        this.totalPrice = in.readDouble();
    }
    //endregion
}
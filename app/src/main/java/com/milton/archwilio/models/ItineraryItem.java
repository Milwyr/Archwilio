package com.milton.archwilio.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIgnore;
import com.google.android.gms.location.places.Place;
import com.milton.archwilio.common.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Milton on 19/02/2016.
 */
@DynamoDBDocument
public abstract class ItineraryItem implements Comparable<ItineraryItem>, Parcelable {
    //region Constants
    private final String ERROR_TAG = "ItineraryItem";

    public static final String COLUMN_ID = "IId";
    public static final String ATTRIBUTE_PLACE = "Place";
    public static final String COLUMN_PLACE_ID = "PlaceId";
    public static final String COLUMN_START_DATE = "StartDate";
    public static final String COLUMN_END_DATE = "EndDate";
    public static final String COLUMN_NOTE = "Note";
    //endregion

    //region Instance variables
    private long id;
    private Place place;
    private Date startDateTime;
    private Date endDateTime;
    private String note;

    private SimpleDateFormat dateTimeFormat;
    //endregion

    //region Constructors

    /**
     * Constructor that sets start and end time to be now
     */
    public ItineraryItem() {
        this(-1, null, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), "");
    }

    /**
     * Constructor
     *
     * @param place         Place object
     * @param startDateTime Start date object
     * @param endDateTime   End date object
     * @param note          Note
     */
    public ItineraryItem(Place place, Date startDateTime, Date endDateTime, String note) {
        this(-1, place, startDateTime, endDateTime, note);
    }

    /**
     * Constructor
     *
     * @param id            Hotel record id
     * @param place         Place object
     * @param startDateTime Start date object
     * @param endDateTime   End date object
     * @param note          Note
     */
    public ItineraryItem(long id, Place place, Date startDateTime, Date endDateTime, String note) {
        this.id = id;
        this.place = place;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.note = note;

        this.dateTimeFormat = new SimpleDateFormat(
                Utility.DATE_TIME_FORMAT_PATTERN, Locale.ENGLISH);
    }
    //endregion

    //region Getter methods
    @DynamoDBHashKey(attributeName = COLUMN_ID)
    public long getId() {
        return this.id;
    }

    @DynamoDBIgnore
    public Place getPlace() {
        return this.place;
    }

    @DynamoDBAttribute(attributeName = ATTRIBUTE_PLACE)
    public PlaceImpl getPlaceImpl() {
        if (this.place == null) {
            return null;
        }
        return new PlaceImpl(this.place);
    }

    @DynamoDBIgnore
    public Date getStartDateTime() {
        return this.startDateTime;
    }

    @DynamoDBAttribute(attributeName = COLUMN_START_DATE)
    public String getStartDateTimeString() {
        return this.dateTimeFormat.format(this.startDateTime);
    }

    @DynamoDBIgnore
    public Date getEndDateTime() {
        return this.endDateTime;
    }

    @DynamoDBAttribute(attributeName = COLUMN_END_DATE)
    public String getEndDateTimeString() {
        return this.dateTimeFormat.format(this.endDateTime);
    }

    @DynamoDBAttribute(attributeName = COLUMN_NOTE)
    public String getNote() {
        if (this.note == null) {
            return "";
        }
        return this.note;
    }
    //endregion

    //region Setter methods
    public void setId(long id) {
        this.id = id;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public void setPlaceImpl(PlaceImpl placeImpl) {
        this.place = placeImpl;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setStartDateTimeString(String startDateTimeString) {
        try {
            this.startDateTime = this.dateTimeFormat.parse(startDateTimeString);
        } catch (ParseException e) {
        }
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setEndDateTimeString(String endDateTimeString) {
        try {
            this.endDateTime = this.dateTimeFormat.parse(endDateTimeString);
        } catch (ParseException e) {
        }
    }

    public void setNote(String note) {
        this.note = note;
    }
    //endregion

    @DynamoDBIgnore
    public boolean isDataValid() {
        return this.place != null && this.startDateTime != null && this.endDateTime != null;
    }

    //region Methods for comparison
    @Override
    public int compareTo(@NonNull ItineraryItem another) {
        Long id1 = this.getId();
        Long id2 = another.getId();

        // Compare the itinerary items according to ids
        return id1.compareTo(id2);
    }

    @Override
    public boolean equals(@NonNull Object other) {
        if (other instanceof ItineraryItem) {
            Long id1 = this.id;
            Long id2 = ((ItineraryItem) other).getId();
            return id1.equals(id2);
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Long.valueOf(this.id).hashCode();
    }

    /**
     * This comparator compares itinerary items according to their start dates.
     * If the start dates are the same, then compare them according to the end dates.
     */
    public static Comparator<ItineraryItem> DateComparator = new Comparator<ItineraryItem>() {
        @Override
        public int compare(ItineraryItem t1, ItineraryItem t2) {
            if (t1.getStartDateTime() == null || t2.getStartDateTime() == null) {
                return 0;
            }

            int startDateResult = t1.getStartDateTime().compareTo(t2.getStartDateTime());

            if (startDateResult != 0) {
                return startDateResult;
            } else {
                return t1.getEndDateTime().compareTo(t2.getEndDateTime());
            }
        }
    };
    //endregion

    //region Code to implement Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(getId());
        out.writeParcelable(getPlaceImpl(), PARCELABLE_WRITE_RETURN_VALUE);
        out.writeSerializable(getStartDateTime());
        out.writeSerializable(getEndDateTime());
        out.writeString(getNote());
    }

    protected ItineraryItem(Parcel in) throws ClassNotFoundException {
        setId(in.readLong());
        PlaceImpl placeImpl = in.readParcelable(new ClassLoader() {
            @Override
            protected Class<?> findClass(String className) throws ClassNotFoundException {
                if (className.equals(PlaceImpl.class.getName()) ||
                        className.equals(PlaceImpl.class.getSimpleName())) {
                    return PlaceImpl.class;
                }
                return super.findClass(className);
            }
        });
        setPlaceImpl(placeImpl);
        setStartDateTime((Date) in.readSerializable());
        setEndDateTime((Date) in.readSerializable());
        setNote(in.readString());
    }
    //endregion
}
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Milton on 07/02/2016.
 */
@DynamoDBDocument
public class Trip implements Comparable<Trip>, Parcelable {
    //region Constants
    public static final String TABLE_NAME = "Trip";
    public static final String COLUMN_ID = "TId";
    public static final String COLUMN_TITLE = "Title";
    private static final String ATTRIBUTE_DESTINATIONS = "Destinations";
    public static final String COLUMN_START_DATE = "Start";
    public static final String COLUMN_END_DATE = "End";
    public static final String COLUMN_NOTE = "Note";
    public static final String COLUMN_IS_DELETED = "IsDeleted";
    //endregion

    //region Instance variables
    // Fields for SQLite database
    private long id;
    private String title;
    private List<Place> destinations;
    private Date startDate;
    private Date endDate;
    private String note;
    private boolean isDeleted;

    // Additional fields DynamoDB
    private List<AttractionRecord> attractionRecords;
    private List<HotelRecord> hotelRecords;
    private List<TransportRecord> transportRecords;

    private SimpleDateFormat dateTimeFormat;
    //endregion

    //region Constructors
    public Trip() {
        this(-1, null, new ArrayList<Place>(), null, null, null, false);
    }

    /**
     * Constructor with trip id set to -1
     *
     * @param title        Tile of the trip
     * @param destinations A list of destinations
     * @param note         Note of the trip
     * @param startDate    Start date of the trip
     * @param endDate      End date of the trip
     */
    public Trip(String title, List<Place> destinations,
                Date startDate, Date endDate, String note, boolean isDeleted) {
        this(-1, title, destinations, startDate, endDate, note, isDeleted);
    }

    /**
     * Constructor
     *
     * @param id           Id of the trip
     * @param title        Tile of the trip
     * @param destinations A list of destinations
     * @param startDate    Start date of the trip
     * @param endDate      End date of the trip
     * @param note         Note of the trip
     */
    public Trip(long id, String title, List<Place> destinations,
                Date startDate, Date endDate, String note, boolean isDeleted) {
        this.id = id;
        this.title = title;
        this.destinations = destinations;
        this.startDate = startDate;
        this.endDate = endDate;
        this.note = note;
        this.isDeleted = isDeleted;

        if (this.destinations == null) {
            this.destinations = new ArrayList<>();
        }

        this.attractionRecords = new ArrayList<>();
        this.hotelRecords = new ArrayList<>();
        this.transportRecords = new ArrayList<>();

        this.dateTimeFormat = new SimpleDateFormat(
                Utility.DATE_TIME_FORMAT_PATTERN, Locale.ENGLISH);
    }
    //endregion

    //region Getter methods
    @DynamoDBHashKey(attributeName = COLUMN_ID)
    public long getId() {
        return this.id;
    }

    @DynamoDBAttribute(attributeName = COLUMN_TITLE)
    public String getTitle() {
        if (this.title == null) {
            return "";
        }
        return this.title;
    }

    @DynamoDBIgnore
    public List<Place> getDestinations() {
        return this.destinations;
    }

    @DynamoDBAttribute(attributeName = ATTRIBUTE_DESTINATIONS)
    public List<PlaceImpl> getDestinationImpls() {
        List<PlaceImpl> destinationImpls = new ArrayList<>();
        for (Place destination : this.destinations) {
            destinationImpls.add(new PlaceImpl(destination));
        }
        return destinationImpls;
    }

    @DynamoDBAttribute(attributeName = COLUMN_NOTE)
    public String getNote() {
        if (this.note == null) {
            return "";
        }
        return this.note;
    }

    @DynamoDBIgnore
    public Date getStartDate() {
        return this.startDate;
    }

    @DynamoDBAttribute(attributeName = COLUMN_START_DATE)
    public String getStartDateString() {
        if (this.startDate == null) {
            return null;
        }
        return this.dateTimeFormat.format(this.startDate);
    }

    @DynamoDBIgnore
    public Date getEndDate() {
        return this.endDate;
    }

    @DynamoDBAttribute(attributeName = COLUMN_END_DATE)
    public String getEndDateString() {
        if (this.endDate == null) {
            return null;
        }
        return this.dateTimeFormat.format(this.endDate);
    }

    @DynamoDBAttribute(attributeName = COLUMN_IS_DELETED)
    public boolean getIsDeleted() {
        return this.isDeleted;
    }

    @DynamoDBAttribute(attributeName = AttractionRecord.TABLE_NAME)
    public List<AttractionRecord> getAttractionRecords() {
        return this.attractionRecords;
    }

    @DynamoDBAttribute(attributeName = HotelRecord.TABLE_NAME)
    public List<HotelRecord> getHotelRecords() {
        return this.hotelRecords;
    }

    @DynamoDBAttribute(attributeName = TransportRecord.TABLE_NAME)
    public List<TransportRecord> getTransportRecords() {
        return this.transportRecords;
    }
    //endregion

    //region Setter methods
    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDestinations(List<Place> destinations) {
        this.destinations = destinations;
    }

    public void setDestinationImpls(List<PlaceImpl> destinations) {
        for (Place destination : destinations) {
            this.destinations.add(destination);
        }
        // TODO: try to replace PlaceImpl by Place in the method signature
    }

    public void addDestination(Place destination) {
        this.destinations.add(destination);
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setStartDateString(String startDateString) {
        try {
            setStartDate(this.dateTimeFormat.parse(startDateString));
        } catch (ParseException e) {
//            Log.e(ERROR_TAG, e.getMessage());
        }
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setEndDateString(String endDateString) {
        try {
            setEndDate(this.dateTimeFormat.parse(endDateString));
        } catch (ParseException e) {
//            Log.e(ERROR_TAG, e.getMessage());
        }
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void setAttractionRecords(List<AttractionRecord> attractionRecords) {
        this.attractionRecords = attractionRecords;
    }

    public void setHotelRecords(List<HotelRecord> hotelRecords) {
        this.hotelRecords = hotelRecords;
    }

    public void setTransportRecords(List<TransportRecord> transportRecords) {
        this.transportRecords = transportRecords;
    }
    //endregion

    //region Methods for comparison
    @Override
    public int compareTo(@NonNull Trip another) {
        Long id1 = this.getId();
        Long id2 = another.getId();

        // Compare the trips according to ids
        return id1.compareTo(id2);
    }

    @Override
    public boolean equals(@NonNull Object other) {
        if (other instanceof Trip) {
            Long id1 = this.getId();
            Long id2 = ((Trip) other).getId();
            return id1.equals(id2);
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Long.valueOf(this.id).hashCode();
    }

    /**
     * This comparator compares trips according to their start dates in ascending order.
     * If the start dates are the same, then compare them according to the end dates.
     */
    public static class DateComparator implements Comparator<Trip> {
        @Override
        public int compare(Trip t1, Trip t2) {
            if (t1 == null || t2 == null) {
                return 0;
            }

            if (t1.getStartDate() == null || t2.getStartDate() == null) {
                return 0;
            }

            int startDateResult = t1.getStartDate().compareTo(t2.getStartDate());

            if (startDateResult != 0) {
                return startDateResult;
            } else {
                int endDateResult = t1.getEndDate().compareTo(t2.getEndDate());

                if (endDateResult != 0) {
                    return endDateResult;
                }
                return t1.compareTo(t2);
            }
        }
    }
    //endregion

    //region Code to implement Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.id);
        out.writeString(this.title);

        List<PlaceImpl> placeImpls = new ArrayList<>();
        for (Place p: this.destinations) {
            placeImpls.add(new PlaceImpl(p));
        }
        out.writeTypedList(placeImpls);

        out.writeSerializable(this.startDate);
        out.writeSerializable(this.endDate);
        out.writeString(this.note);
        out.writeInt(this.isDeleted ? 1 : 0);
    }

    public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel source) {
            return new Trip(source);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    private Trip(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();

        this.destinations = new ArrayList<>();
        List<PlaceImpl> placeImpls = new ArrayList<>();
        in.readTypedList(placeImpls, PlaceImpl.CREATOR);
        for (PlaceImpl p: placeImpls) {
            this.destinations.add(p);
        }

        this.startDate = (Date) in.readSerializable();
        this.endDate = (Date) in.readSerializable();
        this.note = in.readString();
        this.isDeleted = in.readInt() != 0;
    }
    //endregion
}
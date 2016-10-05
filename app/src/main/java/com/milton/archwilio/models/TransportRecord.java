package com.milton.archwilio.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIgnore;
import com.google.android.gms.location.places.Place;

import java.util.Date;

/**
 * Created by Milton on 02/03/2016.
 */
@DynamoDBDocument
public class TransportRecord extends ItineraryItem {
    //region Constants
    public static final String TABLE_NAME = "TransportRecord";
    public static final String COLUMN_TRANSPORT_MODE = "TransportMode";
    public static final String COLUMN_DISPLAY_MESSAGE = "DisplayMessage";
    public static final String ATTRIBUTE_ARRIVAL_PLACE = "ArrivalPlace";
    public static final String COLUMN_ARRIVAL_PLACE_ID = "ArrivalPlaceId";
    public static final String COLUMN_REFERENCE_NO = "ReferenceNo";
    public static final String COLUMN_PRICE = "Price";
    public static final String COLUMN_TRIP_ID = "TripId"; // foreign key
    //endregion

    //region Instance variables
    private int transportMode;
    private String displayMessage;
    private Place arrivalPlace;
    private String referenceNumber;
    private double price;
    //endregion

    //region Constructors
    public TransportRecord() {
        super();
    }

    public TransportRecord(long id, int transportMode, String displayMessage, Place place, Place arrivalPlace,
                           Date startDateTime, Date endDateTime, String note, String referenceNumber) {
        super(id, place, startDateTime, endDateTime, note);
        this.transportMode = transportMode;
        this.displayMessage = displayMessage;
        this.arrivalPlace = arrivalPlace;
        this.referenceNumber = referenceNumber;
    }
    //endregion

    //region Getter methods
    /**
     * This method gets the value for transportMode (refer to travel_mode_options in strings.xml).
     *
     * @return The value for transportMode
     */
    @DynamoDBAttribute(attributeName = COLUMN_TRANSPORT_MODE)
    public int getTransportMode() {
        return this.transportMode;
    }

    @DynamoDBAttribute(attributeName = COLUMN_DISPLAY_MESSAGE)
    public String getDisplayMessage() {
        if (this.displayMessage == null) {
            return "";
        }
        return this.displayMessage;
    }

    @DynamoDBIgnore
    public Place getArrivalPlace() {
        return this.arrivalPlace;
    }

    @DynamoDBAttribute(attributeName = ATTRIBUTE_ARRIVAL_PLACE)
    public PlaceImpl getArrivalPlaceImpl() {
        if (this.arrivalPlace == null) {
            return null;
        }
        return new PlaceImpl(this.arrivalPlace);
    }

    @DynamoDBAttribute(attributeName = "ReferenceNumber")
    public String getReferenceNumber() {
        return this.referenceNumber;
    }

    @DynamoDBAttribute(attributeName = COLUMN_PRICE)
    public double getPrice() {
        return this.price;
    }
    //endregion

    //region Setter methods
    /**
     * This method sets the value for transportMode (refer to travel_mode_options in strings.xml).
     *
     * @param transportMode Transport mode
     */
    public void setTransportMode(int transportMode) {
        this.transportMode = transportMode;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public void setArrivalPlace(Place arrivalPlace) {
        this.arrivalPlace = arrivalPlace;
    }

    public void setArrivalPlaceImpl(PlaceImpl arrivalPlace) {
        this.arrivalPlace = arrivalPlace;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    //endregion

    @DynamoDBIgnore
    @Override
    public boolean isDataValid() {
        return super.isDataValid() && this.arrivalPlace != null;
    }

    //region Code to implement Parcelable
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(this.transportMode);
        out.writeString(this.displayMessage);
        out.writeParcelable(getArrivalPlaceImpl(), PARCELABLE_WRITE_RETURN_VALUE);
        out.writeString(this.referenceNumber);
    }

    public static final Parcelable.Creator<TransportRecord> CREATOR = new Parcelable.Creator<TransportRecord>() {
        @Override
        public TransportRecord createFromParcel(Parcel source) {
            try {
                return new TransportRecord(source);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        @Override
        public TransportRecord[] newArray(int size) {
            return new TransportRecord[size];
        }
    };

    private TransportRecord(Parcel in) throws ClassNotFoundException {
        super(in);
        setTransportMode(in.readInt());
        setDisplayMessage(in.readString());
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
        setArrivalPlaceImpl(placeImpl);
        setReferenceNumber(in.readString());
    }
    //endregion
}
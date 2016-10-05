package com.milton.archwilio.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIgnore;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Milton on 04/04/2016.
 */
@DynamoDBDocument
public class PlaceImpl implements Place, Parcelable {
    //region Constants
    public static final String TABLE_NAME = "Place";
    public static final String COLUMN_ID = "PId";
    public static final String COLUMN_ADDRESS = "Address";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_LATITUDE = "Latitude";
    public static final String COLUMN_LONGITUDE = "Longitude";
    public static final String COLUMN_SW_LAT = "SwLat"; // south west latitude
    public static final String COLUMN_SW_LNG = "SwLng"; // south west longitude
    public static final String COLUMN_NE_LAT = "NeLat"; // north east latitude
    public static final String COLUMN_NE_LNG = "NeLng"; // north east longitude
    public static final String COLUMN_WEBSITE_URI = "WebsiteUri";
    public static final String COLUMN_PHONE_NUMBER = "PhoneNumber";
    public static final String COLUMN_RATING = "Rating";
    public static final String COLUMN_PRICE_LEVEL = "PriceLevel";
    public static final String COLUMN_ATTRIBUTIONS = "Attributions";
    //endregion

    //region Instance variables
    private String id;
    private CharSequence address;
    private CharSequence name;
    private LatLng latLng;
    private LatLngBounds viewport;
    private Uri websiteUri;
    private CharSequence phoneNumber;
    private float rating;
    private int priceLevel;
    private CharSequence attributions;
    //endregion

    //region Constructors
    public PlaceImpl() {
    }

    public PlaceImpl(Place place) {
        this.id = place.getId();
        this.address = place.getAddress();
        this.name = place.getName();
        this.latLng = place.getLatLng();
        this.viewport = place.getViewport();
        this.websiteUri = place.getWebsiteUri();
        this.phoneNumber = place.getPhoneNumber();
        this.rating = place.getRating();
        this.priceLevel = place.getPriceLevel();
        this.attributions = place.getAttributions();
    }

    public PlaceImpl(String id, CharSequence address, CharSequence name, LatLng latLng, LatLngBounds viewport,
                     Uri websiteUri, CharSequence phoneNumber, float rating, int priceLevel, CharSequence attributions) {
        this.id = id;
        this.address = address;
        this.name = name;
        this.latLng = latLng;
        this.viewport = viewport;
        this.websiteUri = websiteUri;
        this.phoneNumber = phoneNumber;
        this.rating = rating;
        this.priceLevel = priceLevel;
        this.attributions = attributions;
    }
    //endregion

    //region Getter methods
    @Override
    @DynamoDBHashKey(attributeName = COLUMN_ID)
    public String getId() {
        return this.id;
    }

    @Override
    @DynamoDBAttribute(attributeName = "PlaceType")
    public List<Integer> getPlaceTypes() {
        return null;
    }

    @Override
    @DynamoDBIgnore
    public CharSequence getAddress() {
        return this.address;
    }

    @DynamoDBAttribute(attributeName = COLUMN_ADDRESS)
    public String getAddressString() {
        if (this.address == null) {
            return null;
        }
        return this.address.toString();
    }

    @Override
    @DynamoDBIgnore
    public Locale getLocale() {
        return null;
    }

    @Override
    @DynamoDBIgnore
    public CharSequence getName() {
        return this.name;
    }

    @DynamoDBAttribute(attributeName = COLUMN_NAME)
    public String getNameString() {
        if (this.name == null) {
            return null;
        }
        return this.name.toString();
    }

    @Override
    @DynamoDBIgnore
    public LatLng getLatLng() {
        return this.latLng;
    }

    /**
     * The first element is latitude, and the second element is longitude.
     *
     * @return A list of Double objects with size 2
     */
    @DynamoDBAttribute(attributeName = "LatLng")
    public List<Double> getLatLngDouble() {
        if (this.latLng == null) {
            return null;
        }
        List<Double> latLngs = new ArrayList<>();
        latLngs.add(this.latLng.latitude);
        latLngs.add(this.latLng.longitude);
        return latLngs;
    }

    @Override
    @DynamoDBIgnore
    public LatLngBounds getViewport() {
        return this.viewport;
    }

    /**
     * [0]: Southwest latitude, [1]: Southwest longitude
     * [2]: Northeast Latitude, [3]: Northeast longitude
     *
     * @return A list of doubles with size four
     */
    @DynamoDBAttribute(attributeName = "Viewport")
    public List<Double> getViewportDouble() {
        if (this.viewport == null) {
            return null;
        }
        List<Double> viewports = new ArrayList<>();
        viewports.add(this.viewport.southwest.latitude);
        viewports.add(this.viewport.southwest.longitude);
        viewports.add(this.viewport.northeast.latitude);
        viewports.add(this.viewport.northeast.longitude);
        return viewports;
    }

    @Override
    @DynamoDBIgnore
    public Uri getWebsiteUri() {
        return this.websiteUri;
    }

    @DynamoDBAttribute(attributeName = COLUMN_WEBSITE_URI)
    public String getWebsiteUriString() {
        if (this.websiteUri == null) {
            return null;
        }
        return this.websiteUri.toString();
    }

    @Override
    @DynamoDBIgnore
    public CharSequence getPhoneNumber() {
        return this.phoneNumber;
    }

    @DynamoDBAttribute(attributeName = COLUMN_PHONE_NUMBER)
    public String getPhoneNumberString() {
        if (this.phoneNumber == null) {
            return null;
        }
        return this.phoneNumber.toString();
    }

    @Override
    @DynamoDBAttribute(attributeName = COLUMN_RATING)
    public float getRating() {
        return this.rating;
    }

    @Override
    @DynamoDBAttribute(attributeName = COLUMN_PRICE_LEVEL)
    public int getPriceLevel() {
        return this.priceLevel;
    }

    @Override
    @DynamoDBIgnore
    public CharSequence getAttributions() {
        return this.attributions;
    }

    @DynamoDBAttribute(attributeName = COLUMN_ATTRIBUTIONS)
    public String getAttributionsString() {
        if (this.attributions == null) {
            return null;
        }
        return this.attributions.toString();
    }
    //endregion

    @Override
    public Place freeze() {
        return new PlaceImpl();
    }

    @Override
    @DynamoDBIgnore
    public boolean isDataValid() {
        return (this.id != null && !this.id.isEmpty()) && this.address != null &&
                (this.name != null && this.latLng != null) &&
                (this.rating > 1.0 && this.rating <= 5.0) &&
                (this.priceLevel >= 0 && this.priceLevel <= 4);
    }

    //region Setter methods
    public void setId(String id) {
        this.id = id;
    }

    public void setAddress(CharSequence address) {
        this.address = address;
    }

    public void setAddressString(String address) {
        this.address = address;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public void setNameString(String name) {
        this.name = name;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void setLatLngDouble(List<Double> latLngDoubles) {
        this.latLng = new LatLng(latLngDoubles.get(0), latLngDoubles.get(1));
    }

    public void setViewport(LatLngBounds viewport) {
        this.viewport = viewport;
    }

    public void setViewportDouble(List<Double> viewportDoubles) {
        if (viewportDoubles.size() == 4) {
            LatLngBounds viewport = new LatLngBounds(
                    new LatLng(viewportDoubles.get(0), viewportDoubles.get(1)),
                    new LatLng(viewportDoubles.get(2), viewportDoubles.get(3)));
            this.viewport = viewport;
        }
    }

    public void setWebsiteUri(Uri websiteUri) {
        this.websiteUri = websiteUri;
    }

    public void setWebsiteUriString(String websiteUri) {
        if (websiteUri == null) {
            this.websiteUri = null;
        } else {
            this.websiteUri = Uri.parse(websiteUri);
        }
    }

    public void setPhoneNumber(CharSequence phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPhoneNumberString(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @DynamoDBAttribute(attributeName = COLUMN_RATING)
    public void setRating(float rating) {
        this.rating = rating;
    }

    @DynamoDBAttribute(attributeName = COLUMN_PRICE_LEVEL)
    public void setPriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }

    @DynamoDBIgnore
    public void setAttributions(CharSequence attributions) {
        this.attributions = attributions;
    }

    @DynamoDBAttribute(attributeName = COLUMN_ATTRIBUTIONS)
    public void setAttributionsString(String attributions) {
        this.attributions = attributions;
    }
    //endregion

    //region Methods for comparison
    @Override
    public boolean equals(Object other) {
        if (other instanceof Place) {
            Place anotherPlace = (Place) other;
            return this.getId().equals(anotherPlace.getId());
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
    //endregion

    //region Code to implement Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getId());
        out.writeString(getAddressString());
        out.writeString(getNameString());
        out.writeDoubleArray(new double[]{this.latLng.latitude, this.latLng.longitude});

        if (this.viewport != null) {
            out.writeDoubleArray(new double[]{
                    this.viewport.southwest.latitude, this.viewport.southwest.longitude,
                    this.viewport.northeast.latitude, this.viewport.northeast.longitude
            });
        } else {
            // A double array with minimum values indicates the view port is null
            out.writeDoubleArray(new double[]{
                    Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE});
        }

        out.writeString(getWebsiteUriString());
        out.writeString(getPhoneNumberString());
        out.writeFloat(getRating());
        out.writeInt(getPriceLevel());
        out.writeString(getAttributionsString());
    }

    public static final Parcelable.Creator<PlaceImpl> CREATOR = new Parcelable.Creator<PlaceImpl>() {
        @Override
        public PlaceImpl createFromParcel(Parcel source) {
            return new PlaceImpl(source);
        }

        @Override
        public PlaceImpl[] newArray(int size) {
            return new PlaceImpl[size];
        }
    };

    private PlaceImpl(Parcel in) {
        this.id = in.readString();
        this.address = in.readString();
        this.name = in.readString();

        double[] latLngDoubles = new double[2];
        in.readDoubleArray(latLngDoubles);
        this.latLng = new LatLng(latLngDoubles[0], latLngDoubles[1]);

        double[] viewportDoubles = new double[4];
        in.readDoubleArray(viewportDoubles);

        this.viewport = null;

        // Viewport doubles consist of meaningful coordinates data
        if (Math.abs(viewportDoubles[0] - Double.MIN_VALUE) > 0.01) {
            this.viewport = new LatLngBounds(
                    new LatLng(viewportDoubles[0], viewportDoubles[1]),
                    new LatLng(viewportDoubles[2], viewportDoubles[3])
            );
        }

        setWebsiteUriString(in.readString());
        this.phoneNumber = in.readString();
        this.rating = in.readFloat();
        this.priceLevel = in.readInt();
        this.attributions = in.readString();
    }
    //endregion
}
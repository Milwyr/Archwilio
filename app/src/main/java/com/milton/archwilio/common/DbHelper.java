package com.milton.archwilio.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.milton.archwilio.models.AttractionRecord;
import com.milton.archwilio.models.HotelRecord;
import com.milton.archwilio.models.ItineraryItem;
import com.milton.archwilio.models.PlaceImpl;
import com.milton.archwilio.models.TransportRecord;
import com.milton.archwilio.models.Trip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is a helper class for SQLite database.
 */
public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TravelPlanner.db";

    private final String TRIP_PLACE_LINKING_TABLE_NAME = "TripPlace";

    private static DbHelper mInstance;

    private Map<Long, Trip> mTripMap;
    private SimpleDateFormat mDateTimeFormat;

    // Constructor
    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mDateTimeFormat = new SimpleDateFormat(Utility.DATE_TIME_FORMAT_PATTERN, Locale.ENGLISH);

        initialiseTripMap();
    }

    /**
     * This method returns an instance of DbHelper object.
     *
     * @param context Context
     * @return A DbHelper instance
     */
    public static synchronized DbHelper getInstance(Context context) {
        if (mInstance == null) {
            // Application context ensures the instance will last for the lifetime of the app
            mInstance = new DbHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TRIP_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                Trip.TABLE_NAME + " (" +
                Trip.COLUMN_ID + " LONG PRIMARY KEY, " +
                Trip.COLUMN_TITLE + " VARCHAR(255), " +
                Trip.COLUMN_NOTE + " VARCHAR(255), " +
                Trip.COLUMN_START_DATE + " VARCHAR(255), " +
                Trip.COLUMN_END_DATE + " VARCHAR(255), " +
                Trip.COLUMN_IS_DELETED + " INTEGER);";
        db.execSQL(CREATE_TRIP_TABLE_SQL_QUERY);

        final String CREATE_ATTRACTION_RECORD_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                AttractionRecord.TABLE_NAME + " (" +
                AttractionRecord.COLUMN_ID + " LONG PRIMARY KEY, " +
                AttractionRecord.COLUMN_PLACE_ID + " VARCHAR(255) NOT NULL, " +
                AttractionRecord.COLUMN_START_DATE + " VARCHAR(255) NOT NULL, " +
                AttractionRecord.COLUMN_DURATION + " INTEGER, " +
                AttractionRecord.COLUMN_END_DATE + " VARCHAR(255), " +
                AttractionRecord.COLUMN_NOTE + " VARCHAR(255)," +
                Trip.COLUMN_ID + " LONG, " +
                "FOREIGN KEY(" + Trip.COLUMN_ID + ") REFERENCES " +
                Trip.TABLE_NAME + "(" + Trip.COLUMN_ID + ") ON DELETE CASCADE);";
        db.execSQL(CREATE_ATTRACTION_RECORD_TABLE_SQL_QUERY);

        final String CREATE_HOTEL_RECORD_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                HotelRecord.TABLE_NAME + " (" +
                HotelRecord.COLUMN_ID + " LONG PRIMARY KEY, " +
                HotelRecord.COLUMN_PLACE_ID + " VARCHAR(255) NOT NULL, " +
                HotelRecord.COLUMN_START_DATE + " VARCHAR(255) NOT NULL, " +
                HotelRecord.COLUMN_END_DATE + " VARCHAR(255) NOT NULL, " +
                HotelRecord.COLUMN_NOTE + " VARCHAR(255), " +
                HotelRecord.COLUMN_NUMBER_OF_GUESTS + " INTEGER, " +
                HotelRecord.COLUMN_TOTAL_PRICE + " DOUBLE, " +
                HotelRecord.COLUMN_TRIP_ID + " LONG, " +
                "FOREIGN KEY(" + HotelRecord.COLUMN_TRIP_ID + ") REFERENCES " +
                Trip.TABLE_NAME + "(" + Trip.COLUMN_ID + ") ON DELETE CASCADE);";
        db.execSQL(CREATE_HOTEL_RECORD_TABLE_SQL_QUERY);

        final String CREATE_TRANSPORT_RECORD_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                TransportRecord.TABLE_NAME + " (" +
                TransportRecord.COLUMN_ID + " LONG PRIMARY KEY, " +
                TransportRecord.COLUMN_TRANSPORT_MODE + " INTEGER, " +
                TransportRecord.COLUMN_DISPLAY_MESSAGE + " VARCHAR(255), " +
                TransportRecord.COLUMN_PLACE_ID + " VARCHAR(255) NOT NULL, " +
                TransportRecord.COLUMN_ARRIVAL_PLACE_ID + " VARCHAR(255)," +
                TransportRecord.COLUMN_START_DATE + " VARCHAR(255) NOT NULL, " +
                TransportRecord.COLUMN_END_DATE + " VARCHAR(255) NOT NULL, " +
                TransportRecord.COLUMN_NOTE + " VARCHAR(255)," +
                TransportRecord.COLUMN_REFERENCE_NO + " VARCHAR(255)," +
                TransportRecord.COLUMN_PRICE + " DOUBLE, " +
                TransportRecord.COLUMN_TRIP_ID + " LONG, " +
                "FOREIGN KEY(" + TransportRecord.COLUMN_TRIP_ID + ") REFERENCES " +
                Trip.TABLE_NAME + "(" + Trip.COLUMN_ID + ") ON DELETE CASCADE);";
        db.execSQL(CREATE_TRANSPORT_RECORD_TABLE_SQL_QUERY);

        final String CREATE_PLACE_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                PlaceImpl.TABLE_NAME + " (" +
                PlaceImpl.COLUMN_ID + " VARCHAR(255) PRIMARY KEY, " +
                PlaceImpl.COLUMN_ADDRESS + " VARCHAR(255) NOT NULL, " +
                PlaceImpl.COLUMN_NAME + " VARCHAR(255), " +
                PlaceImpl.COLUMN_LATITUDE + " DOUBLE, " +
                PlaceImpl.COLUMN_LONGITUDE + " DOUBLE, " +
                PlaceImpl.COLUMN_SW_LAT + " DOUBLE, " +
                PlaceImpl.COLUMN_SW_LNG + " DOUBLE, " +
                PlaceImpl.COLUMN_NE_LAT + " DOUBLE, " +
                PlaceImpl.COLUMN_NE_LNG + " DOUBLE, " +
                PlaceImpl.COLUMN_WEBSITE_URI + " VARCHAR(255), " +
                PlaceImpl.COLUMN_PHONE_NUMBER + " VARCHAR(255), " +
                PlaceImpl.COLUMN_RATING + " FLOAT, " +
                PlaceImpl.COLUMN_PRICE_LEVEL + " INT, " +
                PlaceImpl.COLUMN_ATTRIBUTIONS + " VARCHAR(255));";
        db.execSQL(CREATE_PLACE_TABLE_SQL_QUERY);

        // This linking table is to store all the places in a trip
        final String CREATE_TRIP_PLACE_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                TRIP_PLACE_LINKING_TABLE_NAME + " (" +
                Trip.COLUMN_ID + " LONG, " +
                PlaceImpl.COLUMN_ID + " VARCHAR(255) NOT NULL," +
                "PRIMARY KEY(" + Trip.COLUMN_ID + ", " + PlaceImpl.COLUMN_ID + "));";
        db.execSQL(CREATE_TRIP_PLACE_TABLE_SQL_QUERY);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        // Enable foreign key constraints, which is necessary to make on delete cascade working
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    /**
     * This database is only a cache for online data, so its upgrade policy
     * is to simply to discard the data and refresh the database.
     *
     * @param db Database
     * @param oldVersion Old version
     * @param newVersion New version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String DROP_TABLE_SQL_QUERY = "DROP TABLE IF EXISTS " + Trip.TABLE_NAME;
        db.execSQL(DROP_TABLE_SQL_QUERY);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    //region Insert record

    /**
     * This method inserts a trip into the database.
     *
     * @param trip The trip to insert
     * @return True if the trip is inserted
     */
    public boolean insertTrip(Trip trip) {
        long id = getMaximum(Trip.TABLE_NAME, Trip.COLUMN_ID) + 1;
        trip.setId(id);

        ContentValues values = new ContentValues();
        values.put(Trip.COLUMN_ID, id);
        values.put(Trip.COLUMN_TITLE, trip.getTitle());
        values.put(Trip.COLUMN_NOTE, trip.getNote());
        values.put(Trip.COLUMN_START_DATE, mDateTimeFormat.format(trip.getStartDate()));
        values.put(Trip.COLUMN_END_DATE, mDateTimeFormat.format(trip.getEndDate()));

        if (trip.getIsDeleted()) {
            values.put(Trip.COLUMN_IS_DELETED, 1);
        } else {
            values.put(Trip.COLUMN_IS_DELETED, 0);
        }

        boolean isSuccess = getWritableDatabase().insertWithOnConflict(
                Trip.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ROLLBACK) > 0;
        isSuccess = isSuccess & insertPlaces(id, trip.getDestinations());

        if (isSuccess) {
            mTripMap.put(trip.getId(), trip);
        }

        return isSuccess;
    }

    /**
     * This method inserts a list of trips into the database.
     *
     * @param trips A list of trips
     * @throws SQLiteException
     */
    public void insertTrips(List<Trip> trips) throws SQLiteException {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransactionNonExclusive();

        String sql = "INSERT OR REPLACE INTO " + Trip.TABLE_NAME + " (" +
                Trip.COLUMN_ID + ", " +
                Trip.COLUMN_TITLE + ", " +
                Trip.COLUMN_NOTE + ", " +
                Trip.COLUMN_START_DATE + ", " +
                Trip.COLUMN_END_DATE + ", " +
                Trip.COLUMN_IS_DELETED + ") VALUES(?, ?, ?, ?, ?, ?);";

        try {
            SQLiteStatement statement = database.compileStatement(sql);

            for (Trip trip : trips) {
                statement.bindLong(1, trip.getId());
                statement.bindString(2, trip.getTitle());
                statement.bindString(3, trip.getNote());
                statement.bindString(4, trip.getStartDateString());
                statement.bindString(5, trip.getEndDateString());

                int isDeletedCode = trip.getIsDeleted() ? 1 : 0;
                statement.bindLong(6, isDeletedCode);

                statement.execute();
                statement.clearBindings();

                insertPlaces(trip.getId(), trip.getDestinations());

                mTripMap.put(trip.getId(), trip);
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * This method inserts an attraction record into the database.
     *
     * @param tripId The trip id this attraction record belongs to
     * @param record The attraction record
     * @return True if the attraction record is inserted
     */
    public boolean insertAttractionRecord(long tripId, AttractionRecord record) {
        long id = getMaximum(AttractionRecord.TABLE_NAME, HotelRecord.COLUMN_ID) + 1;
        record.setId(id);

        ContentValues values = new ContentValues();
        values.put(AttractionRecord.COLUMN_ID, id);
        values.put(AttractionRecord.COLUMN_PLACE_ID, record.getPlace().getId());
        values.put(AttractionRecord.COLUMN_START_DATE, mDateTimeFormat.format(record.getStartDateTime()));
        values.put(AttractionRecord.COLUMN_DURATION, record.getDuration());
        values.put(AttractionRecord.COLUMN_END_DATE, mDateTimeFormat.format(record.getEndDateTime()));
        values.put(AttractionRecord.COLUMN_NOTE, record.getNote());
        values.put(Trip.COLUMN_ID, tripId);

        boolean isSuccess = getWritableDatabase().insertWithOnConflict(
                AttractionRecord.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ROLLBACK) > 0;
        isSuccess = isSuccess & insertPlace(record.getPlace());

        return isSuccess;
    }

    /**
     * This method inserts a list of attraction records into the database.
     *
     * @param tripId  The trip id the attraction records belong to
     * @param records The attraction records
     * @throws SQLiteException
     */
    public void insertAttractionRecords(long tripId, List<AttractionRecord> records) throws SQLiteException {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransactionNonExclusive();

        String sql = "INSERT OR REPLACE INTO " + AttractionRecord.TABLE_NAME + " (" +
                AttractionRecord.COLUMN_ID + ", " +
                AttractionRecord.COLUMN_PLACE_ID + ", " +
                AttractionRecord.COLUMN_START_DATE + ", " +
                AttractionRecord.COLUMN_DURATION + ", " +
                AttractionRecord.COLUMN_END_DATE + ", " +
                AttractionRecord.COLUMN_NOTE + ", " +
                Trip.COLUMN_ID + ") VALUES(?, ?, ?, ?, ?, ?, ?);";

        try {
            SQLiteStatement statement = database.compileStatement(sql);

            for (AttractionRecord record : records) {
                statement.bindLong(1, record.getId());
                statement.bindString(2, record.getPlace().getId());
                statement.bindString(3, record.getStartDateTimeString());
                statement.bindLong(4, record.getDuration());
                statement.bindString(5, record.getEndDateTimeString());
                statement.bindString(6, record.getNote());
                statement.bindLong(7, tripId);
                statement.execute();
                statement.clearBindings();

                insertPlace(record.getPlace());
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * This method inserts a hotel record into the database.
     *
     * @param tripId The trip id this hotel record belongs to
     * @param record The hotel record
     * @return True if the hotel record is inserted
     */
    public boolean insertHotelRecord(long tripId, HotelRecord record) {
        long id = getMaximum(HotelRecord.TABLE_NAME, HotelRecord.COLUMN_ID) + 1;

        // The id starts from 10000 to prevent duplicate ids
        // with other attraction or transport records
        if (id == 1) {
            id += 10000;
        }

        record.setId(id);

        ContentValues values = new ContentValues();
        values.put(HotelRecord.COLUMN_ID, id);
        values.put(HotelRecord.COLUMN_PLACE_ID, record.getPlace().getId());
        values.put(HotelRecord.COLUMN_START_DATE, mDateTimeFormat.format(record.getStartDateTime()));
        values.put(HotelRecord.COLUMN_END_DATE, mDateTimeFormat.format(record.getEndDateTime()));
        values.put(HotelRecord.COLUMN_NOTE, record.getNote());
        values.put(HotelRecord.COLUMN_NUMBER_OF_GUESTS, record.getNumberOfGuests());
        values.put(HotelRecord.COLUMN_TOTAL_PRICE, record.getTotalPrice());
        values.put(HotelRecord.COLUMN_TRIP_ID, tripId);

        boolean isSuccess = getWritableDatabase().insertWithOnConflict(
                HotelRecord.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ROLLBACK) > 0;
        isSuccess = isSuccess & insertPlace(record.getPlace());

        return isSuccess;
    }

    /**
     * This method inserts a list of hotel records into the database.
     *
     * @param tripId  The trip id the hotel records belong to
     * @param records The hotel records
     * @throws SQLiteException
     */
    public void insertHotelRecords(long tripId, List<HotelRecord> records) throws SQLiteException {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransactionNonExclusive();

        String sql = "INSERT OR REPLACE INTO " + HotelRecord.TABLE_NAME + " (" +
                HotelRecord.COLUMN_ID + ", " +
                HotelRecord.COLUMN_PLACE_ID + ", " +
                HotelRecord.COLUMN_START_DATE + ", " +
                HotelRecord.COLUMN_END_DATE + ", " +
                HotelRecord.COLUMN_NOTE + ", " +
                HotelRecord.COLUMN_NUMBER_OF_GUESTS + ", " +
                HotelRecord.COLUMN_TOTAL_PRICE + ", " +
                HotelRecord.COLUMN_TRIP_ID + ") VALUES(?, ?, ?, ?, ?, ?, ?, ?);";

        try {
            SQLiteStatement statement = database.compileStatement(sql);

            for (HotelRecord record : records) {
                statement.bindLong(1, record.getId());
                statement.bindString(2, record.getPlace().getId());
                statement.bindString(3, record.getStartDateTimeString());
                statement.bindString(4, record.getEndDateTimeString());
                statement.bindString(5, record.getNote());
                statement.bindLong(6, record.getNumberOfGuests());
                statement.bindDouble(7, record.getTotalPrice());
                statement.bindLong(8, tripId);
                statement.execute();
                statement.clearBindings();

                insertPlace(record.getPlace());
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * This method inserts a transport record into the database.
     *
     * @param tripId The trip id this transport record belongs to
     * @param record The transport record
     * @return True if the transport record is inserted
     */
    public boolean insertTransportRecord(long tripId, TransportRecord record) {
        long id = getMaximum(TransportRecord.TABLE_NAME, TransportRecord.COLUMN_ID) + 1;

        // The id starts from 100000 to prevent duplicate ids
        // with other attraction or hotel records
        if (id == 1) {
            id += 100000;
        }

        record.setId(id);

        ContentValues values = new ContentValues();
        values.put(TransportRecord.COLUMN_ID, id);
        values.put(TransportRecord.COLUMN_TRANSPORT_MODE, record.getTransportMode());
        values.put(TransportRecord.COLUMN_DISPLAY_MESSAGE, record.getDisplayMessage());
        values.put(TransportRecord.COLUMN_PLACE_ID, record.getPlace().getId());
        values.put(TransportRecord.COLUMN_ARRIVAL_PLACE_ID, record.getArrivalPlace().getId());
        values.put(TransportRecord.COLUMN_START_DATE, record.getStartDateTimeString());
        values.put(TransportRecord.COLUMN_END_DATE, record.getEndDateTimeString());
        values.put(TransportRecord.COLUMN_NOTE, record.getNote());
        values.put(TransportRecord.COLUMN_REFERENCE_NO, record.getReferenceNumber());
        values.put(TransportRecord.COLUMN_PRICE, record.getPrice());
        values.put(TransportRecord.COLUMN_TRIP_ID, tripId);

        boolean isSuccess = getWritableDatabase().insertWithOnConflict(
                TransportRecord.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ROLLBACK) > 0;
        isSuccess = isSuccess & insertPlace(record.getPlace()) & insertPlace(record.getArrivalPlace());

        return isSuccess;
    }

    /**
     * This method inserts a list of transport records into the database.
     *
     * @param tripId  The trip id the transport records belong to
     * @param records The transport records
     * @throws SQLiteException
     */
    public void insertTransportRecords(long tripId, List<TransportRecord> records) throws SQLiteException {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransactionNonExclusive();

        String sql = "INSERT OR REPLACE INTO " + TransportRecord.TABLE_NAME + " (" +
                TransportRecord.COLUMN_ID + ", " +
                TransportRecord.COLUMN_TRANSPORT_MODE + ", " +
                TransportRecord.COLUMN_DISPLAY_MESSAGE + ", " +
                TransportRecord.COLUMN_PLACE_ID + ", " +
                TransportRecord.COLUMN_ARRIVAL_PLACE_ID + ", " +
                TransportRecord.COLUMN_START_DATE + ", " +
                TransportRecord.COLUMN_END_DATE + ", " +
                TransportRecord.COLUMN_NOTE + ", " +
                TransportRecord.COLUMN_REFERENCE_NO + ", " +
                TransportRecord.COLUMN_PRICE + ", " +
                TransportRecord.COLUMN_TRIP_ID +
                ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try {
            SQLiteStatement statement = database.compileStatement(sql);

            for (TransportRecord record : records) {
                statement.bindLong(1, record.getId());
                statement.bindLong(2, record.getTransportMode());
                statement.bindString(3, record.getDisplayMessage());
                statement.bindString(4, record.getPlace().getId());
                statement.bindString(5, record.getArrivalPlace().getId());
                statement.bindString(6, record.getStartDateTimeString());
                statement.bindString(7, record.getEndDateTimeString());
                statement.bindString(8, record.getNote());
                statement.bindString(9, record.getReferenceNumber());
                statement.bindDouble(10, record.getPrice());
                statement.bindLong(11, tripId);
                statement.execute();
                statement.clearBindings();

                insertPlace(record.getPlace());
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * This method inserts the given place into the database and will override
     * the entire place in the database if there is any conflict.
     *
     * @param place The place to insert
     * @return True if the insertion is successful
     */
    private boolean insertPlace(Place place) {
        if (place == null) {
            return false;
        } else {
            PlaceImpl placeImpl = new PlaceImpl(place);

            ContentValues values = new ContentValues();
            values.put(PlaceImpl.COLUMN_ID, place.getId());
            values.put(PlaceImpl.COLUMN_ADDRESS, placeImpl.getAddressString());
            values.put(PlaceImpl.COLUMN_NAME, placeImpl.getNameString());
            values.put(PlaceImpl.COLUMN_LATITUDE, placeImpl.getLatLng().latitude);
            values.put(PlaceImpl.COLUMN_LONGITUDE, placeImpl.getLatLng().longitude);

            if (place.getViewport() != null) {
                values.put(PlaceImpl.COLUMN_SW_LAT, place.getViewport().southwest.latitude);
                values.put(PlaceImpl.COLUMN_SW_LNG, place.getViewport().southwest.longitude);
                values.put(PlaceImpl.COLUMN_NE_LAT, place.getViewport().northeast.latitude);
                values.put(PlaceImpl.COLUMN_NE_LNG, place.getViewport().northeast.longitude);
            } else {
                values.put(PlaceImpl.COLUMN_SW_LAT, Double.MIN_VALUE);
                values.put(PlaceImpl.COLUMN_SW_LNG, Double.MIN_VALUE);
                values.put(PlaceImpl.COLUMN_NE_LAT, Double.MIN_VALUE);
                values.put(PlaceImpl.COLUMN_NE_LNG, Double.MIN_VALUE);
            }

            values.put(PlaceImpl.COLUMN_WEBSITE_URI, placeImpl.getWebsiteUriString());
            values.put(PlaceImpl.COLUMN_PHONE_NUMBER, placeImpl.getPhoneNumberString());
            values.put(PlaceImpl.COLUMN_RATING, placeImpl.getRating());
            values.put(PlaceImpl.COLUMN_PRICE_LEVEL, placeImpl.getPriceLevel());
            values.put(PlaceImpl.COLUMN_ATTRIBUTIONS, placeImpl.getAttributionsString());

            return getWritableDatabase().insertWithOnConflict(
                    PlaceImpl.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE) > 0;
        }
    }

    /**
     * This method inserts a list of places into the database.
     *
     * @param places A list of places
     * @throws SQLiteException
     */
    private void insertPlaces(List<Place> places) throws SQLiteException {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransactionNonExclusive();

        String sql = "INSERT OR REPLACE INTO " + PlaceImpl.TABLE_NAME + " (" +
                PlaceImpl.COLUMN_ID + ", " +
                PlaceImpl.COLUMN_ADDRESS + ", " +
                PlaceImpl.COLUMN_NAME + ", " +
                PlaceImpl.COLUMN_LATITUDE + ", " +
                PlaceImpl.COLUMN_LONGITUDE + ", " +
                PlaceImpl.COLUMN_SW_LAT + ", " +
                PlaceImpl.COLUMN_SW_LNG + ", " +
                PlaceImpl.COLUMN_NE_LAT + ", " +
                PlaceImpl.COLUMN_NE_LNG + ", " +
                PlaceImpl.COLUMN_WEBSITE_URI + ", " +
                PlaceImpl.COLUMN_PHONE_NUMBER + ", " +
                PlaceImpl.COLUMN_RATING + ", " +
                PlaceImpl.COLUMN_PRICE_LEVEL + ", " +
                PlaceImpl.COLUMN_ATTRIBUTIONS +
                ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try {
            SQLiteStatement statement = database.compileStatement(sql);

            for (Place place : places) {
                PlaceImpl placeImpl = new PlaceImpl(place);

                statement.bindString(1, placeImpl.getId());
                statement.bindString(2, placeImpl.getAddressString());
                statement.bindString(3, placeImpl.getNameString());
                statement.bindDouble(4, placeImpl.getLatLng().latitude);
                statement.bindDouble(5, placeImpl.getLatLng().longitude);

                if (placeImpl.getViewport() != null) {
                    statement.bindDouble(6, place.getViewport().southwest.latitude);
                    statement.bindDouble(7, place.getViewport().southwest.longitude);
                    statement.bindDouble(8, place.getViewport().northeast.latitude);
                    statement.bindDouble(9, place.getViewport().northeast.longitude);
                } else {
                    statement.bindDouble(6, Double.MIN_VALUE);
                    statement.bindDouble(7, Double.MIN_VALUE);
                    statement.bindDouble(8, Double.MIN_VALUE);
                    statement.bindDouble(9, Double.MIN_VALUE);
                }

                statement.bindString(10, placeImpl.getWebsiteUriString());
                statement.bindString(11, placeImpl.getPhoneNumberString());
                statement.bindDouble(12, placeImpl.getRating());
                statement.bindLong(13, placeImpl.getPriceLevel());
                statement.bindString(14, placeImpl.getAttributionsString());

                statement.execute();
                statement.clearBindings();
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    private boolean insertPlaces(long tripId, List<Place> places) {
        boolean isSuccess = true;
        for (Place place : places) {
            isSuccess = isSuccess & insertPlace(place);

            insertTripPlaceRecord(tripId, place.getId());
        }

        return isSuccess;
    }

    private boolean insertTripPlaceRecord(long tripId, String placeId) {
        ContentValues values = new ContentValues();
        values.put(Trip.COLUMN_ID, tripId);
        values.put(PlaceImpl.COLUMN_ID, placeId);
        return getWritableDatabase().insertWithOnConflict(
                TRIP_PLACE_LINKING_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE) > 0;
    }
    //endregion

    //region Update record
    //region Update trip

    /**
     * This method updates the trip deleted status.
     *
     * @param tripId    Trip id of the trip to update
     * @param isDeleted New deleted status after the update
     * @return True if the trip is updated
     */
    public boolean updateTripIsDeletedStatus(long tripId, boolean isDeleted) {
        ContentValues values = new ContentValues();
        values.put(Trip.COLUMN_IS_DELETED, isDeleted ? 1 : 0);

        boolean isSuccess = updateTrip(tripId, values);
        if (isSuccess) {
            Trip updatedTrip = mTripMap.get(tripId);
            updatedTrip.setIsDeleted(isDeleted);
            mTripMap.put(tripId, updatedTrip);
        }

        return isSuccess;
    }

    private boolean updateTrip(long tripId, ContentValues values) {
        String whereClause = Trip.COLUMN_ID + " = " + tripId;
        return getWritableDatabase().update(Trip.TABLE_NAME, values, whereClause, null) > 0;
    }

    /**
     * This method finds the trip id in the updatedTrip object, and updates the trip object
     * stored in the database.
     *
     * @param updatedTrip The updated trip object with a trip id
     * @return True if the update is successful
     */
    public boolean updateTrip(Trip updatedTrip) {
        ContentValues values = new ContentValues();
        values.put(Trip.COLUMN_TITLE, updatedTrip.getTitle());
        values.put(Trip.COLUMN_START_DATE, updatedTrip.getStartDateString());
        values.put(Trip.COLUMN_END_DATE, updatedTrip.getEndDateString());
        values.put(Trip.COLUMN_NOTE, updatedTrip.getNote());

        String whereClause = Trip.COLUMN_ID + " = " + updatedTrip.getId();

        boolean isSuccess = getWritableDatabase().updateWithOnConflict(Trip.TABLE_NAME,
                values, whereClause, null, SQLiteDatabase.CONFLICT_ROLLBACK) > 0;

        if (isSuccess) {
            updatePlaces(updatedTrip.getId(), updatedTrip.getDestinations());
            mTripMap.put(updatedTrip.getId(), updatedTrip);
        }
        return isSuccess;
    }
    //endregion

    public boolean updateAttractionRecord(long tripId, AttractionRecord updatedAttractionRecord) {
        ContentValues values = new ContentValues();
        values.put(AttractionRecord.COLUMN_PLACE_ID, updatedAttractionRecord.getPlace().getId());
        values.put(AttractionRecord.COLUMN_START_DATE, updatedAttractionRecord.getStartDateTimeString());
        values.put(AttractionRecord.COLUMN_DURATION, updatedAttractionRecord.getDuration());
        values.put(AttractionRecord.COLUMN_END_DATE, updatedAttractionRecord.getEndDateTimeString());
        values.put(AttractionRecord.COLUMN_NOTE, updatedAttractionRecord.getNote());
        values.put(Trip.COLUMN_ID, tripId);

        String whereClause = AttractionRecord.COLUMN_ID + " = " + updatedAttractionRecord.getId() +
                " AND " + AttractionRecord.TABLE_NAME + "." + Trip.COLUMN_ID + " = " + tripId;

        boolean isSuccess = getWritableDatabase().updateWithOnConflict(AttractionRecord.TABLE_NAME,
                values, whereClause, null, SQLiteDatabase.CONFLICT_ROLLBACK) > 0;

        if (isSuccess) {
            // Insert the place if it has not been added to database
            insertPlace(updatedAttractionRecord.getPlace());
        }

        return isSuccess;
    }

    public boolean updateHotelRecord(long tripId, HotelRecord updatedHotelRecord) {
        ContentValues values = new ContentValues();
        values.put(HotelRecord.COLUMN_PLACE_ID, updatedHotelRecord.getPlace().getId());
        values.put(HotelRecord.COLUMN_START_DATE, updatedHotelRecord.getStartDateTimeString());
        values.put(HotelRecord.COLUMN_END_DATE, updatedHotelRecord.getEndDateTimeString());
        values.put(HotelRecord.COLUMN_NOTE, updatedHotelRecord.getNote());
        values.put(HotelRecord.COLUMN_NUMBER_OF_GUESTS, updatedHotelRecord.getNumberOfGuests());
        values.put(HotelRecord.COLUMN_TOTAL_PRICE, updatedHotelRecord.getTotalPrice());

        String whereClause = HotelRecord.COLUMN_ID + " = " + updatedHotelRecord.getId() +
                " AND " + HotelRecord.COLUMN_TRIP_ID + " = " + tripId;

        boolean isSuccess = getWritableDatabase().updateWithOnConflict(HotelRecord.TABLE_NAME,
                values, whereClause, null, SQLiteDatabase.CONFLICT_ROLLBACK) > 0;

        if (isSuccess) {
            // Insert the place if it has not been added to database
            insertPlace(updatedHotelRecord.getPlace());
        }

        return isSuccess;
    }

    public boolean updateTransportRecord(long tripId, TransportRecord updatedTransportRecord) {
        ContentValues values = new ContentValues();
        values.put(TransportRecord.COLUMN_TRANSPORT_MODE, updatedTransportRecord.getTransportMode());
        values.put(TransportRecord.COLUMN_DISPLAY_MESSAGE, updatedTransportRecord.getDisplayMessage());
        values.put(TransportRecord.COLUMN_PLACE_ID, updatedTransportRecord.getPlace().getId());
        values.put(TransportRecord.COLUMN_ARRIVAL_PLACE_ID,
                updatedTransportRecord.getArrivalPlace().getId());
        values.put(TransportRecord.COLUMN_START_DATE, updatedTransportRecord.getStartDateTimeString());
        values.put(TransportRecord.COLUMN_END_DATE, updatedTransportRecord.getEndDateTimeString());
        values.put(TransportRecord.COLUMN_REFERENCE_NO, updatedTransportRecord.getReferenceNumber());
        values.put(TransportRecord.COLUMN_PRICE, updatedTransportRecord.getPrice());
        values.put(TransportRecord.COLUMN_NOTE, updatedTransportRecord.getNote());

        String whereClause = TransportRecord.COLUMN_ID + " = " + updatedTransportRecord.getId() +
                " AND " + HotelRecord.COLUMN_TRIP_ID + " = " + tripId;

        boolean isSuccess = getWritableDatabase().updateWithOnConflict(TransportRecord.TABLE_NAME,
                values, whereClause, null, SQLiteDatabase.CONFLICT_ROLLBACK) > 0;

        if (isSuccess) {
            // Insert the place if it has not been added to database
            insertPlace(updatedTransportRecord.getPlace());
            insertPlace(updatedTransportRecord.getArrivalPlace());
        }

        return isSuccess;
    }

    private void updatePlaces(long tripId, List<Place> updatedPlaces) {
        List<Place> insertedPlaces = new ArrayList<>(updatedPlaces);
        List<Place> dbPlaces = getPlaces(tripId);

        // The list contains a list of new places that are not in the database
        insertedPlaces.removeAll(dbPlaces);
        insertPlaces(tripId, insertedPlaces);

        // The list contains a list of places being removed from the database
        dbPlaces.removeAll(updatedPlaces);
        deleteTripPlaces(tripId, dbPlaces);
    }
    //endregion

    //region Delete record

    /**
     * This method delete the trip record with the given id.
     * The trips are sorted by its primary key in ascending order by default.
     *
     * @param id The id of the trip to delete
     * @return True if the trip with the given id is deleted
     */
    public boolean deleteTrip(long id) {
        String whereClause = Trip.COLUMN_ID + " = " + id;

        boolean isSuccess = getWritableDatabase().delete(Trip.TABLE_NAME, whereClause, null) > 0;

        if (isSuccess) {
            mTripMap.remove(id);
        }

        return isSuccess;
    }

    public boolean deleteAttractionRecord(long id) {
        String whereClause = AttractionRecord.COLUMN_ID + " = " + id;

        return getWritableDatabase().delete(AttractionRecord.TABLE_NAME, whereClause, null) > 0;
    }

    public boolean deleteHotelRecord(long id) {
        String whereClause = HotelRecord.COLUMN_ID + " = " + id;

        return getWritableDatabase().delete(HotelRecord.TABLE_NAME, whereClause, null) > 0;
    }

    public boolean deleteTransportRecord(long id) {
        String whereClause = TransportRecord.COLUMN_ID + " = " + id;

        return getWritableDatabase().delete(TransportRecord.TABLE_NAME, whereClause, null) > 0;
    }

    // This method deletes the places in the linking table TripPlace that matches the given trip id.
    private void deleteTripPlaces(long tripId, List<Place> places) {
        for (Place place : places) {
            String whereClause = TRIP_PLACE_LINKING_TABLE_NAME + "." + Trip.COLUMN_ID + " = ?" +
                    " AND " + TRIP_PLACE_LINKING_TABLE_NAME + "." + PlaceImpl.COLUMN_ID + " = ?";
            getWritableDatabase().delete(TRIP_PLACE_LINKING_TABLE_NAME,
                    whereClause, new String[]{Long.toString(tripId), place.getId()});
        }
    }
    //endregion

    //region Getter methods for trips

    /**
     * This method returns the trip with the given id.
     *
     * @param tripId Trip id of the target trip
     * @return The trip that matches the given id
     */
    public Trip getTrip(long tripId) {
        if (mTripMap.containsKey(tripId)) {
            return mTripMap.get(tripId);
        }
        return null;
    }

    /**
     * This method returns all the trips that are stored in the database.
     *
     * @return All the trips that are stored in the database
     */
    public List<Trip> getTrips() {
        return new ArrayList<>(mTripMap.values());
    }

    /**
     * This method returns all the trips that are stored in the database with the given status.
     *
     * @param isDeleted True if the trip is deleted
     * @return All the trips that are stored in the database
     */
    public List<Trip> getTrips(boolean isDeleted) {
        List<Trip> trips = new ArrayList<>();
        for (Trip trip : mTripMap.values()) {
            if (trip.getIsDeleted() == isDeleted) {
                trips.add(trip);
            }
        }
        return trips;
    }

    private void initialiseTripMap() {
        if (mTripMap == null) {
            mTripMap = new TreeMap<>();
            SQLiteDatabase db = getWritableDatabase();

            String[] columns = {
                    Trip.COLUMN_ID,
                    Trip.COLUMN_TITLE,
                    Trip.COLUMN_NOTE,
                    Trip.COLUMN_START_DATE,
                    Trip.COLUMN_END_DATE,
                    Trip.COLUMN_IS_DELETED
            };

            Cursor cursor = db.query(Trip.TABLE_NAME, columns, null, null, null, null, null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                long id = cursor.getLong(cursor.getColumnIndex(Trip.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndex(Trip.COLUMN_TITLE));
                String note = cursor.getString(cursor.getColumnIndex(Trip.COLUMN_NOTE));
                String startDateString = cursor.getString(cursor.getColumnIndex(Trip.COLUMN_START_DATE));
                String endDateString = cursor.getString(cursor.getColumnIndex(Trip.COLUMN_END_DATE));
                int isDeletedCode = cursor.getInt(cursor.getColumnIndex(Trip.COLUMN_IS_DELETED));
                boolean isDeleted = isDeletedCode != 0;

                Date startDate;
                Date endDate;
                try {
                    startDate = mDateTimeFormat.parse(startDateString);
                    endDate = mDateTimeFormat.parse(endDateString);
                    Trip trip = new Trip(id, title, getPlaces(id), startDate, endDate, note, isDeleted);
                    mTripMap.put(trip.getId(), trip);
                } catch (ParseException e) {
//                    Log.e(ERROR_TAG, e.getMessage());
                }

                cursor.moveToNext();
            }
            cursor.close();
        }
    }
    //endregion

    //region Getter methods for attraction records

    /**
     * This method returns the hotel record that is associated with the given trip id.
     *
     * @param tripId             The trip id that the hotel records are associated with
     * @param attractionRecordId Target attraction record id
     * @return The hotel record that is associated with the given trip id
     */
    public AttractionRecord getAttractionRecord(long tripId, long attractionRecordId) {
        String selection = Trip.COLUMN_ID + " = " + tripId +
                " AND " + AttractionRecord.COLUMN_ID + " = " + attractionRecordId;
        List<AttractionRecord> attractionRecords = getAttractionRecords(selection);

        if (attractionRecords.size() == 1) {
            return attractionRecords.get(0);
        } else {
            return null;
        }
    }

    /**
     * This method returns a list of hotel records that is associated with the given trip id.
     * The records are sorted by its primary key in ascending order by default.
     *
     * @param tripId The trip id that the hotel records are associated with
     * @return A list of hotel records that is associated with the given trip id
     */
    public List<AttractionRecord> getAttractionRecords(long tripId) {
        String selection = Trip.COLUMN_ID + " = " + tripId;
        return getAttractionRecords(selection);
    }

    /**
     * This method returns a list of hotel records that is associated.
     *
     * @param selection Selection statement
     * @return A list of selected hotel records
     */
    private List<AttractionRecord> getAttractionRecords(String selection) {
        List<AttractionRecord> attractionRecords = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();

        String[] columns = {
                AttractionRecord.COLUMN_ID,
                AttractionRecord.COLUMN_PLACE_ID,
                AttractionRecord.COLUMN_START_DATE,
                AttractionRecord.COLUMN_DURATION,
                AttractionRecord.COLUMN_END_DATE,
                AttractionRecord.COLUMN_NOTE
        };

        Cursor cursor = db.query(AttractionRecord.TABLE_NAME, columns, selection, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long id = cursor.getLong(cursor.getColumnIndex(AttractionRecord.COLUMN_ID));
            String placeId = cursor.getString(cursor.getColumnIndex(AttractionRecord.COLUMN_PLACE_ID));
            String startDateString = cursor.getString(cursor.getColumnIndex(AttractionRecord.COLUMN_START_DATE));
            int duration = cursor.getInt(cursor.getColumnIndex(AttractionRecord.COLUMN_DURATION));
            String endDateString = cursor.getString(cursor.getColumnIndex(AttractionRecord.COLUMN_END_DATE));
            String note = cursor.getString(cursor.getColumnIndex(AttractionRecord.COLUMN_NOTE));

            Date startDateTime;
            Date endDateTime;
            try {
                startDateTime = mDateTimeFormat.parse(startDateString);
                endDateTime = mDateTimeFormat.parse(endDateString);
                AttractionRecord record = new AttractionRecord(id,
                        getPlace(placeId), startDateTime, endDateTime, note);
                record.setDuration(duration);
                attractionRecords.add(record);
            } catch (ParseException e) {
//                Log.e(ERROR_TAG, e.getMessage());
            }
            cursor.moveToNext();
        }

        cursor.close();
        return attractionRecords;
    }
    //endregion

    //region Getter methods for hotel records

    /**
     * This method returns the hotel record that is associated with the given trip id.
     *
     * @param tripId        The trip id that the hotel records are associated with
     * @param hotelRecordId Target hotel record id
     * @return The hotel record that is associated with the given trip id
     */
    public HotelRecord getHotelRecord(long tripId, long hotelRecordId) {
        String selection = HotelRecord.COLUMN_TRIP_ID + " = " + tripId +
                " AND " + HotelRecord.COLUMN_ID + " = " + hotelRecordId;
        List<HotelRecord> trips = getHotelRecords(selection);

        if (trips.size() == 1) {
            return trips.get(0);
        } else {
            return null;
        }
    }

    /**
     * This method returns a list of hotel records that is associated with the given trip id.
     * The records are sorted by its primary key in ascending order by default.
     *
     * @param tripId The trip id that the hotel records are associated with
     * @return A list of hotel records that is associated with the given trip id
     */
    public List<HotelRecord> getHotelRecords(long tripId) {
        String selection = HotelRecord.COLUMN_TRIP_ID + " = " + tripId;
        return getHotelRecords(selection);
    }

    /**
     * This method returns a list of hotel records that is associated.
     *
     * @param selection Selection statement
     * @return A list of selected hotel records
     */
    private List<HotelRecord> getHotelRecords(String selection) {
        List<HotelRecord> hotelRecords = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();

        String[] columns = {
                HotelRecord.COLUMN_ID,
                HotelRecord.COLUMN_PLACE_ID,
                HotelRecord.COLUMN_START_DATE,
                HotelRecord.COLUMN_END_DATE,
                HotelRecord.COLUMN_NOTE,
                HotelRecord.COLUMN_NUMBER_OF_GUESTS,
                HotelRecord.COLUMN_TOTAL_PRICE
        };

        Cursor cursor = db.query(HotelRecord.TABLE_NAME, columns, selection, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long id = cursor.getLong(cursor.getColumnIndex(HotelRecord.COLUMN_ID));
            String placeId = cursor.getString(cursor.getColumnIndex(ItineraryItem.COLUMN_PLACE_ID));
            String startDateString = cursor.getString(cursor.getColumnIndex(ItineraryItem.COLUMN_START_DATE));
            String endDateString = cursor.getString(cursor.getColumnIndex(ItineraryItem.COLUMN_END_DATE));
            String note = cursor.getString(cursor.getColumnIndex(ItineraryItem.COLUMN_NOTE));
            int numberOfGuests = cursor.getInt(cursor.getColumnIndex(HotelRecord.COLUMN_NUMBER_OF_GUESTS));
            double totalPrice = cursor.getDouble(cursor.getColumnIndex(HotelRecord.COLUMN_TOTAL_PRICE));

            Date startDateTime;
            Date endDateTime;
            try {
                startDateTime = mDateTimeFormat.parse(startDateString);
                endDateTime = mDateTimeFormat.parse(endDateString);
                HotelRecord record = new HotelRecord(
                        getPlace(placeId), startDateTime, endDateTime, note);
                record.setId(id);
                record.setNumberOfGuests(numberOfGuests);
                record.setTotalPrice(totalPrice);
                hotelRecords.add(record);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cursor.moveToNext();
        }

        cursor.close();
        return hotelRecords;
    }
    //endregion

    //region Getter methods for transport records
    public TransportRecord getTransportRecord(long tripId, long transportRecordId) {
        String selection = TransportRecord.COLUMN_TRIP_ID + " = " + tripId +
                " AND " + TransportRecord.COLUMN_ID + " = " + transportRecordId;
        List<TransportRecord> records = getTransportRecords(selection, null);

        if (records.size() == 1) {
            return records.get(0);
        } else {
            return null;
        }
    }

    /**
     * This method returns a list of transport records that is associated with the given trip id.
     * The records are sorted by its primary key in ascending order by default.
     *
     * @param tripId The trip id that the transport records are associated with
     * @return A list of transport records that is associated with the given trip id
     */
    public List<TransportRecord> getTransportRecords(long tripId) {
        String sortOrder = TransportRecord.COLUMN_ID + " ASC";
        return getTransportRecords(tripId, sortOrder);
    }

    /**
     * This method returns a list of transport records that is associated with the given trip id.
     *
     * @param tripId    The trip id that the transport records are associated with
     * @param sortOrder Either '[column name] ASC' or '[column name] DESC'
     * @return A list of transport records that is associated with the given trip id
     */
    public List<TransportRecord> getTransportRecords(long tripId, String sortOrder) {
        String selection = TransportRecord.COLUMN_TRIP_ID + " = " + tripId;
        return getTransportRecords(selection, sortOrder);
    }

    public List<TransportRecord> getTransportRecords(String selection, String sortOrder) {
        List<TransportRecord> transportRecords = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();

        String[] columns = {
                TransportRecord.COLUMN_ID,
                TransportRecord.COLUMN_TRANSPORT_MODE,
                TransportRecord.COLUMN_DISPLAY_MESSAGE,
                TransportRecord.COLUMN_PLACE_ID,
                TransportRecord.COLUMN_ARRIVAL_PLACE_ID,
                TransportRecord.COLUMN_START_DATE,
                TransportRecord.COLUMN_END_DATE,
                TransportRecord.COLUMN_NOTE,
                TransportRecord.COLUMN_REFERENCE_NO,
                TransportRecord.COLUMN_PRICE,
                TransportRecord.COLUMN_TRIP_ID
        };

        Cursor cursor = db.query(TransportRecord.TABLE_NAME, columns, selection, null, null, null, sortOrder);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long id = cursor.getLong(cursor.getColumnIndex(TransportRecord.COLUMN_ID));
            int transportMode = cursor.getInt(cursor.getColumnIndex(TransportRecord.COLUMN_TRANSPORT_MODE));
            String displayMessage = cursor.getString(cursor.getColumnIndex(TransportRecord.COLUMN_DISPLAY_MESSAGE));
            String placeId = cursor.getString(cursor.getColumnIndex(TransportRecord.COLUMN_PLACE_ID));
            String arrivalPlaceId = cursor.getString(cursor.getColumnIndex(TransportRecord.COLUMN_ARRIVAL_PLACE_ID));
            String startDateString = cursor.getString(cursor.getColumnIndex(TransportRecord.COLUMN_START_DATE));
            String endDateString = cursor.getString(cursor.getColumnIndex(TransportRecord.COLUMN_END_DATE));
            String note = cursor.getString(cursor.getColumnIndex(TransportRecord.COLUMN_NOTE));
            String ref = cursor.getString(cursor.getColumnIndex(TransportRecord.COLUMN_REFERENCE_NO));
            double price = cursor.getDouble(cursor.getColumnIndex(TransportRecord.COLUMN_PRICE));

            Date startDateTime;
            Date endDateTime;
            try {
                startDateTime = mDateTimeFormat.parse(startDateString);
                endDateTime = mDateTimeFormat.parse(endDateString);
                TransportRecord record = new TransportRecord(id, transportMode, displayMessage,
                        getPlace(placeId), getPlace(arrivalPlaceId), startDateTime, endDateTime, note, ref);
                record.setPrice(price);
                transportRecords.add(record);
            } catch (ParseException e) {
//                Log.e(ERROR_TAG, e.getMessage());
            }
            cursor.moveToNext();
        }
        cursor.close();

        return transportRecords;
    }

    private Place getPlace(String placeId) {
        String sql = "SELECT * FROM " + PlaceImpl.TABLE_NAME +
                " WHERE " + PlaceImpl.COLUMN_ID + " = '" + placeId + "';";
        Cursor cursor = getWritableDatabase().rawQuery(sql, null);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            return getPlace(cursor);
        }
        return null;
    }

    private List<Place> getPlaces(long tripId) {
        List<Place> places = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();

        String sql = "SELECT * FROM " + PlaceImpl.TABLE_NAME + " INNER JOIN " +
                TRIP_PLACE_LINKING_TABLE_NAME + " ON " + PlaceImpl.TABLE_NAME + "." + PlaceImpl.COLUMN_ID +
                " = " + TRIP_PLACE_LINKING_TABLE_NAME + "." + PlaceImpl.COLUMN_ID + " AND " +
                TRIP_PLACE_LINKING_TABLE_NAME + "." + Trip.COLUMN_ID + " = " + tripId + ";";

        Cursor cursor = db.rawQuery(sql, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Place place = getPlace(cursor);
            if (place != null) {
                places.add(place);
            }

            cursor.moveToNext();
        }
        cursor.close();

        return places;
    }

    // This method is only called when reading from SQLite database with a cursor.
    private Place getPlace(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(PlaceImpl.COLUMN_ID));
        String address = cursor.getString(cursor.getColumnIndex(PlaceImpl.COLUMN_ADDRESS));
        String name = cursor.getString(cursor.getColumnIndex(PlaceImpl.COLUMN_NAME));
        double latitude = cursor.getDouble(cursor.getColumnIndex(PlaceImpl.COLUMN_LATITUDE));
        double longitude = cursor.getDouble(cursor.getColumnIndex(PlaceImpl.COLUMN_LONGITUDE));
        double swLat = cursor.getDouble(cursor.getColumnIndex(PlaceImpl.COLUMN_SW_LAT));
        double swLng = cursor.getDouble(cursor.getColumnIndex(PlaceImpl.COLUMN_SW_LNG));
        double neLat = cursor.getDouble(cursor.getColumnIndex(PlaceImpl.COLUMN_NE_LAT));
        double neLng = cursor.getDouble(cursor.getColumnIndex(PlaceImpl.COLUMN_NE_LNG));
        String uriString = cursor.getString(cursor.getColumnIndex(PlaceImpl.COLUMN_WEBSITE_URI));
        String phoneNumber = cursor.getString(cursor.getColumnIndex(PlaceImpl.COLUMN_PHONE_NUMBER));
        float rating = cursor.getFloat(cursor.getColumnIndex(PlaceImpl.COLUMN_RATING));
        int priceLevel = cursor.getInt(cursor.getColumnIndex(PlaceImpl.COLUMN_PRICE_LEVEL));
        String attributions = cursor.getString(cursor.getColumnIndex(PlaceImpl.COLUMN_ATTRIBUTIONS));

        // Minimum value means the viewport is not provided, and hence is not stored in the database
        LatLngBounds viewPort = null;
        if (swLat != Double.MIN_VALUE && swLng != Double.MIN_VALUE) {
            viewPort = new LatLngBounds(new LatLng(swLat, swLng), new LatLng(neLat, neLng));
        }

        Uri websiteUri = null;
        if (uriString != null && !uriString.isEmpty()) {
            websiteUri = Uri.parse(uriString);
        }

        return new PlaceImpl(id, address, name, new LatLng(latitude, longitude), viewPort,
                websiteUri, phoneNumber, rating, priceLevel, attributions);
    }
    //endregion

    /**
     * This method deletes all records that have been stored in the SQLite database.
     *
     * @return true if all the tables are deleted
     */
    public boolean clearDatabase() {
        try {
            SQLiteDatabase database = getWritableDatabase();
            database.delete(Trip.TABLE_NAME, null, null);
            database.delete(AttractionRecord.TABLE_NAME, null, null);
            database.delete(HotelRecord.TABLE_NAME, null, null);
            database.delete(TransportRecord.TABLE_NAME, null, null);
            database.delete(PlaceImpl.TABLE_NAME, null, null);
            database.delete(TRIP_PLACE_LINKING_TABLE_NAME, null, null);
            return true;
        } catch (SQLiteException e) {
//            Log.e(ERROR_TAG, e.getMessage());
            return false;
        }
    }

    /**
     * This method returns the largest value of the given column stored in the given table.
     *
     * @param tableName  Table name
     * @param columnName Column name
     * @return The largest value of the given column stored in the given table
     */
    public long getMaximum(String tableName, String columnName) {
        String sql = "SELECT MAX(" + columnName + ") FROM " + tableName + ";";
        SQLiteStatement statement = getWritableDatabase().compileStatement(sql);
        return statement.simpleQueryForLong();
    }
}
package com.milton.archwilio.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;

/**
 * Created by Milton on 14/04/2016.
 */
@DynamoDBTable(tableName = "User")
public class User {
    //region Constants
    public static final String TABLE_NAME = "User";
    public static final String COLUMN_ID = "Id";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_TRIPS = "Trips";
    //endregion

    //region Instance variables
    private String id;
    private String name;
    private List<Trip> trips;
    //endregion

    //region Constructors
    public User() {}

    public User(String id, String name, List<Trip> trips) {
        this.id = id;
        this.name = name;
        this.trips = trips;
    }
    //endregion

    @DynamoDBHashKey(attributeName = COLUMN_ID)
    public String getId() {
        return this.id;
    }

    @DynamoDBAttribute(attributeName = COLUMN_NAME)
    public String getName() {
        return this.name;
    }

    @DynamoDBAttribute(attributeName = COLUMN_TRIPS)
    public List<Trip> getTrips() {
        return this.trips;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }
}
package com.milton.archwilio.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class initialises the credentials for the AmazonDynamoDBClient.
 * Reference: docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/dynamodbv2/AmazonDynamoDBClient.html
 */
public class DynamoDbManager {
    private static DynamoDbManager mDynamoDbManager;

    private boolean hasCredentials; // True if the user has logged in with a valid Google account
    private AmazonDynamoDBClient ddbClient;

    private DynamoDbManager(Context context) throws IOException, GoogleAuthException {
        final String IDENTITY_POOL_ID = "eu-west-1:1bf5ebe9-946e-46ea-9ee4-362b8cd0a11d";
        final String GOOGLE_CLIENT_ID =
                "535832425973-bmd3q2j6nss7hb97vtpn7hqbgqqaks2e.apps.googleusercontent.com";

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context, IDENTITY_POOL_ID, Regions.EU_WEST_1
        );
        this.ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        this.hasCredentials = false;

        //region Initialise credentials provider with Google credentials
        Account[] accounts = AccountManager.get(context)
                .getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            String scope = "audience:server:client_id:" + GOOGLE_CLIENT_ID;
            String token = GoogleAuthUtil.getToken(context, accounts[0], scope);
            Map<String, String> logins = new HashMap<>();

            if (token != null && !token.isEmpty()) {
                logins.put("accounts.google.com", token);
                credentialsProvider.setLogins(logins);
                this.hasCredentials = true;
            }
        }
        //endregion
    }

    public static synchronized DynamoDbManager getInstance(Context context) throws IOException, GoogleAuthException {
        if (mDynamoDbManager == null) {
            mDynamoDbManager = new DynamoDbManager(context);
        }
        return mDynamoDbManager;
    }

    /**
     * This method creates a table on DynamoDB.
     *
     * @param tableName          Table name
     * @param readCapacityUnits  The default value is 10L
     * @param writeCapacityUnits The default value is 5L
     * @param partitionKeyName   Partition key name
     * @param partitionKeyType   Partition key type
     */
    public void createTable(String tableName, long readCapacityUnits, long writeCapacityUnits,
                            String partitionKeyName, String partitionKeyType) {
        if (this.hasCredentials) {
            // User id acts as the partition key
            List<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(new KeySchemaElement(partitionKeyName, KeyType.HASH));

            // Create a partition key as the attribute
            List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(new AttributeDefinition(partitionKeyName, partitionKeyType));

            CreateTableRequest request = new CreateTableRequest(
                    attributeDefinitions, tableName, keySchema,
                    new ProvisionedThroughput(readCapacityUnits, writeCapacityUnits));

            this.ddbClient.createTable(request);
        }
    }

    /**
     * This method returns an instance of the AmazonDynamoDBClient.
     *
     * @return An instance of the AmazonDynamoDBClient
     */
    public AmazonDynamoDBClient getDynamoDBClient() {
        return this.ddbClient;
    }
}
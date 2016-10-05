package com.milton.archwilio.activities;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException;
import com.google.android.gms.auth.GoogleAuthException;
import com.milton.archwilio.R;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.common.DynamoDbManager;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.models.Trip;
import com.milton.archwilio.models.User;

import java.io.IOException;
import java.util.List;

/**
 * Created by Milton on 11/02/2016.
 */
public class SettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, new SettingsFragment()).commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_settings);
        }

        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getResources().getString(R.string.pref_key_language))) {
            String defaultLanguage = getResources().getStringArray(R.array.pref_language_list_values)[0];
            String selectedLanguage = mSharedPreferences.getString(key, defaultLanguage);
            sharedPreferences.edit().putString(key, selectedLanguage).apply();
        } else if (key.equals(getResources().getString(R.string.pref_key_currency))) {
            String defaultCurrency = getResources().getStringArray(R.array.pref_currency_list_values)[0];
            String selectedCurrency = mSharedPreferences.getString(key, defaultCurrency);
            sharedPreferences.edit().putString(key, selectedCurrency).apply();
        }
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference uploadPreference = findPreference(getResources().getString(R.string.pref_key_amazon_upload));
            uploadPreference.setOnPreferenceClickListener(this);

            Preference downloadPreference = findPreference(getResources().getString(R.string.pref_key_amazon_download));
            downloadPreference.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String userId = sharedPreferences.getString(Utility.PREF_KEY_USER_ID, "");
            String userName = sharedPreferences.getString(Utility.PREF_KEY_USER_NAME, "");

            if (userId.isEmpty()) {
                Toast.makeText(getActivity(),
                        R.string.error_not_logged_in, Toast.LENGTH_LONG).show();
            } else {
                if (!isNetworkAvailable()) {
                    Toast.makeText(getActivity(), R.string.error_no_internet_connection, Toast.LENGTH_LONG).show();
                } else {
                    if (preference.getKey().equals(getResources().getString(R.string.pref_key_amazon_upload))) {
                        uploadDataToAWS(userId, userName);
                        return true;
                    } else if (preference.getKey().equals(getResources().getString(R.string.pref_key_amazon_download))) {
                        downloadDataFromAWS(userId);
                        return true;
                    }
                }
            }

            return false;
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        private void uploadDataToAWS(final String userId, final String userName) {
            new AsyncTask<Void, Void, String>() {
                private ProgressDialog progressDialog;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(getActivity().getString(R.string.progressing));
                    progressDialog.show();
                }

                @Override
                protected String doInBackground(Void... params) {
                    // Populate itinerary items in the trips, as the SQLite is a
                    // relational database while DynamoDB is NoSQL.
                    List<Trip> trips = DbHelper.getInstance(getActivity()).getTrips();
                    for (Trip trip : trips) {
                        DbHelper helper = DbHelper.getInstance(getActivity());
                        trip.setAttractionRecords(helper.getAttractionRecords(trip.getId()));
                        trip.setHotelRecords(helper.getHotelRecords(trip.getId()));
                        trip.setTransportRecords(helper.getTransportRecords(trip.getId()));
                    }

                    try {
                        DynamoDbManager manager = DynamoDbManager.getInstance(getActivity());
                        DynamoDBMapper mapper = new DynamoDBMapper(manager.getDynamoDBClient());

                        // Delete the user object stored on DynamoDB
                        User cloudUser = mapper.load(User.class, userId);
                        if (cloudUser != null) {
                            mapper.delete(cloudUser);
                        }

                        // Create an user object if it does not exist on DynamoDB
                        User user = new User(userId, userName, trips);
                        mapper.save(user);

                        return null;
                    } catch (GoogleAuthException | IOException e) {
                        return getResources().getString(R.string.error_google_authentication);
                    } catch (NotAuthorizedException e) {
                        return getResources().getString(R.string.error_aws_authorisation);
                    } catch (Exception e) {
                        return getResources().getString(R.string.error_general);
                    }
                }

                @Override
                protected void onPostExecute(String errorMessage) {
                    super.onPostExecute(errorMessage);
                    progressDialog.dismiss();

                    if (!Utility.isNullOrEmpty(errorMessage)) {
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.uploaded_successfully,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }

        private void downloadDataFromAWS(final String userId) {
            new AsyncTask<Void, Void, String>() {
                private ProgressDialog progressDialog;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(getActivity().getString(R.string.progressing));
                    progressDialog.show();
                }

                @Override
                protected String doInBackground(Void... params) {
                    try {
                        DynamoDbManager manager = DynamoDbManager.getInstance(getActivity());
                        DynamoDBMapper mapper = new DynamoDBMapper(manager.getDynamoDBClient());

                        User cloudUser = mapper.load(User.class, userId);
                        if (cloudUser != null) {
                            DbHelper helper = DbHelper.getInstance(getActivity());
                            helper.clearDatabase();
                            helper.insertTrips(cloudUser.getTrips());

                            for (Trip trip : cloudUser.getTrips()) {
                                long tripId = trip.getId();

                                helper.insertAttractionRecords(tripId, trip.getAttractionRecords());
                                helper.insertHotelRecords(tripId, trip.getHotelRecords());
                                helper.insertTransportRecords(tripId, trip.getTransportRecords());
                            }

                            return null;
                        }
                        return getResources().getString(R.string.error_user_not_found);
                    } catch (GoogleAuthException | IOException e) {
                        return getResources().getString(R.string.error_google_authentication);
                    } catch (NotAuthorizedException e) {
                        return getResources().getString(R.string.error_aws_authorisation);
                    } catch (SQLiteException e) {
                        return getResources().getString(R.string.error_message_sql);
                    } catch (Exception e) {
                        return getResources().getString(R.string.error_general);
                    }
                }

                @Override
                protected void onPostExecute(String errorMessage) {
                    super.onPostExecute(errorMessage);
                    progressDialog.dismiss();

                    if (!Utility.isNullOrEmpty(errorMessage)) {
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.downloaded_successfully,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }
    }
}
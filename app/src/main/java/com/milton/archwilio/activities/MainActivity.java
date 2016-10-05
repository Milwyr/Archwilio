package com.milton.archwilio.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.milton.archwilio.R;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.common.PhotoWorker;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.common.VolleySingleton;
import com.milton.archwilio.fragments.AboutFragment;
import com.milton.archwilio.fragments.CardViewFragment;
import com.milton.archwilio.fragments.ProfileFragment;
import com.milton.archwilio.models.Trip;

import java.io.File;
import java.io.IOException;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    //region Constants
    private final int ADD_TRIP_ACTIVITY_REQUEST_CODE = 8001;
    public static final int EDIT_TRIP_ACTIVITY_REQUEST_CODE = 8002;
    private final String PROFILE_PICTURE_FOLDER_NAME = "profile picture";
    private final String KEY_CURRENT_FRAGMENT = "current_fragment";
    private final String HOME_FRAGMENT = "home_fragment";
    private final String DELETED_TRIPS_FRAGMENT = "deleted_trips_fragment";
    private final String PROFILE_FRAGMENT = "profile_fragment";
    private final String ABOUT_FRAGMENT = "about_fragment";
    //endregion

    //region Instance variables
    private CardViewFragment mCardViewFragment;
    private FloatingActionButton mFab;
    private DbHelper mDbHelper;
    private SharedPreferences mSharedPreferences;

    private DrawerLayout mDrawerLayout;
    private TextView mNavHeaderNameTextView;
    private TextView mNavHeaderEmailTextView;
    private ImageView mNavHeaderAvatar;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseComponents();
        updateUserInterface();

        // Display the fragment before the orientation changed
        if (savedInstanceState != null) {
            String currentFragmentName = savedInstanceState.getString(KEY_CURRENT_FRAGMENT, "");
            restoreFragment(currentFragmentName);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        if (resultCode == Utility.SUCCESS_RESULT_CODE) {
            if (requestCode == ADD_TRIP_ACTIVITY_REQUEST_CODE) {
                if (data != null) {
                    Trip trip = data.getParcelableExtra(Trip.TABLE_NAME);

                    // If a new trip has been added, add it to CareViewFragment
                    if (trip != null) {
                        mCardViewFragment.add(trip);

                        // Display a Snackbar message that the trip is saved
                        Snackbar.make(coordinatorLayout, R.string.saved_successfully, Snackbar.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == EDIT_TRIP_ACTIVITY_REQUEST_CODE) {
                Trip trip = data.getParcelableExtra(Trip.TABLE_NAME);

                // If a new trip has been added, add it to CareViewFragment
                if (trip != null) {
                    mCardViewFragment.update(trip);

                    // Display a Snackbar message that the trip is saved
                    Snackbar.make(coordinatorLayout, R.string.saved_successfully, Snackbar.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == Utility.FAIL_RESULT_CODE) {
            Snackbar.make(coordinatorLayout,
                    R.string.error_message_sql, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.container);
        if (currentFragment != null) {
            if (currentFragment instanceof CardViewFragment) {
                if (getTitle().equals(getResources().getString(R.string.app_name))) {
                    outState.putString(KEY_CURRENT_FRAGMENT, HOME_FRAGMENT);
                } else if (getTitle().equals(getResources().getString(R.string.title_deleted_trip_fragment))) {
                    outState.putString(KEY_CURRENT_FRAGMENT, DELETED_TRIPS_FRAGMENT);
                }
            } else if (currentFragment instanceof ProfileFragment) {
                outState.putString(KEY_CURRENT_FRAGMENT, PROFILE_FRAGMENT);
            } else if (currentFragment instanceof AboutFragment) {
                outState.putString(KEY_CURRENT_FRAGMENT, ABOUT_FRAGMENT);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                Intent intent = new Intent(this, AddTripActivity.class);
                startActivityForResult(intent, ADD_TRIP_ACTIVITY_REQUEST_CODE);
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                changeToHomeFragment(R.string.app_name, false);
                break;
            case R.id.nav_deleted_trips:
                changeToHomeFragment(R.string.title_deleted_trip_fragment, true);
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_profile:
                changeToProfileFragment();
                break;
            case R.id.nav_about:
                AboutFragment fragment = new AboutFragment();
                getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                mFab.hide();
                break;
        }

        item.setCheckable(true);
        item.setChecked(true);

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeaderView = navigationView.getHeaderView(0);
        mNavHeaderNameTextView = (TextView) navHeaderView.findViewById(R.id.navigation_header_name_text_view);
        mNavHeaderEmailTextView = (TextView) navHeaderView.findViewById(R.id.navigation_header_email_text_view);
        mNavHeaderAvatar = (ImageView) navHeaderView.findViewById(R.id.navigation_header_avatar_image_view);

        mDbHelper = DbHelper.getInstance(this);
        changeToHomeFragment(R.string.app_name, false);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /**
     * This method changes the fragment to the home page, which is a CardViewFragment.
     *
     * @param titleId   Resource id of the title of the action bar
     * @param isDeleted True if the trip is moved to trash bin
     */
    private void changeToHomeFragment(int titleId, boolean isDeleted) {
        if (!isDeleted)
            mFab.show();
        else
            mFab.hide();

        mCardViewFragment = CardViewFragment.newInstance(titleId, isDeleted);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, mCardViewFragment).commit();
    }

    /**
     * This method changes the current fragment to profile fragment.
     */
    private void changeToProfileFragment() {
        ProfileFragment fragment = ProfileFragment.newInstance(
                new ProfileFragment.LoginCallback() {
                    @Override
                    public void onLogin() {
                        updateUserInterface();
                    }

                    @Override
                    public void onLogout() {
                        updateUserInterface();
                    }
                });
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();

        mFab.hide();
    }

    /**
     * This method updates the fields on the user interface.
     */
    private void updateUserInterface() {
        boolean isSignedIn = mSharedPreferences.getBoolean(Utility.PREF_KEY_IS_SIGNED_IN_WITH_GOOGLE, false);
        if (isSignedIn) {
            String userName = mSharedPreferences.getString(Utility.PREF_KEY_USER_NAME,
                    getResources().getString(R.string.navigation_header_default_name));
            String userEmail = mSharedPreferences.getString(Utility.PREF_KEY_USER_EMAIL,
                    getResources().getString(R.string.navigation_header_default_email));

            mNavHeaderNameTextView.setText(userName);
            mNavHeaderEmailTextView.setText(userEmail);

            // Show user's profile picture fetched from Google
            String userId = mSharedPreferences.getString(Utility.PREF_KEY_USER_ID, null);
            if (!Utility.isNullOrEmpty(userId)) {
                // User's profile picture stored in the device
                File profilePictureFolder = new File(getExternalFilesDir(null), PROFILE_PICTURE_FOLDER_NAME);
                File profilePictureFile = new File(profilePictureFolder, userId + ".jpg");

                if (profilePictureFile.exists()) {
                    mNavHeaderAvatar.setImageURI(Uri.fromFile(profilePictureFile));
                } else {
                    downloadProfilePicture(userId + ".jpg");
                }
            }
        } else {
            mNavHeaderNameTextView.setText(R.string.navigation_header_default_name);
            mNavHeaderEmailTextView.setText(R.string.navigation_header_default_email);
            mNavHeaderAvatar.setImageResource(R.drawable.ic_default_avatar_72dp);
        }
    }

    /**
     * This method downloads user's profile picture and
     * displays it on the header of navigation drawer.
     *
     * @param filePath file name + extension of the photo, such as photo.jpg
     */
    private void downloadProfilePicture(final String filePath) {
        String url = mSharedPreferences.getString(Utility.PREF_KEY_USER_PHOTO_URL, null);
        ImageRequest request = new ImageRequest(
                url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                PhotoWorker worker = new PhotoWorker(MainActivity.this);
                try {
                    worker.savePhoto(response, PROFILE_PICTURE_FOLDER_NAME, filePath);
                    mNavHeaderAvatar.setImageBitmap(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 256, 256, ImageView.ScaleType.CENTER_CROP, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * This method changes the current fragment to the fragment before the orientation changed.
     *
     * @param currentFragmentName Name of the current fragment
     */
    private void restoreFragment(String currentFragmentName) {
        if (!currentFragmentName.isEmpty()) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

            switch (currentFragmentName) {
                case HOME_FRAGMENT:
                    changeToHomeFragment(R.string.app_name, false);
                    navigationView.getMenu().getItem(0).setCheckable(true);
                    navigationView.getMenu().getItem(0).setChecked(true);
                    break;
                case DELETED_TRIPS_FRAGMENT:
                    changeToHomeFragment(R.string.title_deleted_trip_fragment, true);
                    navigationView.getMenu().getItem(1).setCheckable(true);
                    navigationView.getMenu().getItem(1).setChecked(true);
                    break;
                case PROFILE_FRAGMENT:
                    changeToProfileFragment();
                    navigationView.getMenu().getItem(2).setCheckable(true);
                    navigationView.getMenu().getItem(2).setChecked(true);
                    break;
                case ABOUT_FRAGMENT:
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, new AboutFragment()).commit();
                    navigationView.getMenu().getItem(3).setCheckable(true);
                    navigationView.getMenu().getItem(3).setChecked(true);
                    mFab.hide();
            }
        }
    }
}
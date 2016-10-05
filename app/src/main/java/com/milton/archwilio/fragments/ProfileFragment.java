package com.milton.archwilio.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.milton.archwilio.R;
import com.milton.archwilio.common.PhotoWorker;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.common.VolleySingleton;

import java.io.File;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    private String PROFILE_PICTURE_FOLDER_NAME = "profile picture";

    private static LoginCallback mLoginCallback;

    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences mSharedPreferences;

    private ImageView mProfileImageView;
    private TextView mProfileTextView;
    private SignInButton mSignInButton;
    private Button mSignOutButton;

    /**
     * This callback is used to inform the MainActivity the login and logout events.
     */
    public interface LoginCallback {
        void onLogin();

        void onLogout();
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param callback A callback
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance(LoginCallback callback) {
        ProfileFragment fragment = new ProfileFragment();
        mLoginCallback = callback;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.title_profile_fragment);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mProfileImageView = (ImageView) rootView.findViewById(R.id.profile_picture);
        mProfileTextView = (TextView) rootView.findViewById(R.id.profile_text_view);
        mSignInButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(this);

        mSignOutButton = (Button) rootView.findViewById(R.id.sign_out_button);
        mSignOutButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            GoogleSignInResult result = opr.get();
            updateUserInterface(result.isSuccess(), result.getSignInAccount());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.SIGN_IN_REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            updateUserInterface(result.isSuccess(), result.getSignInAccount());

            GoogleSignInAccount account = result.getSignInAccount();
            if (account != null) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(Utility.PREF_KEY_IS_SIGNED_IN_WITH_GOOGLE, result.isSuccess());
                editor.putString(Utility.PREF_KEY_USER_ID, account.getId());
                editor.putString(Utility.PREF_KEY_USER_NAME, account.getDisplayName());
                editor.putString(Utility.PREF_KEY_USER_EMAIL, account.getEmail());

                if (account.getPhotoUrl() != null) {
                    editor.putString(Utility.PREF_KEY_USER_PHOTO_URL, account.getPhotoUrl().toString());
                }
                editor.apply();
                mLoginCallback.onLogin();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, Utility.SIGN_IN_REQUEST_CODE);
                break;
            case R.id.sign_out_button:
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                // Success means logout successfully
                                boolean isSignIn = !status.isSuccess();
                                if (!isSignIn) {
                                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                                    editor.remove(Utility.PREF_KEY_USER_ID);
                                    editor.remove(Utility.PREF_KEY_USER_NAME);
                                    editor.remove(Utility.PREF_KEY_USER_EMAIL);
                                    editor.remove(Utility.PREF_KEY_USER_PHOTO_URL);
                                    editor.putBoolean(Utility.PREF_KEY_IS_SIGNED_IN_WITH_GOOGLE, false).apply();
                                    editor.apply();
                                }

                                updateUserInterface(!status.isSuccess());
                                mLoginCallback.onLogout();
                            }
                        }
                );
                break;
            case R.id.fab:
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
        }
    }

    /**
     * This method changes the visibility of the login/logout button depending on the login state.
     *
     * @param isSignedIn True if the user has signed in
     */
    private void updateUserInterface(boolean isSignedIn) {
        updateUserInterface(isSignedIn, null);
    }

    /**
     * This method changes the visibility of the views depending on the login state.
     *
     * @param isSignedIn True if the user has signed in
     * @param account    Sign in account of Google
     */
    private void updateUserInterface(boolean isSignedIn, GoogleSignInAccount account) {
        if (isSignedIn) {
            mSignInButton.setVisibility(View.GONE);
            mSignOutButton.setVisibility(View.VISIBLE);

            if (account != null) {
                File profilePictureFolder = new File(
                        getActivity().getExternalFilesDir(null), PROFILE_PICTURE_FOLDER_NAME);
                File profilePictureFile = new File(profilePictureFolder, account.getId() + ".jpg");

                if (profilePictureFile.exists()) {
                    // Display the profile picture if it has been downloaded
                    mProfileImageView.setImageURI(Uri.fromFile(profilePictureFile));
                } else {
                    downloadProfilePicture(profilePictureFile.getPath());
                }

                mProfileTextView.setText(account.getDisplayName());
            }
        } else {
            mSignInButton.setVisibility(View.VISIBLE);
            mSignOutButton.setVisibility(View.GONE);
            mProfileImageView.setImageResource(R.drawable.ic_default_avatar_72dp);
            mProfileTextView.setText(R.string.default_name);
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
                PhotoWorker worker = new PhotoWorker(getActivity());
                try {
                    worker.savePhoto(response, PROFILE_PICTURE_FOLDER_NAME, filePath);
                    mProfileImageView.setImageBitmap(response);
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
        VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }
}
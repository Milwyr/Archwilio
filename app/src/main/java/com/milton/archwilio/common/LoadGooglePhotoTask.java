package com.milton.archwilio.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.milton.archwilio.R;

/**
 * This asynchronous task loads the first matched photo loaded from Google.
 * Refer to https://developers.google.com/places/android-api/photos
 */
public class LoadGooglePhotoTask extends AsyncTask<String, Integer, Bitmap> {

    private final int PROGRESS_FINISH = 100;

    private double aspectRatio;
    private Context context;
    private GoogleApiClient googleApiClient;
    private ImageView imageView;
    private ProgressDialog progressDialog;

    //region Constructors

    /**
     * Constructor
     *
     * @param context Context
     */
    public LoadGooglePhotoTask(Context context) {
        this(context, -1d, null);
    }

    /**
     * Constructor
     *
     * @param context     Context
     * @param aspectRatio Aspect ratio of the target photo, which is calculated by  weight / height
     */
    public LoadGooglePhotoTask(Context context, double aspectRatio) {
        this(context, aspectRatio, null);
    }

    /**
     * Constructor
     *
     * @param context   Context
     * @param imageView The image view to display the photo
     */
    public LoadGooglePhotoTask(Context context, ImageView imageView) {
        this(context, -1d, imageView);
    }

    /**
     * Constructor
     *
     * @param context     Context
     * @param aspectRatio Aspect ratio of the target photo, which is calculated by  weight / height
     * @param imageView   The image view to display the photo
     */
    public LoadGooglePhotoTask(Context context, double aspectRatio, ImageView imageView) {
        this.context = context;
        this.aspectRatio = aspectRatio;
        this.imageView = imageView;

        this.googleApiClient = new GoogleApiClient
                .Builder(this.context).addApi(Places.GEO_DATA_API).build();
        this.googleApiClient.connect();
    }

    //endregion

    /**
     * This method loads the first photo for a place id from the Geo Data API.
     *
     * @param id A place id retrieved from Google Places API
     * @return The first matched photo, null if no matched photo is found
     */
    @Override
    protected Bitmap doInBackground(String... id) {
        final String placeId = id[0];
        PlacePhotoMetadataResult result = Places.GeoDataApi
                .getPlacePhotos(this.googleApiClient, placeId).await();

        if (result.getStatus().isSuccess()) {
            PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
            int numberOfPhotos = photoMetadataBuffer.getCount();

            if (numberOfPhotos > 0 && !isCancelled()) {
                for (int i = 0; i < numberOfPhotos; i++) {
                    publishProgress((i + 1) * 100 / numberOfPhotos);

                    // Load the thumbnail from buffer to determine the aspect ratio
                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(i);
                    Bitmap thumbnail = photoMetadata.getScaledPhoto(
                            this.googleApiClient, 160, 90).await().getBitmap();

                    // Return the photo if the aspect ratio is unspecified,
                    // i.e. aspect ratio is -1
                    if (Math.abs(this.aspectRatio - -1d) < 0.0001) {
                        Bitmap bitmap = photoMetadata.getScaledPhoto(
                                this.googleApiClient, 1920, 1080).await().getBitmap();
                        publishProgress(PROGRESS_FINISH);
                        photoMetadataBuffer.release();
                        return bitmap;
                    }

                    // Calculate the aspect ratio of the photo
                    double aspectRatio = (double) thumbnail.getWidth() / (double) thumbnail.getHeight();

                    // Return the photo if the aspect ratio matches the requirement
                    if (Math.abs(this.aspectRatio - aspectRatio) < 0.01) {
                        Bitmap bitmap = photoMetadata.getScaledPhoto(
                                this.googleApiClient, 1920, 1080).await().getBitmap();
                        photoMetadataBuffer.release();
                        publishProgress(PROGRESS_FINISH);
                        return bitmap;
                    }
                }
            }
            photoMetadataBuffer.release();
        }
        publishProgress(PROGRESS_FINISH);
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);

        if (this.progressDialog == null) {
            this.progressDialog = new ProgressDialog(this.context);
            this.progressDialog.setTitle(this.context.getResources().getString(R.string.progressing));
        }

        this.progressDialog.setProgress(progress[0]);
        this.progressDialog.setMessage(progress[0] + "%");

        if (progress[0] == PROGRESS_FINISH) {
            this.progressDialog.hide();
        } else {
            this.progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (this.imageView != null) {
            if (bitmap != null)
                this.imageView.setImageBitmap(bitmap);
            else
                this.imageView.setImageResource(R.drawable.ic_indigo_light_wide);
        }
    }
}
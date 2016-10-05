package com.milton.archwilio.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Milton on 30/03/2016.
 */
public class PhotoWorker {
    private Context mContext;

    //region Constructor

    /**
     * Constructor
     *
     * @param context Context
     */
    public PhotoWorker(Context context) {
        mContext = context;
    }
    //endregion

    //region Fetch photo urls from Wikimedia
    public interface ResultsCallback {
        void onFinish(List<String> photoUrls);

        void onError(Exception e);
    }

    /**
     * This method fetches the photo urls from Wikimedia, and returns the urls via callback.
     *
     * @param maxNum      Maximum number of photo urls
     * @param latLng      A LatLng object that consists of latitude and longitude of the location
     * @param aspectRatio Aspect ratio of the photos to look for
     * @param callback    A callback that returns the photo urls
     */
    public void fetchPhotoUrls(final int maxNum, LatLng latLng,
                               final double aspectRatio, final ResultsCallback callback) {
        // Reference 1: https://www.mediawiki.org/wiki/API:Imageinfo
        // Reference 2: https://www.mediawiki.org/wiki/API:Showing_nearby_wiki_information
        // Reference 3: https://www.mediawiki.org/wiki/Extension:GeoData
        String url = "https://commons.wikimedia.org/w/api.php?action=query&generator=geosearch&ggsprimary=all&ggsnamespace=6&ggsradius=350&ggscoord=" +
                latLng.latitude + "%7C" + latLng.longitude + "&ggslimit=30&prop=imageinfo&iilimit=1&iiurlwidth=1080&iiprop=dimensions|url&format=json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Save the url fetched from the JSON object from Wikimedia
                        try {
                            traverseJsonObject(response, maxNum, aspectRatio, callback);
                        } catch (JSONException e) {
                            callback.onError(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error);
                    }
                }
        );
        RequestQueue requestQueue = VolleySingleton.getInstance(mContext).getRequestQueue();
        requestQueue.add(jsonObjectRequest);
    }

    private void traverseJsonObject(JSONObject response, int maxNum,
                                    double aspectRatio, ResultsCallback callback) throws JSONException {
        JSONObject pagesObject = ((JSONObject) response.get("query")).getJSONObject("pages");
        Iterator<String> keys = pagesObject.keys();
        List<String> photoUrls = new ArrayList<>();

        while (keys.hasNext() && photoUrls.size() < maxNum) {
            JSONObject pageObject = pagesObject.getJSONObject(keys.next());
            JSONArray imageInfoArray = (JSONArray) pageObject.get("imageinfo");
            JSONObject imageInfoObject = (JSONObject) imageInfoArray.get(0);

            String url = imageInfoObject.getString("thumburl");
            int width = imageInfoObject.getInt("width");
            int height = imageInfoObject.getInt("height");

            double wikiPhotoAspectRatio = (double) width / (double) height;

            // Save the photo if the aspect ratio matches the requirement
            // and the number of urls has not exceeded the maximum number.
            // The minimum width of the photo is 300px.
            if (Math.abs(aspectRatio - wikiPhotoAspectRatio) < 0.01 && width > 360) {
                // Use thumbnail if the photo is too large
                if (width > 1080) {
                    url = imageInfoObject.getString("thumburl");
                }

                photoUrls.add(url);
            }
        }

        callback.onFinish(photoUrls);
    }
    //endregion

    //region Save photo

    /**
     * This method saves the given bitmap as a file in the given folder path with the given name.
     * The root directory is retreived by context.externalCacheDir().
     *
     * @param bitmap     The bitmap to save as a photo
     * @param folderPath Path of the folder that contains the saved photo
     * @param photoPath  File path of the photo, including extension, such as photo.jpg
     * @throws IOException
     */
    public void savePhoto(Bitmap bitmap, String folderPath, String photoPath) throws IOException {
        if (bitmap != null) {
            // Scale down the photo if it is larger than the device resolution
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
            if (bitmap.getWidth() > metrics.widthPixels || bitmap.getHeight() > metrics.heightPixels) {
                bitmap = Bitmap.createScaledBitmap(
                        bitmap, metrics.widthPixels, metrics.heightPixels, false);
            }

            // Create a file object for the photo to save
            File rootDirectory = mContext.getExternalFilesDir(null);
            File photoFolder = new File(rootDirectory, folderPath);
            File photoFile = new File(photoFolder, photoPath);

            // Create the parent directory of the photo file if it does not exist
            if (!photoFolder.exists()) {
                photoFolder.mkdirs();
            }

            // Create the photo file if it does not exist
            if (!photoFile.exists()) {
                photoFile.createNewFile();
            }

            // Save the file to the given file path
            if (photoFile.exists()) {
                FileOutputStream out = new FileOutputStream(photoFile);
                bitmap.compress(getCompressFormat(photoFile.getName()), 100, out);
            }
        }
    }

    private Bitmap.CompressFormat getCompressFormat(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf("."));

        if (extension.equalsIgnoreCase(".jpg")) {
            return Bitmap.CompressFormat.JPEG;
        } else if (extension.equalsIgnoreCase(".webp")) {
            return Bitmap.CompressFormat.WEBP;
        } else {
            return Bitmap.CompressFormat.PNG;
        }
    }
    //endregion
}
package com.milton.archwilio.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.milton.archwilio.R;
import com.milton.archwilio.activities.EditTripActivity;
import com.milton.archwilio.activities.ItineraryActivity;
import com.milton.archwilio.activities.MainActivity;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.common.PhotoWorker;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.common.VolleySingleton;
import com.milton.archwilio.models.Trip;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

/**
 * Reference: http://developer.android.com/training/material/lists-cards.html
 */
public class TripRecycleViewAdapter extends RecyclerView.Adapter<TripRecycleViewAdapter.ViewHolder> {
    private Context mContext;
    private boolean mIsDeleted;
    private Callback mCallback;
    private List<Trip> mTrips;
    private String datePatternString;

    /**
     * This callback propagates the event when a trip is deleted.
     */
    public interface Callback {
        void onTripMovedToTrash(long deletedTripId);

        void onTripDeletedPermanently(long deletedTripId);

        void onTripRestored(long restoredTripId);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        protected CardView cardView;
        protected ImageView imageView;
        protected TextView titleTextView;
        protected TextView datesTextView;
        protected ImageView deleteTripImageView;

        protected ViewHolder(View v) {
            super(v);
            cardView = (CardView) v.findViewById(R.id.trip_card_view);
            imageView = (ImageView) v.findViewById(R.id.trip_card_view_image);
            titleTextView = (TextView) v.findViewById(R.id.trip_name_text);
            datesTextView = (TextView) v.findViewById(R.id.trip_dates_text);
            deleteTripImageView = (ImageView) v.findViewById(R.id.delete_trip_image);
        }
    }

    /**
     * @param context   Context
     * @param isDeleted True if the trip is deleted
     * @param trips     A list of trips
     * @param callback  Callback
     */
    public TripRecycleViewAdapter(Context context, boolean isDeleted, List<Trip> trips, Callback callback) {
        mContext = context;
        mIsDeleted = isDeleted;
        mCallback = callback;
        mTrips = trips;
        initialiseDatePatternString();
    }

    @Override
    public TripRecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TripRecycleViewAdapter.ViewHolder holder, final int position) {
        final Trip trip = mTrips.get(position);

        // Initialise the card view and the associated views if it has not been initialised
        if (holder.cardView.getTag() == null) {
            // Initialise card view
            holder.cardView.setTag(trip.getId());
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Save the current trip id in the shared preferences
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(mContext).edit();
                    editor.putLong(Utility.PREF_KEY_CURRENT_TRIP_ID, trip.getId()).apply();

                    // Start ItineraryActivity
                    Intent intent = new Intent(mContext, ItineraryActivity.class);
                    mContext.startActivity(intent);
                }
            });

            final int widthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
            // Display the city's image fetched from Wikimedia on the CardView
            new PhotoWorker(mContext).fetchPhotoUrls(3, trip.getDestinations().get(0).getLatLng(),
                    3d / 2d, new PhotoWorker.ResultsCallback() {
                        @Override
                        public void onFinish(List<String> photoUrls) {
                            if (photoUrls.size() > 0) {
                                ImageRequest imageRequest = new ImageRequest(photoUrls.get(0),
                                        new Response.Listener<Bitmap>() {
                                            @Override
                                            public void onResponse(Bitmap response) {
                                                holder.imageView.setImageBitmap(response);
                                            }
                                        }, widthPixels, widthPixels * 9 / 16, ImageView.ScaleType.CENTER_CROP, null,
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {

                                            }
                                        }
                                );
                                VolleySingleton.getInstance(mContext).addToRequestQueue(imageRequest);
                            }
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });

            holder.titleTextView.setText(trip.getTitle());

            SimpleDateFormat sdf = new SimpleDateFormat(
                    this.datePatternString, Utility.getLocale(mContext));
            String dateText = sdf.format(trip.getStartDate()) +
                    " " + mContext.getResources().getString(R.string.to) + " " + sdf.format(trip.getEndDate());
            holder.datesTextView.setText(dateText);

            //TODO: Format the code properly
            holder.deleteTripImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show an alert dialog to ask what the user wants to do
                    long currentTripId = (long) holder.cardView.getTag();
                    showAlertDialog(currentTripId);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mTrips.size();
    }

    /**
     * This method adds a trip to the list of trips, and the list is sorted in descending order.
     *
     * @param trip A new trip
     */
    public void add(Trip trip) {
        mTrips.add(trip);
        Collections.sort(mTrips, Collections.reverseOrder(new Trip.DateComparator()));
        notifyDataSetChanged();
    }

    /**
     * This method updates the given trip found from the list of trips.
     *
     * @param updatedTrip The updated trip
     * @return True if the given trip is found and updated
     */
    public boolean update(Trip updatedTrip) {
        int position = 0;
        for (Trip trip : mTrips) {
            if (trip.getId() == updatedTrip.getId()) {
                mTrips.set(position, updatedTrip);
                notifyItemChanged(position);
                return true;
            }
            position++;
        }
        return false;
    }

    /**
     * This method removes the given trip from the list of trips.
     *
     * @param tripId Id of the trip to remove
     * @return True if the given trip is deleted
     */
    public boolean remove(long tripId) {
        int index = 0;
        for (Trip trip : mTrips) {
            if (trip.getId() == tripId) {
                mTrips.remove(trip);
                notifyItemRemoved(index);
                return true;
            }
            index++;
        }
        return false;
    }

    private void initialiseDatePatternString() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        String selectedLanguage = sharedPreferences.getString(
                mContext.getResources().getString(R.string.pref_key_language), "en");
        if (selectedLanguage.equals("zh") || selectedLanguage.equals("ja")) {
            this.datePatternString = Utility.DATE_FORMAT_PATTERN_ASIA;
        } else {
            this.datePatternString = Utility.DATE_FORMAT_PATTERN;
        }
    }

    // This method shows an alert dialog embedded with two options depending on the delete status.
    private void showAlertDialog(final long currentTripId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        if (mIsDeleted) {
            builder.setItems(R.array.deleted_list_dialog_choices, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int index) {
                    if (index == 0) {
                        // Restore the trip from recycle bin
                        DbHelper.getInstance(mContext)
                                .updateTripIsDeletedStatus(currentTripId, false);
                        mCallback.onTripRestored(currentTripId);
                    } else if (index == 1) {
                        // Delete the trip permanently
                        DbHelper.getInstance(mContext).deleteTrip(currentTripId);
                        mCallback.onTripDeletedPermanently(currentTripId);
                    }
                }
            });
        } else {
            builder.setItems(R.array.list_dialog_choices, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int index) {
                    if (index == 0) {
                        // Edit trip
                        Intent intent = new Intent(mContext, EditTripActivity.class);
                        intent.putExtra(Utility.PREF_KEY_CURRENT_TRIP_ID, currentTripId);
                        ((MainActivity) mContext).startActivityForResult(
                                intent, MainActivity.EDIT_TRIP_ACTIVITY_REQUEST_CODE);
                    } else if (index == 1) {
                        // Move the trip to trash
                        if (currentTripId != -1) {
                            DbHelper.getInstance(mContext)
                                    .updateTripIsDeletedStatus(currentTripId, true);
                            // Inform CardViewFragment the current trip is deleted
                            mCallback.onTripMovedToTrash(currentTripId);
                        }
                    }
                }
            });
        }
        builder.create().show();
    }
}
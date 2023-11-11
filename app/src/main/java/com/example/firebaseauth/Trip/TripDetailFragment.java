package com.example.firebaseauth.Trip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.firebaseauth.DatabaseModel.TripDB;
import com.example.firebaseauth.DatabaseModel.TripDetail;
import com.example.firebaseauth.Library.CustomLatLng;
import com.example.firebaseauth.Library.FirestoreUtil;
import com.example.firebaseauth.Utility.MainActivity;
import com.example.firebaseauth.Library.MemoryGalleryFragment;
import com.example.firebaseauth.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TripDetailFragment extends Fragment {

    private GoogleMap map;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private View rootView;

    public TripDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.trip_record_detail, container, false);

        // Handle the back button click
        ImageButton backButton = rootView.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            if(getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setBottomNavigationVisibility(View.VISIBLE);
            }
            getActivity().onBackPressed();
        });


        TripDB selectedTrip = (TripDB) getArguments().getSerializable("selected_trip");
        if (selectedTrip != null) {
            // Initialize the map fragment
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.tripDetailMap);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this::initMap);
            }
        }
        return rootView;
    }

    private void initMap(@NonNull GoogleMap googleMap) {
        map = googleMap;
        TripDB selectedTrip = (TripDB) getArguments().getSerializable("selected_trip");
        if (selectedTrip != null && selectedTrip.getLocationList() != null) {
            logTripDetailsCount(selectedTrip);
            addLocationsToRoute(selectedTrip.getLocationList());
            addMarker(selectedTrip);
        }
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Get the data associated with the clicked marker.
                // This could be stored in the marker's tag or fetched from a list.
                TripDetail tripDetail = (TripDetail) marker.getTag();

                if (tripDetail != null) {
                    openMemoryGalleryWithDetails(tripDetail);
                    return true;  // Return true to indicate that the marker click event was handled.
                }
                return false;
            }
        });
    }
    private void openMemoryGalleryWithDetails(TripDetail tripDetail) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedTripDetail", tripDetail);

        // Assuming MemoryGalleryFragment is the fragment for the memory gallery screen.
        MemoryGalleryFragment fragment = new MemoryGalleryFragment();
        fragment.setArguments(bundle);

        // Replace the current fragment with MemoryGalleryFragment. (Change R.id.container to your actual container's ID)
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.linearLayout, fragment)
                .addToBackStack(null)
                .commit();
    }
    private BitmapDescriptor resizeMarker(int resource, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resource);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

    private void addLocationsToRoute(List<CustomLatLng> locations) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .color(Color.rgb(255, 165, 0))
                .width(10)
                .clickable(true);

        for (CustomLatLng customLatLng : locations) {
            polylineOptions.add(customLatLng.toLatLng());
        }

        map.addPolyline(polylineOptions);

        // Check if there are locations in the list
        if (!locations.isEmpty()) {
            int markerWidth = 100; // Adjust these values as needed
            int markerHeight = 100;
            BitmapDescriptor startIconResized = resizeMarker(R.drawable.start_marker, markerWidth, markerHeight);
            BitmapDescriptor endIconResized = resizeMarker(R.drawable.end_marker, markerWidth, markerHeight);
            // Add a marker at the starting point
            map.addMarker(new MarkerOptions()
                    .position(locations.get(0).toLatLng())
                    .title("Start")
                    .icon(startIconResized)
                    .anchor(0.5f, 0.45f));// Green color for the start marker

            // Add a marker at the endpoint
            if (locations.size() > 1) {
                map.addMarker(new MarkerOptions()
                        .position(locations.get(locations.size() - 1).toLatLng())
                        .title("End")
                        .icon(endIconResized)
                        .anchor(0.5f, 0.45f));  // Red color for the end marker
            }
        }

        moveCameraToRoute(locations);
    }

    private void addMarker(TripDB selectedTrip) {
        if (!selectedTrip.getLocationList().isEmpty()) {
            CustomLatLng location = selectedTrip.getLocationList().get(0);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            getTripDetailForTrip(selectedTrip, new FirestoreUtil.TripDetailCallback() {
                @Override
                public void onTripDetailsLoaded(List<TripDetail> tripDetails) {
                    if (!tripDetails.isEmpty()) {

                        // Sort the tripDetails list by date.
                        Collections.sort(tripDetails, (o1, o2) -> o1.getDate().compareTo(o2.getDate()));

                        // Extract the start and end dates from the sorted list.
                        Date startDate = tripDetails.get(0).getDate();
                        Date endDate = tripDetails.get(tripDetails.size() - 1).getDate();

                        // Format the date range.
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                        String formattedStartDate = sdf.format(startDate);
                        String formattedEndDate = sdf.format(endDate);
                        String dateDuration = formattedStartDate + " - " + formattedEndDate;

                        // Get country name using the Geocoder.
                        // but the code to get the country name seems run very slow
                        Geocoder geocoder = new Geocoder(getContext());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String countryName = addresses.get(0).getCountryName();
                                dateDuration += " · " + countryName;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Update the TextView in the XML.
                        TextView dateDurationTextView = rootView.findViewById(R.id.dateDuration);
                        dateDurationTextView.setText(dateDuration);

                        for (TripDetail tripDetail : tripDetails) {
                            Marker marker = map.addMarker(new MarkerOptions().position(latLng).title(selectedTrip.getEmail()));
                            marker.setTag(tripDetail);
                            if (tripDetail.getImageUri() != null) {
                                String imageUrl = tripDetail.getImageUris().get(0);
                                setMarkerIconFromUrl(marker, imageUrl, 100, 100);
                            }
                        }
                    }
                }
            });
        }
    }

    private Bitmap resizeBitmap(Bitmap resource, int targetWidth, int targetHeight) {
        int newWidth = targetWidth;
        int newHeight = targetHeight;
        return Bitmap.createScaledBitmap(resource, newWidth, newHeight, false);
    }
    private void getMarkerBitmapFromView(String imageUrl, int width, int height, final Marker marker) {
        View customMarkerView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

        // For the background
        ImageView markerBackgroundView = customMarkerView.findViewById(R.id.image_view);
        markerBackgroundView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.marker_background_width);
        markerBackgroundView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.marker_background_height);
        markerBackgroundView.requestLayout();

        // For the image
        ImageView markerImageView = customMarkerView.findViewById(R.id.marker_image);
        markerImageView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.marker_image_width);
        markerImageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.marker_image_height);
        markerImageView.requestLayout();

        Glide.with(getContext())
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().circleCrop())
                .into(new CustomTarget<Bitmap>(width, height) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Bitmap resizedResource = resizeBitmap(resource, (int) getResources().getDimension(R.dimen.marker_image_width),
                                (int) getResources().getDimension(R.dimen.marker_image_height));
                        markerImageView.setImageBitmap(resizedResource);

                        // Now that the image is loaded, let's draw the composite view.
                        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
                        customMarkerView.buildDrawingCache();
                        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(returnedBitmap);
                        Drawable drawable = customMarkerView.getBackground();
                        if (drawable != null) drawable.draw(canvas);
                        customMarkerView.draw(canvas);

                        // Now, set the marker with the composite bitmap
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(returnedBitmap));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private void setMarkerIconFromUrl(Marker marker, String url, int targetWidth, int targetHeight) {
        getMarkerBitmapFromView(url, targetWidth, targetHeight, marker);
    }




    private void getTripDetailForTrip(TripDB trip, FirestoreUtil.TripDetailCallback callback) {
        FirestoreUtil firestoreUtil = new FirestoreUtil();
        firestoreUtil.getTripDetailForTrip(trip, callback);
    }


    private void moveCameraToRoute(List<CustomLatLng> locations) {
        double latSum = 0, lngSum = 0;
        for (CustomLatLng location : locations) {
            latSum += location.getLatitude();
            lngSum += location.getLongitude();
        }
        double latCenter = latSum / locations.size();
        double lngCenter = lngSum / locations.size();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latCenter, lngCenter), 12));
    }

    private void logTripDetailsCount(TripDB trip) {
        List<TripDetail> tripDetails = getTripDetailsForTrip(trip); //
    }

    private List<TripDetail> getTripDetailsForTrip(TripDB trip) {
        return new ArrayList<>();
    }

}

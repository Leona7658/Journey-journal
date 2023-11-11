package com.example.firebaseauth.Library;

/**
 * This Java file is about to handle the world map view in map tabs in library
 * It is link with content_library_global_map.xml
 */

import static com.example.firebaseauth.Trip.Trip.getEmail;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.firebaseauth.Utility.MainActivity;
import com.example.firebaseauth.R;
import com.example.firebaseauth.DatabaseModel.TripDB;
import com.example.firebaseauth.DatabaseModel.TripDetail;
import com.example.firebaseauth.Trip.TripDetailFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.widget.ImageView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.ArrayList;
import java.util.List;

public class GlobalMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;  // Declare mMap as a member variable
    private List<TripDB> tripList = new ArrayList<>();
    private FirestoreUtil firestoreUtil = new FirestoreUtil();

    private FusedLocationProviderClient mFusedLocationClient;


    private String currentUserEmail;


    public GlobalMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.content_library_global_map, container, false);

        currentUserEmail = getEmail();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());



        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapLibrary);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set the map type to be normal
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            mFusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10));
                    }
                }
            });
        }



        firestoreUtil.getAllTrips(new FirestoreUtil.TripsCallback() {
            @Override
            public void onTripsLoaded(List<TripDB> trips) {
                if (trips != null && !trips.isEmpty()) {
                    if (currentUserEmail == null){
                        currentUserEmail = "wrongemail.com";
                    }
                    for (TripDB trip : trips) {
                        String tripEmail = trip.getEmail();
                        if(tripEmail != null && tripEmail.equals(currentUserEmail)) {
                            // Check if the trip's email matches current user's email
                            tripList.add(trip);
                            addMarkerOnMap(trip);
                        }
                    }
                }
            }


        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                TripDB clickedTrip = (TripDB) marker.getTag();

                if (clickedTrip != null) {
                    startTripDetailFragment(clickedTrip);
                }
                return false;
            }
        });
    }

    private void addMarkerOnMap(TripDB trip) {
        if (trip.getLocationList() != null && !trip.getLocationList().isEmpty()) {
            CustomLatLng customLatLng = trip.getLocationList().get(0);
            LatLng position = customLatLng.toLatLng();


            firestoreUtil.getTripDetailForTrip(trip, new FirestoreUtil.TripDetailCallback() {
                @Override
                public void onTripDetailsLoaded(List<TripDetail> tripDetails) {
                    if (tripDetails != null && !tripDetails.isEmpty()) {
                        for(TripDetail tripDetail : tripDetails) {
                            String imageUrl;
                            if (tripDetail.getImageUris() != null && !tripDetail.getImageUris().isEmpty()) {
                                imageUrl = tripDetail.getImageUris().get(0);

                                Marker marker = mMap.addMarker(new MarkerOptions().position(position).title(trip.getEmail()));
                                marker.setTag(trip);

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

    private void startTripDetailFragment(TripDB trip) {
        TripDetailFragment tripDetailFragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("selected_trip", trip);
        tripDetailFragment.setArguments(args);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.linearLayout, tripDetailFragment)
                .addToBackStack(null)
                .commit();

        // Hide bottom navigation bar
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(View.GONE);
        }
    }

}

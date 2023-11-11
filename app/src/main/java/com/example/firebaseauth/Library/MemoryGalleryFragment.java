package com.example.firebaseauth.Library;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.firebaseauth.R;
import com.example.firebaseauth.DatabaseModel.TripDetail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MemoryGalleryFragment extends Fragment {
    private TripDetail tripDetail;
    private int currentImageIndex = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the TripDetail from the arguments passed to this fragment
        if (getArguments() != null) {
            tripDetail = (TripDetail) getArguments().getSerializable("selectedTripDetail");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.content_memory_gallery, container, false);

        // Assign views from the XML to variables
        TextView toolbarTitle = view.findViewById(R.id.toolbarTitle);
        TextView locationText = view.findViewById(R.id.locationText);
        ImageView memoryImage = view.findViewById(R.id.memoryImage);
        TextView tagText = view.findViewById(R.id.tagText);
        TextView memoryContentText = view.findViewById(R.id.memoryContentText);
        TextView dateText = view.findViewById(R.id.dateText);
        ImageButton backButton = view.findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            getActivity().onBackPressed();
        });
        // Populate the views with data from the TripDetail
        toolbarTitle.setText(tripDetail.getTitle());

        // Getting the accurate location name usually takes long time
        CustomLatLng location = tripDetail.getLocation();
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                String region = address.getLocality();  // City/Region name
                String state = address.getAdminArea();  // State name
                String country = address.getCountryName();  // Country name

                String formattedLocation = "";

                if (region != null && !region.isEmpty()) {
                    formattedLocation += region;
                }

                if (state != null && !state.isEmpty()) {
                    if (!formattedLocation.isEmpty()) formattedLocation += ", ";
                    formattedLocation += state;
                }

                if (country != null && !country.isEmpty()) {
                    if (!formattedLocation.isEmpty()) formattedLocation += ", ";
                    formattedLocation += country;
                }

                locationText.setText(formattedLocation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load image with Glide
        if(tripDetail.getImageUris() != null && !tripDetail.getImageUris().isEmpty()) {
            loadImage(tripDetail.getImageUris().get(currentImageIndex),memoryImage); // Load initial image
        }

        ImageButton leftArrow = view.findViewById(R.id.leftArrow);
        ImageButton rightArrow = view.findViewById(R.id.rightArrow);

        leftArrow.setOnClickListener(v -> {
            if (currentImageIndex > 0) {
                currentImageIndex--;
                loadImage(tripDetail.getImageUris().get(currentImageIndex),memoryImage);
            }
        });

        rightArrow.setOnClickListener(v -> {
            if (currentImageIndex < tripDetail.getImageUris().size() - 1) {
                currentImageIndex++;
                loadImage(tripDetail.getImageUris().get(currentImageIndex),memoryImage);
            }
        });

        tagText.setText(tripDetail.getTag());
        memoryContentText.setText(tripDetail.getTextData());

        Date tripDate = tripDetail.getDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String formattedDate = sdf.format(tripDate);
        dateText.setText(formattedDate);

        return view;
    }
    // Separate function to load the image using Glide
    private void loadImage(String imageUrl,ImageView memoryImage) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.sample_trip_icon) // Placeholder image
                .into(memoryImage);
    }
}


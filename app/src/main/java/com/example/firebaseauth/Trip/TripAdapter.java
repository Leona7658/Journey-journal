package com.example.firebaseauth.Trip;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.firebaseauth.DatabaseModel.TripDB;
import com.example.firebaseauth.DatabaseModel.TripDetail;
import com.example.firebaseauth.Library.FirestoreUtil;
import com.example.firebaseauth.Utility.MainActivity;
import com.example.firebaseauth.R;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    protected static List<TripDB> trips;
    private LayoutInflater inflater;
    @ServerTimestamp
    private Date date;

    public TripAdapter(Context context, List<TripDB> trips) {
        this.trips = trips;
        this.inflater = LayoutInflater.from(context);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.trip_record_in_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TripDB trip = trips.get(position);
        holder.title.setText(trip.getEmail());
        holder.subtitle.setText(new SimpleDateFormat("yyyy-MM-dd").format(trip.getDate()));
        FirestoreUtil firestoreUtil = new FirestoreUtil();
        firestoreUtil.getTripDetailForTrip(trip, new FirestoreUtil.TripDetailCallback() {
            @Override
            public void onTripDetailsLoaded(List<TripDetail> tripDetails) {
                if (tripDetails != null && !tripDetails.isEmpty()) {
                    TripDetail tripDetail = tripDetails.get(0);  // Assuming you want the first TripDetail from the list
                    if (tripDetail.getImageUris() != null && !tripDetail.getImageUris().isEmpty()) {
                        String firstImageUri = tripDetail.getImageUris().get(0);
                        Glide.with(holder.thumbnail.getContext()).load(firstImageUri).into(holder.thumbnail);
                    } else {

                    }
                }
            }
        });
    }




    @Override
    public int getItemCount() {
        return trips.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        ImageView thumbnail;

        ViewHolder(final View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tripTitle);
            subtitle = itemView.findViewById(R.id.tripSubtitle);
            thumbnail = itemView.findViewById(R.id.tripThumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        TripDB trip = TripAdapter.this.trips.get(position);

                        Fragment fragment = new TripDetailFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("selected_trip", trip);
                        fragment.setArguments(bundle);

                        FragmentManager fragmentManager = ((FragmentActivity) v.getContext()).getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                        // Hide bottom navigation bar
                        ((MainActivity) v.getContext()).setBottomNavigationVisibility(View.GONE);
                    }
                }

            });
        }
    }

}
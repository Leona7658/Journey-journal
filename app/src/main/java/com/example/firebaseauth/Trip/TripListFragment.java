package com.example.firebaseauth.Trip;

import static com.example.firebaseauth.Trip.Trip.getEmail;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.firebaseauth.DatabaseModel.TripDB;
import com.example.firebaseauth.R;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TripListFragment extends Fragment {

    private FirebaseFirestore db;
    private List<TripDB> tripList;
    private TripAdapter adapter;
    private RecyclerView tripRecyclerView;
    private DocumentSnapshot lastVisibleDocument;
    private String currentUser = getEmail();


    public TripListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_trip_list, container, false);

        tripRecyclerView = rootView.findViewById(R.id.tripRecyclerView);
        tripList = new ArrayList<>();
        adapter = new TripAdapter(getContext(), tripList);
        tripRecyclerView.setAdapter(adapter);
        tripRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        db = FirebaseFirestore.getInstance();

        loadBatch(null);

        return rootView;
    }

    private void loadBatch(@Nullable DocumentSnapshot startAfter) {
        Query batchQuery = db.collection("Trip")
                .orderBy("date")
                .whereEqualTo("email", currentUser);

        if (startAfter != null) {
            batchQuery = batchQuery.startAfter(startAfter);
        }

        batchQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("FirestoreData", "Error fetching data", e);
                    return;
                }

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    tripList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        TripDB trip = document.toObject(TripDB.class);
                        trip.setTripId(document.getId());
                        String email = trip.getEmail();

                        boolean isTripAlreadyAdded = false;
                        for (TripDB existingTrip : tripList) {
                            if (existingTrip.getTripId().equals(trip.getTripId())) {
                                isTripAlreadyAdded = true;
                                break;
                            }
                        }

                        if (email != null && email.equals(currentUser) && !isTripAlreadyAdded) {
                            tripList.add(trip);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    lastVisibleDocument = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                }

            }
        });


}}

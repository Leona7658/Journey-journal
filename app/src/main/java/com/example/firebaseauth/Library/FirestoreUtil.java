package com.example.firebaseauth.Library;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.firebaseauth.DatabaseModel.TripDB;
import com.example.firebaseauth.DatabaseModel.TripDetail;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;


import java.util.ArrayList;
import java.util.List;

public class FirestoreUtil {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    public interface TripDetailCallback {
        void onTripDetailsLoaded(List<TripDetail> tripDetails);
    }


    public void getTripDetailForTrip(TripDB trip, TripDetailCallback callback) {
        db.collection("TripDetail")
                .whereEqualTo("tripID", trip.getTripId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<TripDetail> tripDetails = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            TripDetail tripDetail = document.toObject(TripDetail.class);
                            tripDetails.add(tripDetail);
                        }
                        callback.onTripDetailsLoaded(tripDetails);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onTripDetailsLoaded(null);
                    }
                });
    }


    public interface TripsCallback {
        void onTripsLoaded(List<TripDB> trips);
    }

    public void getAllTrips(TripsCallback callback) {
        db.collection("Trip")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("FirestoreUtil", "Listen failed.", e);
                            callback.onTripsLoaded(null);
                            return;
                        }

                        List<TripDB> trips = new ArrayList<>();
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                TripDB trip = document.toObject(TripDB.class);
                                if (trip != null) {
                                    trip.setTripId(document.getId());
                                    trips.add(trip);
                                }
                            }
                        }
                        callback.onTripsLoaded(trips);
                    }
                });
    }


}

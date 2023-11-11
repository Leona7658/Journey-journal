package com.example.firebaseauth.DatabaseModel;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class DBFactory {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String tripID;
    private String detailID;

    private TripDetail detail;
    private TripDB tripDB;
    private Context context;
    private String email;

    private AvatarDB avatarDB;
    private TagDB tagDB;

    public DBFactory(TripDB tripDB, Context context, String email) {
        this.tripDB = tripDB;
        this.context = context;
        this.email = email;

    }
    public DBFactory(AvatarDB avatarDB, String email) {
        this.avatarDB = avatarDB;
        this.email = email;
    }

    public DBFactory(TagDB tagDB, String email) {
        this.tagDB = tagDB;
        this.email = email;
    }

    public void addTags() {
        DocumentReference tagRef = db.collection("Tags").document(email);
        tagRef.set(tagDB);

    }

    public void addAvatar() {
        DocumentReference avatarRef = db.collection("Avatar").document(email);
        avatarRef.set(avatarDB);

    }


    public void initTripDB() {
        DocumentReference tripRef = db.collection("Trip").document();

        tripID = tripRef.getId();
        tripRef.set(new TripDB(email));
    }

    public void addDetailDB() {
        db.collection("TripDetail")
                .add(detail)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        detailID = documentReference.getId();
                        Toast.makeText(context, "Post successful", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Post failed", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void updateTripDB() {
        // Get a new write batch
        WriteBatch batch = db.batch();

        // Update the population of 'SF'
        DocumentReference updateRef = db.collection("Trip").document(tripID);
        batch.set(updateRef, tripDB);

        // Commit the batch
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(context, "update successful", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateTripDetail(Uri image) {
        // Update image uris
        DocumentReference updateRef = db.collection("TripDetail").document(detailID);
        updateRef.update("imageUris", FieldValue.arrayUnion(image));
        // Update the timestamp field with the value from the server
        updateRef.update("date", FieldValue.serverTimestamp());

    }


    public TripDetail getDetail() {
        return detail;
    }

    public void setDetail(TripDetail detail) {
        this.detail = detail;
    }

    public String getTripID() {
        return tripID;
    }
    public void setNewTripDB(TripDB trip) {
        this.tripDB = trip;
    }
    public void setAvatarDB(AvatarDB avatarDB) {this.avatarDB = avatarDB;}




}

package com.example.firebaseauth.DatabaseModel;

import com.example.firebaseauth.Library.CustomLatLng;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class TripDB implements Serializable {
    private String email;
    private long speed;
    private long time;
    private List<CustomLatLng> locationList;
    @ServerTimestamp
    private Date date;
    private float distance;

    private String tripID;

    public TripDB() {
        // Required empty public constructor for Firestore
    }

    public TripDB(String email) {
        this.email = email;
    }


    public void setTripDB(float distance, int speed, int time, List<CustomLatLng> locationList) {
        this.distance = distance;
        this.speed = speed;
        this.time = time;
        this.locationList = locationList;

    }
    public String getEmail() {
        return email;
    }

    public float getDistance() {
        return distance;
    }

    public long getSpeed() {
        return speed;
    }

    public List<CustomLatLng> getLocationList() {
        return locationList;
    }

    public long getTime() {
        return time;
    }

    public Date getDate() {return date;}

    public void setDate(Date date) {this.date = date;}


    public String getTripId() {
        return tripID;
    }

    public void setTripId(String tripID) {
        this.tripID = tripID;
    }


}
package com.example.firebaseauth.DatabaseModel;

import com.example.firebaseauth.Library.CustomLatLng;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class TripDetail implements Serializable {
    private String email, title, textData, tag;
    private List<String> imageUris;
    private CustomLatLng location;
    @ServerTimestamp
    private Date date;
    private String tripID;


    public TripDetail() {}
    public TripDetail(String email, String title, String text,
                      String tag, CustomLatLng location, String ID) {
        this.email = email;
        this.title = title;
        this.textData = text;
        this.tag = tag;
        this.location = location;
        this.tripID = ID;

    }

    public void setImageUris(List<String> imageUris) {
        this.imageUris = imageUris;
    }

    public String getTitle() {
        return title;
    }

    public String getTag() {
        return tag;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getImageUris() {
        return imageUris;
    }

    public CustomLatLng getLocation() {
        return location;
    }

    public String getTextData() {
        return textData;
    }

    public String getTripID() { return tripID;}

    public Date getDate() {return date;}

    public void setDate(Date date) {this.date = date;}


    public String getImageUri() {
        if (imageUris != null){
            return imageUris.toString();
        }
        return null;
    }

}

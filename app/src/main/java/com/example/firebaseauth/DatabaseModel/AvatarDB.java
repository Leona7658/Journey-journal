package com.example.firebaseauth.DatabaseModel;

import android.net.Uri;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class AvatarDB {
    @ServerTimestamp
    private Date date;

    private String email;

    private Uri avatar;


    public AvatarDB(String email) {
        this.email = email;
    }

    public String getEmail() {return email;}

    public Uri getAvatar() {return avatar;}

    public Date getDate() {return date;}

    public void setAvatar(Uri avatar) {
        this.avatar = avatar;
    }




}

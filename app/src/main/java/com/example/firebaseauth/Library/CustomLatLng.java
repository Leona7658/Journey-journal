package com.example.firebaseauth.Library;

import com.google.android.gms.maps.model.LatLng;

public class CustomLatLng {
    public double latitude;
    public double longitude;

    private double stability;

    public CustomLatLng() {}

    public CustomLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public static CustomLatLng fromLatLng(LatLng latLng) {
        return new CustomLatLng(latLng.latitude, latLng.longitude);
    }

    public double getStability() {
        return stability;
    }

    public void setStability(double stability) {
        this.stability = stability;
    }
}

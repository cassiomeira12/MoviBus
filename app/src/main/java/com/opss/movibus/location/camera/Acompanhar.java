package com.opss.movibus.location.camera;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class Acompanhar implements Camera {

    private GoogleMap mMap;

    public Acompanhar(GoogleMap mMap) {
        this.mMap = mMap;
    }

    @Override
    public void camera(LatLng latLng) {
        this.mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

}

package com.opss.movibus.ui.fragment.marker;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerObjetct {
    protected Marker marker;//Marker para adicionar no GoogleMaps
    protected MarkerOptions markerOptions;//Opcoes de criacao do Marker

    public MarkerObjetct() {

    }

    public MarkerObjetct(int bitMapSource) {
        this.markerOptions = new MarkerOptions();
        this.markerOptions.flat(false);//Permite que o Marker rotacione junto com o Google Maps
        //this.markerOptions.draggable(true);
        this.markerOptions.icon(BitmapDescriptorFactory.fromResource(bitMapSource));//Source da Imagem
    }

    public Marker getMarker(GoogleMap mMap) {
        if (marker == null) {
            marker = mMap.addMarker(markerOptions);
        }
        return marker;
    }

    public Marker getMarker() {
        return marker;
    }

}

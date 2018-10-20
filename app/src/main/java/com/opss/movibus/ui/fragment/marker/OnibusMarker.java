package com.opss.movibus.ui.fragment.marker;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

import com.opss.movibus.R;
import com.opss.movibus.location.camera.Camera;
import com.opss.movibus.location.camera.NaoAcompanhar;
import com.opss.movibus.model.Onibus;

public class OnibusMarker extends MarkerObjetct implements EventListener<DocumentSnapshot> {

    private DocumentReference documentReference;
    private Onibus onibus;//Referencia ao objeto Ponto de Onibus

    private Camera camera = new NaoAcompanhar();

    public OnibusMarker() {
        super(R.mipmap.ic_onibus);
        this.onibus = null;
    }

    public OnibusMarker(Onibus onibus) {
        super(R.mipmap.ic_onibus);
        LatLng posicao = new LatLng(onibus.getLatitude(), onibus.getLongitude());
        super.markerOptions.position(posicao);
        //super.markerOptions.title("");
        this.onibus = onibus;
    }

    @Override
    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
        if (e != null) {
            return;
        }

        if (documentSnapshot != null && documentSnapshot.exists()) {
            double lat = documentSnapshot.getDouble("latitude");
            double lon = documentSnapshot.getDouble("longitude");

            this.marker.setPosition(new LatLng(lat, lon));
            this.camera.camera(new LatLng(lat, lon));

        } else {
            marker.remove();//Remove o Marker do Google Maps
        }
    }

    public void setDocumentReference(DocumentReference document) {
        this.documentReference = document;
        this.documentReference.addSnapshotListener(this);
    }

    public void setTitulo(String titulo) {
        super.marker.setTitle(titulo);
    }

    public void setPosicao(double latitude, double longitude) {
        if (documentReference == null) {
            return;
        }
        //this.documentReference.update("latitude", latitude);
        //this.documentReference.update("longitude", longitude);
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Onibus getOnibus() {
        return onibus;
    }
}

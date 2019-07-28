package com.opss.movibus.ui.fragment.marker;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

import com.opss.movibus.R;
import com.opss.movibus.location.camera.Camera;
import com.opss.movibus.location.camera.NaoAcompanhar;
import com.opss.movibus.model.Onibus;

public class OnibusMarker extends MarkerObjetct implements EventListener<DocumentSnapshot>, ValueEventListener {

    private DocumentReference documentReference;//Firestore Database
    private DatabaseReference documentReference2;//Realtime Database
    private Onibus onibus;//Referencia ao objeto Ponto de Onibus

    private Camera camera = new NaoAcompanhar();

    public OnibusMarker() {
        super(R.mipmap.ic_onibus);
        this.onibus = null;
    }

    public OnibusMarker(Onibus onibus) {
        super(R.mipmap.ic_onibus);
        //LatLng posicao = new LatLng(onibus.getLatitude(), onibus.getLongitude());
        super.markerOptions.position(new LatLng(0,0));
        this.onibus = onibus;
        if (onibus.getLinha() != null) {
            super.markerOptions.title(onibus.getLinha().getNome());
        }

        documentReference2 = FirebaseDatabase.getInstance().getReference("onibus").child(onibus.getId());
        documentReference2.addValueEventListener(this);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        double lat = dataSnapshot.child("latitude").getValue(Double.class);
        double lon = dataSnapshot.child("longitude").getValue(Double.class);

        LatLng localizacao = new LatLng(lat, lon);

        this.marker.setPosition(localizacao);
        this.camera.camera(localizacao);
        this.onibus.setPosicao(lat, lon);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

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

    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Onibus getOnibus() {
        return onibus;
    }

}

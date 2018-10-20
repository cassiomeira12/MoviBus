package com.opss.movibus.model;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;

import com.opss.movibus.firebase.Firebase;
import com.opss.movibus.ui.fragment.marker.OnibusMarker;

import java.io.Serializable;

public class Onibus implements Serializable {
    @Exclude
    public static final String COLECAO = "onibus";

    private String id;//Id deste Onibus
    private String idLinha;//Id da Linha do onibus
    private double latitude;//Latitude atual do onibus
    private double longitude;//Longitude atual do onibus
    private boolean elevador;//Se o onibus tem elevador ou nao

    @Exclude
    private Linha linha;
    @Exclude
    private OnibusMarker marker;
    @Exclude
    private boolean acompanhando = false;//Variavel controla quando o onibus esta sendo acompanhado
    @Exclude
    public PontoOnibus pontoOnibus;//Referencia a um ponto de onibus pra calcular a distancia

    public Onibus() {
        this.id = "";
        this.idLinha = "";
        this.latitude = 0;
        this.longitude = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdLinha() {
        return idLinha;
    }

    public void setIdLinha(String idLinha) {
        this.idLinha = idLinha;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Exclude
    public boolean isElevador() {
        return elevador;
    }

    @Exclude
    public void setElevador(boolean elevador) {
        this.elevador = elevador;
    }

    public void setPosicao(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        if (marker != null) {
            marker.setPosicao(latitude, longitude);
        }
    }

    public void atualizarLinha() {
        Firebase.get().getFireLinha().getCollection().document(idLinha).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                linha = documentSnapshot.toObject(Linha.class);
                if (marker!= null) {
                    marker.setTitulo(linha.getNome());
                    //marker.getMarker().setSnippet(linha.getVia());
                }
            }
        });
    }

    @Exclude
    public void setLinha(Linha linha) {
        this.linha = linha;
    }

    @Exclude
    public Linha getLinha() {
        return linha;
    }

    @Exclude
    public OnibusMarker getMarker() {
        return marker;
    }

    @Exclude
    public void setMarker(OnibusMarker marker) {
        this.marker = marker;
    }

    @Exclude
    public boolean getAcompanhando() {
        return acompanhando;
    }

    @Exclude
    public void setAcompanhando(boolean acompanhando) {
        this.acompanhando = acompanhando;
    }
}

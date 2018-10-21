package com.opss.movibus.model;


import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.opss.movibus.ui.fragment.marker.PontoMarker;

public class PontoOnibus implements Serializable {
    @Exclude
    public static final String COLECAO = "pontosOnibus";

    private String id;
    private String descricao;
    private boolean coberto;
    private double latitude;
    private double longitude;

    @Exclude
    private PontoMarker marker;

    public PontoOnibus() {
        this.id = "";
        this.descricao = "";
        this.coberto = false;
        this.latitude = 0;
        this.longitude = 0;
    }

    public PontoOnibus(String id, String descricao, boolean coberto, double latitude, double longitude, PontoMarker marker) {
        this.id = id;
        this.descricao = descricao;
        this.coberto = coberto;
        this.latitude = latitude;
        this.longitude = longitude;
        this.marker = marker;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isCoberto() {
        return coberto;
    }

    public void setCoberto(boolean coberto) {
        this.coberto = coberto;
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
    public void setPosicao(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Exclude
    public PontoMarker getMarker() {
        return marker;
    }

    @Exclude
    public void setMarker(PontoMarker marker) {
        this.marker = marker;
    }

    @Exclude
    public Map<String, Object> getUpdate() {
        Map<String, Object> update = new HashMap<>();

        update.put("id", id);
        update.put("descricao", descricao);
        update.put("coberto", coberto);
        update.put("latitude", latitude);
        update.put("longitude", longitude);

        return update;
    }

    @Override
    public String toString() {
        return descricao;
    }
}

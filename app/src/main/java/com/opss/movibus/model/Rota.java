package com.opss.movibus.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rota implements Serializable {
    @Exclude
    public static final String COLECAO = "rotas";

    private String id;
    private List<GeoPoint> coordenadas;
    private Map<String, PontoOnibus> pontosOnibus;

    public Rota() {
        this.id = "";
        this.coordenadas = new ArrayList<>();
        this.pontosOnibus = new HashMap<>();
    }

    public Rota(String id, List<GeoPoint> coordenadas, Map<String, PontoOnibus> pontosOnibus) {
        this.id = id;
        this.coordenadas = coordenadas;
        this.pontosOnibus = pontosOnibus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<GeoPoint> getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(List<GeoPoint> coordenadas) {
        this.coordenadas = coordenadas;
    }

    public Map<String, PontoOnibus> getPontosOnibus() {
        return pontosOnibus;
    }

    public void setPontosOnibus(Map<String, PontoOnibus> pontosOnibus) {
        this.pontosOnibus = pontosOnibus;
    }

    public void addPonto(PontoOnibus ponto) {
        this.pontosOnibus.put(ponto.getId(), ponto);
    }

    public void addAllPonto(PontoOnibus... pontos) {
        for (PontoOnibus ponto : pontos) {
            this.pontosOnibus.put(ponto.getId(), ponto);
        }
    }

    public PontoOnibus getPonto(String key) {
        return this.pontosOnibus.get(key);
    }
}

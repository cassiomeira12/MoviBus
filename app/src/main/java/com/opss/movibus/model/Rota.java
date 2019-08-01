package com.opss.movibus.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Rota implements Serializable {
    @Exclude
    public static final String COLECAO = "rotas";
    @Exclude
    public static final String COORDENADAS = "coordenadas";
    @Exclude
    public static final String PONTOS_ONIBUS = "pontosOnibus";

    private String id, origem, destino;

    @Exclude
    private List<Coordenada> coordenadas;

    public Rota() {
        this.coordenadas = new ArrayList<>();
    }

    public Rota(String id) {
        this.id = id;
        this.coordenadas = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public List<Coordenada> getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(List<Coordenada> coordenadas) {
        this.coordenadas = coordenadas;
    }
}

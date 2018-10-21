package com.opss.movibus.model;


import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Linha implements Serializable {
    @Exclude
    public static final String COLECAO = "linhas";

    private String id;
    private String nome;
    private String origem;
    private String destino;
    private String via;
    private String idRotaIda;
    private String idRotaVolta;

    @Exclude
    private Rota rotaIDA;
    @Exclude
    private Rota rotaVolta;
    @Exclude
    private Map<String, Onibus> onibusMap = new HashMap<>();

    public Linha() {
        this.id = "";
        this.nome = "";
        this.origem = "";
        this.destino = "";
        this.via = "";
        this.idRotaIda = "";
        this.idRotaVolta = "";
    }

    public Linha(String id, String nome, String origem, String destino, String via, String idRotaIda, String idRotaVolta) {
        this.id = id;
        this.nome = nome;
        this.origem = origem;
        this.destino = destino;
        this.via = via;
        this.idRotaIda = idRotaIda;
        this.idRotaVolta = idRotaVolta;
        //this.rotaIDA = rotaIDA;
        //this.rotaVolta = rotaVolta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public String getVia() {
        return via.isEmpty() ? "" : " Via " + via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getIdRotaIda() {
        return idRotaIda;
    }

    public void setIdRotaIda(String idRotaIda) {
        this.idRotaIda = idRotaIda;
    }

    public String getIdRotaVolta() {
        return idRotaVolta;
    }

    public void setIdRotaVolta(String idRotaVolta) {
        this.idRotaVolta = idRotaVolta;
    }

    @Exclude
    public void setRotaIDA(Rota rota) {
        this.rotaIDA = rota;
    }

    @Exclude
    public Rota getRotaIDA() {
        return rotaIDA;
    }

    @Exclude
    public void setRotaVolta(Rota rota) {
        this.rotaVolta = rota;
    }

    @Exclude
    public Rota getRotaVolta() {
        return rotaVolta;
    }

    @Exclude
    public void setOnibus(Onibus onibus) {
        onibusMap.put(onibus.getId(), onibus);
    }

    @Exclude
    public Onibus getOnibus(String idOnibus) {
        return onibusMap.get(idOnibus);
    }

    @Exclude
    public Map<String, Onibus> getOnibusMap() {
        return onibusMap;
    }

    @Exclude
    public void setOnibusVisible(boolean visible) {
        for (Onibus onibus : onibusMap.values()) {
            onibus.getMarker().getMarker().setVisible(visible);
        }
    }

    @Override
    public String toString() {
        return nome + "\nORIGEM: " + origem + "\nDESTINO: " + destino + "VIA: " + via;
    }
}

package com.opss.movibus.model;


import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class PontoFavorito implements Serializable {
    @Exclude
    public static final String COLECAO = "pontosFavoritos";

    private String id;
    private String idPonto;
    private String descricao;
    private boolean coberto;

    public PontoFavorito() {
        this.id = "";
        this.idPonto = "";
        this.descricao = "";
        this.coberto = false;
    }

    public PontoFavorito(PontoOnibus pontoOnibus) {
        this();
        this.idPonto = pontoOnibus.getId();
        this.coberto = pontoOnibus.isCoberto();
        this.descricao = pontoOnibus.getDescricao();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdPonto() {
        return idPonto;
    }

    public void setIdPonto(String idPonto) {
        this.idPonto = idPonto;
    }

    public boolean isCoberto() {
        return coberto;
    }

    public void setCoberto(boolean coberto) {
        this.coberto = coberto;
    }

    @Exclude
    public String getCoberto() {
        return coberto ? "Coberto" : "";
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}

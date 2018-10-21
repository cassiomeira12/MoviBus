package com.opss.movibus.model;


import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class LinhaFavorita implements Serializable {
    @Exclude
    public static final String COLECAO = "linhasFavoritas";

    private String id;
    private String idLinha;
    private String nome;
    private String via;
    private String origem;
    private String destino;

    @Exclude
    public Linha linha;

    @Exclude
    private int onibusOnline = 0;

    public LinhaFavorita() {
        this.id = "";
        this.idLinha = "";
        this.nome = "";
        this.via = "";
        this.origem = "";
        this.destino = "";
        this.onibusOnline = 0;
    }

    public LinhaFavorita(Linha linha) {
        this.linha = linha;
        this.idLinha = linha.getId();
        this.nome = linha.getNome();
        this.via = linha.getVia();
        this.origem = linha.getOrigem();
        this.destino = linha.getDestino();
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
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

    @Exclude
    public String getIdentificador() {
        return nome + " " + via;
    }

    @Exclude
    public int getOnibusOnline() {
        return onibusOnline;
    }

    @Exclude
    public void setOnibusOnline(int onibusOnline) {
        this.onibusOnline = onibusOnline;
    }

    @Exclude
    public void addOnibusOnline() {
        this.onibusOnline++;
    }
}

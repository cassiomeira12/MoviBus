package com.opss.movibus.model;

import java.io.Serializable;

public class Favorito implements Serializable {

    private String descricao;

    private LinhaFavorita linha = null;
    private String origem;
    private String destino;
    private int onibusOnline;

    private PontoFavorito ponto = null;
    private String coberto;

    public Favorito() {
        this.descricao = "";
        this.origem = "";
        this.destino = "";
        this.onibusOnline = 0;
        this.coberto = "";
    }

    public Favorito(LinhaFavorita linha) {
        this();
        this.linha = linha;
        this.descricao = linha.getNome() + " " + linha.getVia();
        this.origem = linha.getOrigem();
        this.destino = linha.getDestino();
        this.onibusOnline = linha.getOnibusOnline();
    }

    public Favorito(PontoFavorito ponto) {
        this();
        this.ponto = ponto;
        this.descricao = ponto.getDescricao();
        this.coberto = ponto.isCoberto() ? "Coberto" : "";
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LinhaFavorita getLinha() {
        return linha;
    }

    public void setLinha(LinhaFavorita linha) {
        this.linha = linha;
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

    public int getOnibusOnline() {
        return onibusOnline;
    }

    public void setOnibusOnline(int onibusOnline) {
        this.onibusOnline = onibusOnline;
    }

    public PontoFavorito getPonto() {
        return ponto;
    }

    public void setPonto(PontoFavorito ponto) {
        this.ponto = ponto;
    }

    public String getCoberto() {
        return coberto;
    }

    public void setCoberto(String coberto) {
        this.coberto = coberto;
    }
}

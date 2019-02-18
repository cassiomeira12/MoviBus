package com.opss.movibus.model;

import java.io.Serializable;

public class Coordenada implements Serializable {
    public int numero;
    public double latitude;
    public double longitude;
    public String endereco;

    public Coordenada() {
        this.numero = 0;
        this.latitude = 0;
        this.longitude = 0;
    }

    public Coordenada(int numero, double latitude, double longitude) {
        this.numero = numero;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "[" + numero + "] " + latitude  + " " + longitude;
    }

}
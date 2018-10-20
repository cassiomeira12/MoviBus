package com.opss.movibus.ui.fragment.marker;

import com.google.android.gms.maps.model.LatLng;

import com.opss.movibus.R;
import com.opss.movibus.model.PontoOnibus;

public class PontoMarker extends MarkerObjetct {
    private PontoOnibus ponto;//Referencia ao objeto Ponto de Onibus

    public PontoMarker() {
        super(R.mipmap.ic_ponto);
        this.ponto = null;
    }

    public PontoMarker(PontoOnibus ponto) {
        super(R.mipmap.ic_ponto);
        LatLng posicao = new LatLng(ponto.getLatitude(), ponto.getLongitude());
        super.markerOptions.position(posicao);
        super.markerOptions.title("Ponto de Ônibus");
        this.ponto = ponto;
    }

    public PontoOnibus getPonto() {
        return ponto;
    }

}

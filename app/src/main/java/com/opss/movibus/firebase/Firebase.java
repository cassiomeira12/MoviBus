package com.opss.movibus.firebase;

import com.opss.movibus.firebase.fire.*;
import com.opss.movibus.model.*;

public class Firebase {

    private static Firebase instance = new Firebase();

    private FireUsuario fireUsuario = new FireUsuario(Usuario.COLECAO);
    private FireRota fireRota = new FireRota(Rota.COLECAO);
    private FireLinha fireLinha = new FireLinha(Linha.COLECAO);
    private FireOnibus fireOnibus = new FireOnibus(Onibus.COLECAO);
    private FirePontoOnibus firePontoOnibus = new FirePontoOnibus(PontoOnibus.COLECAO);


    public static Firebase get() {
        return instance;
    }

    public FireUsuario getFireUsuario() {
        return fireUsuario;
    }

    public FireRota getFireRota() {
        return fireRota;
    }

    public FireLinha getFireLinha() {
        return fireLinha;
    }

    public FireOnibus getFireOnibus() {
        return fireOnibus;
    }

    public FirePontoOnibus getFirePontoOnibus() {
        return firePontoOnibus;
    }
}

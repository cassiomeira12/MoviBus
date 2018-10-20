package com.opss.movibus.firebase.fire;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.opss.movibus.firebase.IFirebase;
import com.opss.movibus.model.PontoOnibus;

public class FirePontoOnibus implements IFirebase<PontoOnibus> {

    private CollectionReference collection;

    public FirePontoOnibus(String colecao) {
        this.collection = FirebaseFirestore.getInstance().collection(colecao);
    }

    @Override
    public CollectionReference getCollection() {
        return collection;
    }

    @Override
    public String generateKey() {
        return collection.document().getId();
    }

    @Override
    public void setDocument(PontoOnibus pontoOnibus) {
        this.collection.document(pontoOnibus.getId()).set(pontoOnibus);
    }

    @Override
    public void updateDocument(PontoOnibus pontoOnibus) {
        this.collection.document(pontoOnibus.getId()).update(pontoOnibus.getUpdate());
    }
}

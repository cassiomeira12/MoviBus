package com.opss.movibus.firebase.fire;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.opss.movibus.firebase.IFirebase;
import com.opss.movibus.model.Rota;

public class FireRota implements IFirebase<Rota> {

    private CollectionReference collection;

    public FireRota(String colecao) {
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
    public void setDocument(Rota rota) {
        this.collection.document(rota.getId()).set(rota);
    }

    @Override
    public void updateDocument(Rota document) {

    }
}

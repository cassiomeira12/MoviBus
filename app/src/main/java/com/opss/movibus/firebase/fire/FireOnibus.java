package com.opss.movibus.firebase.fire;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.opss.movibus.firebase.IFirebase;
import com.opss.movibus.model.Onibus;

public class FireOnibus implements IFirebase<Onibus> {

    private CollectionReference collection;

    public FireOnibus(String colecao) {
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
    public void setDocument(Onibus onibus) {
        this.collection.document(onibus.getId()).set(onibus);
    }

    @Override
    public void updateDocument(Onibus document) {

    }
}

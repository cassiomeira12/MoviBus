package com.opss.movibus.firebase.fire;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.opss.movibus.firebase.IFirebase;
import com.opss.movibus.model.Linha;

public class FireLinha implements IFirebase<Linha> {

    private CollectionReference collection;

    public FireLinha(String colecao) {
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
    public void setDocument(Linha linha) {
        this.collection.document(linha.getId()).set(linha);
    }

    @Override
    public void updateDocument(Linha document) {

    }

}

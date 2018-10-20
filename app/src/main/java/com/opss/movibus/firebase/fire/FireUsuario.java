package com.opss.movibus.firebase.fire;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.opss.movibus.firebase.IFirebase;
import com.opss.movibus.model.Usuario;

public class FireUsuario implements IFirebase<Usuario> {

    private CollectionReference collection;

    public FireUsuario(String colecao) {
        this.collection = FirebaseFirestore.getInstance().collection(colecao);
    }

    public DocumentReference getUserDocument() {
        return collection.document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    public CollectionReference getCollection() {
        return collection;
    }

    @Override
    public String generateKey() {
        return null;
    }

    @Override
    public void setDocument(Usuario document) {
        this.collection.document(document.getId()).set(document);
    }

    @Override
    public void updateDocument(Usuario document) {

    }

}

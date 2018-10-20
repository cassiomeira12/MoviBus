package com.opss.movibus.firebase;

import com.google.firebase.firestore.CollectionReference;

/**
 * Created by cassio on 05/10/18.
 */

public interface IFirebase<T> {

    public CollectionReference getCollection();
    public String generateKey();
    public void setDocument(T document);
    public void updateDocument(T document);

}

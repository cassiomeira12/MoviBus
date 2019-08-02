package com.opss.movibus.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Usuario implements Serializable {
    @Exclude
    public static final String COLECAO = "usuarios";

    private String id;
    private String nome;
    private String telefone;
    //Utilizando o Firebase não precisa armazenar Email e Senha
    //Porque eles ficam armazenados no Authentication
    private String email;
    //private String senha;

    private List<Favorito> favoritoList;

    public Usuario() {
        //Construtor obrigatorio para o Firebase
    }

    public Usuario(String id, String nome, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.favoritoList = new ArrayList<>();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String toString() {
        return "\nNome " + nome +
                "\nEmail " + email +
                "\nTelefone " + telefone;
    }
}

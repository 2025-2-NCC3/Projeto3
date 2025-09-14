 package com.example.pi.models;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    private int id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("email")
    private String email;

    @SerializedName("senha")
    private String senha;

    // Construtor para criar um novo usuário para envio (sem o ID)
    public User(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }
}
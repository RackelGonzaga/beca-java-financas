package com.beca.ms1_users.domain;

import java.util.UUID;

public class User {

    private UUID id;
    private String nome;
    private String email;
    private String senha;

    public User(UUID id, String nome, String email, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public User(String nome, String email, String senha) {
        this(null, nome, email, senha);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public void atualizar(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }
}
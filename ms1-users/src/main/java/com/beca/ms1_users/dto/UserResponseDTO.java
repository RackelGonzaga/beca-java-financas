package com.beca.ms1_users.dto;

import java.util.UUID;

public class UserResponseDTO {

    private UUID id;
    private String nome;    
    private String email;

    public UserResponseDTO(UUID id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
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

}
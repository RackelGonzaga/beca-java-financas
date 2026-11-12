package com.beca.ms1_users.dto;

public record AuthenticateUserRequestDTO(
    String email,
    String senha
) {
    
}
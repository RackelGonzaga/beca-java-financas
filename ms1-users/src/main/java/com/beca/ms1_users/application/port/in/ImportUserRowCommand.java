package com.beca.ms1_users.application.port.in;

public record ImportUserRowCommand(
        int numeroLinha,
        String nome,
        String email,
        String senha
) {
}
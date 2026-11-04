package com.beca.ms1_users.application.port.in;

public record RegisterUserCommand(
    String nome,
    String email,
    String senha
) {}
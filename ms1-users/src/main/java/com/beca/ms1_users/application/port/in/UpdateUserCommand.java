package com.beca.ms1_users.application.port.in;

public record UpdateUserCommand(
    String nome,
    String email
) {}
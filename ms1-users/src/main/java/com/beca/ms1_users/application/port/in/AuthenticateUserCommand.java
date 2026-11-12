package com.beca.ms1_users.application.port.in;

public record AuthenticateUserCommand(
    String email,
    String senha
) {
    
}

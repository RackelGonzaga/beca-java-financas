package com.beca.ms1_users.application.port.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserCommand(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String senha

) {}
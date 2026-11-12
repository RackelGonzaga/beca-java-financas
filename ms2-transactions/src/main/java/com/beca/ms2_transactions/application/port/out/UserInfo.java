package com.beca.ms2_transactions.application.port.out;

import java.util.UUID;

public record UserInfo(
    UUID id,
    String nome,
    String email
) {
    
}
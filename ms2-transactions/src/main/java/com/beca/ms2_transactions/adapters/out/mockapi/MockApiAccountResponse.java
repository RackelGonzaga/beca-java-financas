package com.beca.ms2_transactions.adapters.out.mockapi;

import java.math.BigDecimal;

public record MockApiAccountResponse(
        String id,
        String userId,
        BigDecimal saldo,
        BigDecimal limiteDiario,
        BigDecimal limitePorTransacao,
        String email
) {
}
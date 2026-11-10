package com.beca.ms2_transactions.application.port.in;

import java.util.Optional;
import java.util.UUID;

import java.math.BigDecimal;

import com.beca.ms2_transactions.domain.Transaction;

public interface FindTransactionUseCase {
    
    Optional<Transaction> findTransactionById(UUID id);

    BigDecimal getBalance(UUID userId);


}

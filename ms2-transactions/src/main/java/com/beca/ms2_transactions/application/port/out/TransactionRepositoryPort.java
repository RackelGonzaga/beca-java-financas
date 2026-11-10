package com.beca.ms2_transactions.application.port.out;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import com.beca.ms2_transactions.domain.Transaction;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);
    
    List<Transaction> findByUserId(UUID userId);

    Optional<Transaction> findById(UUID id);

    void deleteById(UUID id);
    
}
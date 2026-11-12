package com.beca.ms2_transactions.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionType;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    List<Transaction> findByFilters(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type
    );

    BigDecimal sumApprovedOutflowsByUserAndDate(
            UUID userId,
            LocalDate date
    );

    Optional<Transaction> findById(UUID id);

    void lockUserTransactions(UUID userId);
}
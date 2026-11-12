package com.beca.ms2_transactions.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.beca.ms2_transactions.domain.AccountBalance;
import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionType;

public interface FindTransactionUseCase {

    Optional<Transaction> findTransactionById(UUID id);

    Optional<AccountBalance> getBalance(UUID userId);

    List<Transaction> findTransactions(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type
    );
}
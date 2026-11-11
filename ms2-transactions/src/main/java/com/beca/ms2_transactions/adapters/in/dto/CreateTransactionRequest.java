package com.beca.ms2_transactions.adapters.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionType;

public record CreateTransactionRequest (

    @NotNull
    UUID userId,

    @NotNull
    TransactionType type,

    @NotBlank
    String description,

    @NotNull
    @Positive
    BigDecimal amount,

    @NotNull
    LocalDate date,

    String origin,

    UUID destination

) {
    public Transaction toDomain() {
        return new Transaction(
            userId,
            type,
            description,
            amount,
            date,
            origin,
            destination

        );
    }
}
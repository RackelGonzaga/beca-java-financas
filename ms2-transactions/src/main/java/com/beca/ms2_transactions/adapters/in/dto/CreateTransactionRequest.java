package com.beca.ms2_transactions.adapters.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionType;
import com.beca.ms2_transactions.domain.Category;

public record CreateTransactionRequest(
        @NotNull
        TransactionType type,

        @NotNull
        Category category,

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
    public Transaction toDomain(UUID userId) {
        return new Transaction(
                userId,
                type,
                category,
                description,
                amount,
                date,
                origin,
                destination
        );
    }
}
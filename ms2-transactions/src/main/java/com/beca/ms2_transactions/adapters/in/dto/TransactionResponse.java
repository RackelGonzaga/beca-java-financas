package com.beca.ms2_transactions.adapters.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionStatus;
import com.beca.ms2_transactions.domain.TransactionType;
import com.beca.ms2_transactions.domain.Category;

public record TransactionResponse(
        UUID id,
        UUID userId,
        TransactionType type,
        Category category,
        TransactionStatus status,
        String rejectionReason,
        String description,
        BigDecimal amount,
        LocalDate date,
        String origin,
        UUID destination,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getStatus(),
                transaction.getRejectionReason(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getOrigin(),
                transaction.getDestination(),
                transaction.getCreatedAt()
        );
    }
}
package com.beca.ms2_transactions.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private UUID id;
    private UUID userId;
    private TransactionType type;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String origin;
    private String destination;
    private LocalDateTime createdAt;

    // Construtor para criar uma nova transação
    public Transaction(UUID userId, TransactionType type,
            String description, BigDecimal amount, LocalDate date,
            String origin, String destination) {

        validarValor(amount);

        this.userId = userId;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.origin = origin;
        this.destination = destination;
    }

    // Construtor para reconstruir uma transação existente
    public Transaction(UUID id, UUID userId, TransactionType type,
            String description, BigDecimal amount, LocalDate date,
            String origin, String destination, LocalDateTime createdAt) {

        this(userId, type, description, amount, date, origin, destination);

        this.id = id;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public TransactionType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void update(TransactionType type, String description,
            BigDecimal amount, LocalDate date, String origin,
            String destination) {

        validarValor(amount);

        this.type = type;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.origin = origin;
        this.destination = destination;
    }

    private void validarValor(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "O valor da transação deve ser maior que zero.");
        }
    }
}
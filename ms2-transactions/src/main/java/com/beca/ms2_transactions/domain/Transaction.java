package com.beca.ms2_transactions.domain;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private UUID id;
    private UUID userId;
    private TransactionType type;
    private TransactionStatus status;
    private String rejectionReason;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String origin;
    private UUID destination;
    private LocalDateTime createdAt;

    // Construtor para criar uma nova transação aprovada
    public Transaction(UUID userId, TransactionType type,
            String description, BigDecimal amount, LocalDate date,
            String origin, UUID destination) {

        this(userId, type, description, amount, date,
                origin, destination, TransactionStatus.APPROVED, null);
    }

    // Construtor para criar uma transação com status definido
    public Transaction(UUID userId, TransactionType type,
            String description, BigDecimal amount, LocalDate date,
            String origin, UUID destination,
            TransactionStatus status, String rejectionReason) {

        validarStatus(status, rejectionReason);

        if (amount == null) {
            throw new IllegalArgumentException(
                    "O valor da transação é obrigatório.");
        }

        if (status == TransactionStatus.APPROVED) {
            validarValor(amount);
            validarTransferencia(userId, type, destination);
        }

        this.userId = userId;
        this.type = type;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.origin = origin;
        this.destination = destination;
    }

    // Construtor para reconstruir uma transação existente
    public Transaction(UUID id, UUID userId, TransactionType type,
            String description, BigDecimal amount, LocalDate date,
            String origin, UUID destination, LocalDateTime createdAt,
            TransactionStatus status, String rejectionReason) {

        this(userId, type, description, amount, date,
                origin, destination, status, rejectionReason);

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

    public TransactionStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
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

    public UUID getDestination() {
        return destination;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private void validarValor(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O valor da transação deve ser maior que zero.");
        }
    }

    private void validarStatus(TransactionStatus status,
            String rejectionReason) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "O status da transação é obrigatório.");
        }

        if (status == TransactionStatus.REJECTED
                && (rejectionReason == null || rejectionReason.isBlank())) {
            throw new IllegalArgumentException(
                    "O motivo da rejeição é obrigatório.");
        }

        if (status == TransactionStatus.APPROVED
                && rejectionReason != null) {
            throw new IllegalArgumentException(
                    "Uma transação aprovada não pode ter motivo de rejeição.");
        }
    }

    private void validarTransferencia(UUID userId, TransactionType type,
            UUID destination) {

        if (type == TransactionType.TRANSFER && destination == null) {
            throw new IllegalArgumentException(
                    "O usuário de destino é obrigatório para transferências.");
        }
    }
}

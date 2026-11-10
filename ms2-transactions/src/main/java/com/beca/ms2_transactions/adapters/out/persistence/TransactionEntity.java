package com.beca.ms2_transactions.adapters.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionType;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String origin;
    private String destination;

    protected TransactionEntity() {}

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public TransactionEntity(
            UUID userId,
            TransactionType type,
            String description,
            BigDecimal amount,
            LocalDate date,
            String origin,
            String destination) {

        this.userId = userId;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.origin = origin;
        this.destination = destination;
    }

    public void update(Transaction transaction) {
        this.type = transaction.getType();
        this.description = transaction.getDescription();
        this.amount = transaction.getAmount();
        this.date = transaction.getDate();
        this.origin = transaction.getOrigin();
        this.destination = transaction.getDestination();
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
}
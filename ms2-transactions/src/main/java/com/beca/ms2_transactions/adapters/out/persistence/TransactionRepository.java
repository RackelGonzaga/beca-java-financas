package com.beca.ms2_transactions.adapters.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository
        extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findByUserId(UUID userId);

    List<TransactionEntity> findByDestination(UUID destination);
}
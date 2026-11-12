package com.beca.ms2_transactions.adapters.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.beca.ms2_transactions.domain.TransactionStatus;
import com.beca.ms2_transactions.domain.TransactionType;

public interface TransactionRepository
        extends JpaRepository<TransactionEntity, UUID> {

    @Query("""
        SELECT t FROM TransactionEntity t
        WHERE t.userId = :userId
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
          AND (:type IS NULL OR t.type = :type)
        ORDER BY t.date DESC, t.createdAt DESC
        """)
    List<TransactionEntity> findByFilters(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") TransactionType type
    );

    @Query("""
        SELECT SUM(t.amount)
        FROM TransactionEntity t
        WHERE t.userId = :userId
          AND t.date = :date
          AND t.status = :status
          AND t.type IN :types
        """)
    BigDecimal sumApprovedOutflowsByUserAndDate(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date,
            @Param("status") TransactionStatus status,
            @Param("types") List<TransactionType> types
    );
}
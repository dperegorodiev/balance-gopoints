package org.example.balance.repository;

import org.example.balance.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Метод для поиска всех транзакций по счету
    List<Transaction> findByAccountId(UUID accountId);

    // Метод для поиска транзакций по счету за определенный период
    List<Transaction> findByAccountIdAndCreatedAtBetween(UUID accountId, LocalDateTime from, LocalDateTime to);
}

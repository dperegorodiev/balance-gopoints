package org.example.balance.service;


import jakarta.transaction.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    void deposit(UUID accountId, BigDecimal amount);
    void withdraw(UUID accountId, BigDecimal amount);
    void transfer(UUID fromId, UUID toId, BigDecimal amount);
//
    BigDecimal getBalance(UUID accountId);
//    List<Transaction> getStatement(UUID accountId, LocalDateTime from, LocalDateTime to);
}

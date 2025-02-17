package org.example.balance.service;


import org.example.balance.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    void accountReplenishment(UUID accountId, BigDecimal amount);
    void accountWithdrew(UUID accountId, BigDecimal amount);
    void transferFromAccountToAccount(UUID fromId, UUID toId, BigDecimal amount);
    BigDecimal getBalance(UUID accountId);
    List<Transaction> getStatement(UUID accountId, LocalDateTime from, LocalDateTime to);
}

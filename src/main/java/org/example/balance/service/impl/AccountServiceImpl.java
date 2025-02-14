package org.example.balance.service.impl;

import jakarta.transaction.Transaction;
import jakarta.transaction.Transactional;
import org.example.balance.repository.AccountRepository;
import org.example.balance.repository.TransactionRepository;
import org.example.balance.service.AccountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;

    private TransactionRepository transactionRepository;

    public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }


    @Override
    public void deposit(UUID accountId, BigDecimal amount) {

    }

    @Override
    public void withdraw(UUID accountId, BigDecimal amount) {

    }

    @Override
    public void transfer(UUID fromId, UUID toId, BigDecimal amount) {

    }

    @Override
    public BigDecimal getBalance(UUID accountId) {
        return null;
    }

    @Override
    public List<Transaction> getStatement(UUID accountId, LocalDateTime from, LocalDateTime to) {
        return List.of();
    }
}

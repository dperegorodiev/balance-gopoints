package org.example.balance.service.impl;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.balance.exception.AccountNotFoundException;
import org.example.balance.model.Account;
import org.example.balance.model.Transaction;
import org.example.balance.model.TransactionType;
import org.example.balance.repository.AccountRepository;
import org.example.balance.repository.TransactionRepository;
import org.example.balance.service.AccountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
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
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        createTransaction(account, amount, TransactionType.DEPOSIT);
    }
    private void createTransaction(Account account, BigDecimal amount, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccountId(account.getId());
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

//    @Override
//    public void withdraw(UUID accountId, BigDecimal amount) {
//
//    }
//
//    @Override
//    public void transfer(UUID fromId, UUID toId, BigDecimal amount) {
//
//    }
//
//    @Override
//    public BigDecimal getBalance(UUID accountId) {
//        return null;
//    }
//
//    @Override
//    public List<Transaction> getStatement(UUID accountId, LocalDateTime from, LocalDateTime to) {
//        return List.of();
//    }
}

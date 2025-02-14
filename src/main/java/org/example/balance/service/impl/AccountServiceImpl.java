package org.example.balance.service.impl;



import lombok.extern.slf4j.Slf4j;
import org.example.balance.exception.AccountNotFoundException;
import org.example.balance.exception.InsufficientFundsException;
import org.example.balance.model.Account;
import org.example.balance.model.Transaction;
import org.example.balance.model.TransactionType;
import org.example.balance.repository.AccountRepository;
import org.example.balance.repository.TransactionRepository;
import org.example.balance.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Account account = accountRepository.findByIdWithLock(accountId)
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

    @Override
    public void withdraw(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountId);
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        createTransaction(account, amount, TransactionType.WITHDRAWAL);
    }
//
//    @Override
//    public void transfer(UUID fromId, UUID toId, BigDecimal amount) {
//
//    }
//
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId))
                .getBalance();
    }
//
//    @Override
//    public List<Transaction> getStatement(UUID accountId, LocalDateTime from, LocalDateTime to) {
//        return List.of();
//    }
}

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

    // зачисление
    @Override
    public void deposit(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        createTransaction(account, amount, TransactionType.DEPOSIT);
    }

    // создаем запись в истории операций по счету
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

    // создаем запись в истории операций между счетами
    private void creatTransferTransaction(Account from, Account to, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccountId(from.getId());
        transaction.setToAccountId(to.getId());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(from.getBalance());
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    // списание
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

    // перевод
    @Override
    public void transfer(UUID fromId, UUID toId, BigDecimal amount) {

        UUID firstLock = fromId.compareTo(toId) < 0 ? fromId : toId;
        UUID secondLock = fromId.compareTo(toId) < 0 ? toId : fromId;

        Account from = accountRepository.findByIdWithLock(firstLock)
                .orElseThrow(() -> new AccountNotFoundException(firstLock));
        Account to = accountRepository.findByIdWithLock(secondLock)
                .orElseThrow(() -> new AccountNotFoundException(secondLock));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(fromId);
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);

        creatTransferTransaction(from, to, amount);
    }

    // запрос баланса
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

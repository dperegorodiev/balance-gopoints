package org.example.balance.service.impl;



import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public void accountReplenishment(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        createTransaction(account, amount, TransactionType.DEPOSIT);
    }


    private void createTransaction(Account account, BigDecimal amount, TransactionType type) {
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .accountId(account.getId())
                .type(type)
                .amount(amount)
                .balanceAfter(account.getBalance())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
    }

    private void creatTransferTransaction(Account from, Account to, BigDecimal amount) {
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .accountId(from.getId())
                .toAccountId(to.getId())
                .type(TransactionType.TRANSFER)
                .amount(amount)
                .balanceAfter(from.getBalance())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void accountWithdrew(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountId);
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        createTransaction(account, amount, TransactionType.WITHDRAWAL);
    }

    @Override
    @Transactional
    public void transferFromAccountToAccount(UUID fromId, UUID toId, BigDecimal amount) {

        UUID firstLock = fromId.compareTo(toId) < 0 ? fromId : toId;
        UUID secondLock = fromId.compareTo(toId) < 0 ? toId : fromId;

        Account from = accountRepository.findById(firstLock)
                .orElseThrow(() -> new AccountNotFoundException(firstLock));
        Account to = accountRepository.findById(secondLock)
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

    @Override
    public BigDecimal getBalance(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId))
                .getBalance();
    }

    @Override
    public List<Transaction> getStatement(UUID accountId, LocalDateTime from, LocalDateTime to) {

        if(!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        return transactionRepository.findByAccountIdAndCreatedAtBetween(accountId, from, to);
    }
}

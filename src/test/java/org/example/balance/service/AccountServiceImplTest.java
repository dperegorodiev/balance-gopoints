package org.example.balance.service;

import org.example.balance.exception.AccountNotFoundException;
import org.example.balance.exception.InsufficientFundsException;
import org.example.balance.model.Account;
import org.example.balance.model.Transaction;
import org.example.balance.model.TransactionType;
import org.example.balance.repository.AccountRepository;
import org.example.balance.repository.TransactionRepository;
import org.example.balance.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;
    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(500.00);

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(ACCOUNT_ID);
        testAccount.setBalance(INITIAL_BALANCE);
        testAccount.setCreatedAt(LocalDateTime.now());
    }

    // пополнение
    @Test
    void deposit_ShouldIncreaseBalance() {

        BigDecimal amount = BigDecimal.valueOf(100);
        when(accountRepository.findByIdWithLock(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));

        accountService.deposit(ACCOUNT_ID, amount);

        verify(accountRepository).save(testAccount);
        verify(transactionRepository).save(any(Transaction.class));
        assertEquals(INITIAL_BALANCE.add(amount), testAccount.getBalance());
    }

    // пополнение не существующего счета
    @Test
    void deposit_ShouldThrowException_WhenAccountNotFound() {

        when(accountRepository.findByIdWithLock(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.deposit(ACCOUNT_ID, BigDecimal.valueOf(100.00))
        );
    }

    // списание
    @Test
    void withdraw_ShouldDecreaseBalance_WhenSufficientFunds() {

        BigDecimal amount = BigDecimal.valueOf(100.00);
        when(accountRepository.findByIdWithLock(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));

        accountService.withdraw(ACCOUNT_ID, amount);

        verify(accountRepository).save(testAccount);
        verify(transactionRepository).save(any(Transaction.class));
        assertEquals(INITIAL_BALANCE.subtract(amount), testAccount.getBalance());
    }

    // списание больше чем есть на балансе
    @Test
    void withdraw_ShouldThrowException_WhenInsufficientFunds() {

        BigDecimal amount = BigDecimal.valueOf(2000.00);
        when(accountRepository.findByIdWithLock(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));

        assertThrows(InsufficientFundsException.class, () ->
                accountService.withdraw(ACCOUNT_ID, amount)
        );
    }

    // списание с не существующего счета
    @Test
    void withdraw_ShouldThrowException_WhenAccountNotFound() {

        when(accountRepository.findByIdWithLock(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.withdraw(ACCOUNT_ID, BigDecimal.valueOf(100.00))
        );
    }

    // баланс
    @Test
    void getBalance_ShouldReturnCorrectBalance() {

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));

        BigDecimal balance = accountService.getBalance(ACCOUNT_ID);

        assertEquals(INITIAL_BALANCE, balance);
    }

    // баланс не существующего счета
    @Test
    void getBalance_ShouldThrowException_WhenAccountNotFound() {

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.getBalance(ACCOUNT_ID)
        );
    }

    // перевод между счетами
    @Test
    void transfer_ShouldCorrectlyTransferMoney() {

        UUID toAccountId = UUID.randomUUID();
        Account toAccount = new Account();
        toAccount.setId(toAccountId);
        toAccount.setBalance(BigDecimal.valueOf(500.00));

        BigDecimal amount = BigDecimal.valueOf(100.00);

        when(accountRepository.findByIdWithLock(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByIdWithLock(toAccountId)).thenReturn(Optional.of(toAccount));
        doAnswer(invocation -> {
            Account savedAccount = invocation.getArgument(0);
            if (savedAccount.getId().equals(toAccountId)) {
                toAccount.setBalance(savedAccount.getBalance());
            }
            return null;
        }).when(accountRepository).save(any(Account.class));

        accountService.transfer(ACCOUNT_ID, toAccountId, amount);

        assertEquals(INITIAL_BALANCE.subtract(amount), testAccount.getBalance());
        assertEquals(BigDecimal.valueOf(600.00), toAccount.getBalance());
    }

    // перевод между счетами если не достаточно денег
    @Test
    void transfer_ShouldThrowException_WhenInsufficientFunds() {

        UUID toAccountId = UUID.randomUUID();
        Account toAccount = new Account();
        toAccount.setId(toAccountId);
        toAccount.setBalance(BigDecimal.valueOf(500.00));

        BigDecimal amount = BigDecimal.valueOf(2000.00);

        when(accountRepository.findByIdWithLock(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByIdWithLock(toAccountId)).thenReturn(Optional.of(toAccount));

        assertThrows(InsufficientFundsException.class, () ->
                accountService.transfer(ACCOUNT_ID, toAccountId, amount)
        );
    }

    // перевод между счетами если исходящий счет не существует
    @Test
    void transfer_ShouldThrowException_WhenSourceAccountNotFound() {

        UUID toAccountId = UUID.randomUUID();
        UUID firstLock = ACCOUNT_ID.compareTo(toAccountId) < 0 ? ACCOUNT_ID : toAccountId;
        when(accountRepository.findByIdWithLock(firstLock)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.transfer(ACCOUNT_ID, toAccountId, BigDecimal.valueOf(100.00))
        );
    }

    // перевод между счетами если счет получателя  не существует
    @Test
    void transfer_ShouldThrowException_WhenDestinationAccountNotFound() {

        UUID toAccountId = UUID.randomUUID();
        when(accountRepository.findByIdWithLock(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByIdWithLock(toAccountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.transfer(ACCOUNT_ID, toAccountId, BigDecimal.valueOf(100.00))
        );
    }

    // выписка
    @Test
    void getStatement_ShouldReturnTransactions() {

        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();

        List<Transaction> expectedTransactions = Arrays.asList(
                createTestTransaction(ACCOUNT_ID, BigDecimal.valueOf(100.00)),
                createTestTransaction(ACCOUNT_ID, BigDecimal.valueOf(200.00))
        );

        when(accountRepository.existsById(ACCOUNT_ID)).thenReturn(true);
        when(transactionRepository.findByAccountIdAndCreatedAtBetween(
                ACCOUNT_ID, from, to)).thenReturn(expectedTransactions);

        List<Transaction> actualTransactions = accountService.getStatement(ACCOUNT_ID, from, to);

        assertEquals(expectedTransactions, actualTransactions);
    }

    // выписка с не существующего счета
    @Test
    void getStatement_ShouldThrowException_WhenAccountNotFound() {

        when(accountRepository.existsById(ACCOUNT_ID)).thenReturn(false);

        assertThrows(AccountNotFoundException.class, () ->
                accountService.getStatement(ACCOUNT_ID, LocalDateTime.now(), LocalDateTime.now())
        );
    }

    // метод для создания тестовых транзакций
    private Transaction createTestTransaction(UUID accountId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(amount);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setType(TransactionType.DEPOSIT);
        return transaction;
    }
}

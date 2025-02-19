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
    void accountReplenishment_ShouldIncreaseBalance() {

        BigDecimal amount = BigDecimal.valueOf(100);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));

        accountService.accountReplenishment(ACCOUNT_ID, amount);

        verify(accountRepository).save(testAccount);
        verify(transactionRepository).save(any(Transaction.class));
        assertEquals(INITIAL_BALANCE.add(amount), testAccount.getBalance());
    }

    // пополнение не существующего счета
    @Test
    void accountReplenishment_ShouldThrowException_WhenAccountNotFound() {

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.accountReplenishment(ACCOUNT_ID, BigDecimal.valueOf(100.00))
        );
    }

    // списание
    @Test
    void accountWithdrew_ShouldDecreaseBalance_WhenSufficientFunds() {

        BigDecimal amount = BigDecimal.valueOf(100.00);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));

        accountService.accountWithdrew(ACCOUNT_ID, amount);

        verify(accountRepository).save(testAccount);
        verify(transactionRepository).save(any(Transaction.class));
        assertEquals(INITIAL_BALANCE.subtract(amount), testAccount.getBalance());
    }

    // списание больше чем есть на балансе
    @Test
    void accountWithdrew_ShouldThrowException_WhenInsufficientFunds() {

        BigDecimal amount = BigDecimal.valueOf(2000.00);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));

        assertThrows(InsufficientFundsException.class, () ->
                accountService.accountWithdrew(ACCOUNT_ID, amount)
        );
    }

    // списание с не существующего счета
    @Test
    void accountWithdrew_ShouldThrowException_WhenAccountNotFound() {

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.accountWithdrew(ACCOUNT_ID, BigDecimal.valueOf(100.00))
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
    void transferFromAccountToAccount_ShouldCorrectlyTransferMoney() {

        UUID fromId = UUID.fromString("58badf86-8aee-4f35-b0ce-5bfa1ac70e39");
        UUID toId = UUID.fromString("dfbde3fd-5f73-4198-9a49-aa89c4109438");

        Account fromAccount = new Account();
        fromAccount.setId(fromId);
        fromAccount.setBalance(new BigDecimal("1000.00"));

        Account toAccount = new Account();
        toAccount.setId(toId);
        toAccount.setBalance(new BigDecimal("500.00"));

        BigDecimal transferAmount = new BigDecimal("100.00");

        when(accountRepository.findById(any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID id = invocation.getArgument(0);
                    if (id.equals(fromId)) return Optional.of(fromAccount);
                    if (id.equals(toId)) return Optional.of(toAccount);
                    return Optional.empty();
                });

        accountService.transferFromAccountToAccount(fromId, toId, transferAmount);

        assertEquals(new BigDecimal("900.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("600.00"), toAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    // перевод между счетами если не достаточно денег
    @Test
    void transferFromAccountToAccount_ShouldThrowException_WhenInsufficientFunds() {

        UUID toAccountId = UUID.randomUUID();
        Account toAccount = new Account();
        toAccount.setId(toAccountId);
        toAccount.setBalance(BigDecimal.valueOf(500.00));

        BigDecimal amount = BigDecimal.valueOf(2000.00);

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));

        assertThrows(InsufficientFundsException.class, () ->
                accountService.transferFromAccountToAccount(ACCOUNT_ID, toAccountId, amount)
        );
    }

    // перевод между счетами если исходящий счет не существует
    @Test
    void transferFromAccountToAccount_ShouldThrowException_WhenSourceAccountNotFound() {

        UUID toAccountId = UUID.randomUUID();
        UUID firstLock = ACCOUNT_ID.compareTo(toAccountId) < 0 ? ACCOUNT_ID : toAccountId;
        when(accountRepository.findById(firstLock)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.transferFromAccountToAccount(ACCOUNT_ID, toAccountId, BigDecimal.valueOf(100.00))
        );
    }

    // перевод между счетами если счет получателя не существует
    @Test
    void transferFromAccountToAccount_ShouldThrowException_WhenDestinationAccountNotFound() {

        UUID toAccountId = UUID.randomUUID();
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.transferFromAccountToAccount(ACCOUNT_ID, toAccountId, BigDecimal.valueOf(100.00))
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
        return Transaction.builder()
                .id(UUID.randomUUID())
                .accountId(accountId)
                .amount(amount)
                .balanceAfter(amount)
                .createdAt(LocalDateTime.now())
                .type(TransactionType.DEPOSIT)
                .build();
    }
}

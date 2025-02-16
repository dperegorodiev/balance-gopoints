package org.example.balance.controller;


import org.example.balance.model.Transaction;
import org.example.balance.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    private UUID accountId;


    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
    }

    // пополнение
    @Test
    void testDeposit() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/{id}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 10}")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());

        verify(accountService).deposit(accountId, BigDecimal.TEN);
    }

    // списание
    @Test
    void testWithdraw() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/{id}/withdrew", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 10}")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());

        verify(accountService).withdraw(accountId, BigDecimal.TEN);
    }

    // перевод
    @Test
    void testTransfer() throws Exception {
        UUID toAccountId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/accounts/{formId}/transfer/{toId}", accountId, toAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 10}")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());

        verify(accountService).transfer(accountId, toAccountId, BigDecimal.TEN);
    }

    // баланс
    @Test
    void testGetBalance() throws Exception {
        when(accountService.getBalance(accountId)).thenReturn(BigDecimal.TEN);

        mockMvc.perform(get("/api/v1/accounts/{id}/balance", accountId))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(accountService).getBalance(accountId);
    }

    // выписка
    @Test
    void testGetStatement() throws Exception {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();

        Transaction transaction = new Transaction();
        when(accountService.getStatement(accountId, from, to))
                .thenReturn(Collections.singletonList(transaction));

        mockMvc.perform(get("/api/v1/accounts/{id}/statement", accountId)
                        .param("from", from.toString())
                        .param("to", to.toString()))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(accountService).getStatement(accountId, from, to);
    }

    // пополнение счета отрицательной суммой
    @Test
    void testDepositWithInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/{id}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": -10}")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
    }
}

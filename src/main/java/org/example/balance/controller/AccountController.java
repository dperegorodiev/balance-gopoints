package org.example.balance.controller;

import jakarta.transaction.Transaction;
import jakarta.validation.Valid;
import org.example.balance.dto.OperationRequest;
import org.example.balance.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {


    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(@PathVariable UUID id,
                                        @RequestBody @Valid OperationRequest request) {
        accountService.deposit(id, request.getAmount());
        return ResponseEntity.ok().build();
    }
//
//    @PostMapping("/{id}/withdrew")
//    public ResponseEntity<Void> withdrew() {
//        return null;
//    }
//
//    @PostMapping("/{formId}/transfer/{toId}")
//    public ResponseEntity<Void> transfer() {
//        return null;
//    }
//
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getBalance(id));
    }
//
//    @GetMapping("/{id}/statement")
//    public ResponseEntity<List<Transaction>> getStatement(){
//        return ResponseEntity.ok(List.of());
//    }
}


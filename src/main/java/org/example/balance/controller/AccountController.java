package org.example.balance.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.balance.dto.OperationRequest;
import org.example.balance.model.Transaction;
import org.example.balance.service.AccountService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Управление счетами", description = "Операции с банковскими счетами")
public class AccountController {


    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // пополнение
    @PostMapping("/{id}/deposit")
    @Operation(summary = "Пополнение счета",
            description = "Добавление указанной суммы к балансу счета")
    @ApiResponse(responseCode = "200", description = "Пополнение выполнено успешно")
    @ApiResponse(responseCode = "400", description = "Некорректный ввод")
    public ResponseEntity<Void> deposit(@Parameter(description = "Идентификатор счета", required = true)
                                        @PathVariable UUID id,
                                        @RequestBody @Valid OperationRequest request) {
        accountService.deposit(id, request.getAmount());
        return ResponseEntity.ok().build();
    }

    //списание
    @PostMapping("/{id}/withdrew")
    @Operation(summary = "Списание средств",
            description = "Вычитание указанной суммы с баланса счета")
    @ApiResponse(responseCode = "200", description = "Списание выполнено успешно")
    @ApiResponse(responseCode = "400", description = "Некорректный ввод")
    public ResponseEntity<Void> withdrew(@Parameter(description = "Идентификатор счета", required = true)
                                         @PathVariable UUID id,
                                         @RequestBody @Valid OperationRequest request) {
        accountService.withdraw(id, request.getAmount());
        return ResponseEntity.ok().build();
    }

    // перевод со счета на счет
    @PostMapping("/{formId}/transfer/{toId}")
    @Operation(summary = "Перевод между счетами",
            description = "Перевод указанной суммы с одного счета на другой")
    @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно")
    @ApiResponse(responseCode = "400", description = "Некорректный ввод")
    public ResponseEntity<Void> transfer(@Parameter(description = "Идентификатор счета отправителя", required = true)
                                         @PathVariable UUID formId,
                                         @Parameter(description = "Идентификатор счета получателя", required = true)
                                         @PathVariable UUID toId,
                                         @RequestBody @Valid OperationRequest request) {
        accountService.transfer(formId, toId, request.getAmount());
        return ResponseEntity.ok().build();
    }

    //запрос баланса
    @GetMapping("/{id}/balance")
    @Operation(summary = "Получение баланса",
            description = "Получение текущего баланса указанного счета")
    @ApiResponse(responseCode = "200", description = "Баланс успешно получен")
    public ResponseEntity<BigDecimal> getBalance(@Parameter(description = "Идентификатор счета", required = true)
                                                 @PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getBalance(id));
    }

    // выписка
    @GetMapping("/{id}/statement")
    @Operation(summary = "Получение выписки",
            description = "Получение истории транзакций для указанного счета за выбранный период")
    @ApiResponse(responseCode = "200", description = "Выписка успешно получена")
    public ResponseEntity<List<Transaction>> getStatement(@Parameter(description = "Идентификатор счета", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Начальная дата выписки в формате: 2025-02-13T00:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Конечная дата выписки в формате: 2025-02-15T00:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(accountService.getStatement(id, from, to));
    }
}


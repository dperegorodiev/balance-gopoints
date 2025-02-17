package org.example.balance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.balance.dto.OperationRequest;
import org.example.balance.model.Transaction;
import org.example.balance.service.AccountService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Управление счетами", description = "Операции с банковскими счетами")
@RequiredArgsConstructor
public class AccountController {


    private final AccountService accountService;

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Пополнение счета",
            description = "Добавление указанной суммы к балансу счета")
    @ApiResponse(responseCode = "200", description = "Пополнение выполнено успешно")
    @ApiResponse(responseCode = "400", description = "Некорректный ввод")
    @ApiResponse(responseCode = "404", description = "Счет не найден")
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    public void deposit(@Parameter(description = "Идентификатор счета", required = true)
                                        @PathVariable UUID id,
                                        @RequestBody @Valid OperationRequest request) {
        accountService.deposit(id, request.getAmount());
    }


    @PostMapping("/{id}/withdrew")
    @Operation(summary = "Списание средств",
            description = "Вычитание указанной суммы с баланса счета")
    @ApiResponse(responseCode = "200", description = "Списание выполнено успешно")
    @ApiResponse(responseCode = "400", description = "Некорректный ввод")
    @ApiResponse(responseCode = "404", description = "Счет не найден")
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    public void withdrew(@Parameter(description = "Идентификатор счета", required = true)
                                         @PathVariable UUID id,
                                         @RequestBody @Valid OperationRequest request) {
        accountService.withdraw(id, request.getAmount());
    }


    @PostMapping("/{formId}/transfer/{toId}")
    @Operation(summary = "Перевод между счетами",
            description = "Перевод указанной суммы с одного счета на другой")
    @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно")
    @ApiResponse(responseCode = "400", description = "Некорректный ввод")
    @ApiResponse(responseCode = "404", description = "Один или оба счета не найдены")
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    public void transfer(@Parameter(description = "Идентификатор счета отправителя", required = true)
                                         @PathVariable UUID formId,
                                         @Parameter(description = "Идентификатор счета получателя", required = true)
                                         @PathVariable UUID toId,
                                         @RequestBody @Valid OperationRequest request) {
        accountService.transfer(formId, toId, request.getAmount());
    }


    @GetMapping("/{id}/balance")
    @Operation(summary = "Получение баланса",
            description = "Получение текущего баланса указанного счета")
    @ApiResponse(responseCode = "200", description = "Баланс успешно получен")
    @ApiResponse(responseCode = "404", description = "Счет не найден")
    public BigDecimal getBalance(@Parameter(description = "Идентификатор счета", required = true)
                                                 @PathVariable UUID id) {
        return accountService.getBalance(id);
    }


    @GetMapping("/{id}/statement")
    @Operation(summary = "Получение выписки",
            description = "Получение истории транзакций для указанного счета за выбранный период")
    @ApiResponse(responseCode = "200", description = "Выписка успешно получена")
    @ApiResponse(responseCode = "400", description = "Некорректный ввод (даты указаны неверно)")
    @ApiResponse(responseCode = "404", description = "Счет не найден")
    public List<Transaction> getStatement(@Parameter(description = "Идентификатор счета", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Начальная дата выписки в формате: 2025-02-13T00:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Конечная дата выписки в формате: 2025-02-15T00:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return accountService.getStatement(id, from, to);
    }
}


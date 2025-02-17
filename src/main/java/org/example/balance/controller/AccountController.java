package org.example.balance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Operation(operationId = "deposit",
            summary = "Пополнение счета",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пополнение выполнено успешно",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Некорректный ввод",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "404", description = "Счет не найден",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public void accountReplenishment(@Parameter(description = "Идентификатор счета", required = true)
                                        @PathVariable UUID id,
                                        @RequestBody @Valid OperationRequest request) {
        accountService.accountReplenishment(id, request.getAmount());
    }


    @PostMapping("/{id}/withdrew")
    @Operation(operationId = "withdrew",
            summary = "Списание средств",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Списание выполнено успешно",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Некорректный ввод",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "404", description = "Счет не найден",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public void accountWithdrew(@Parameter(description = "Идентификатор счета", required = true)
                                         @PathVariable UUID id,
                                         @RequestBody @Valid OperationRequest request) {
        accountService.accountWithdrew(id, request.getAmount());
    }


    @PostMapping("/{formId}/transfer/{toId}")
    @Operation(operationId = "transfer",
            summary = "Перевод между счетами",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Некорректный ввод",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "404", description = "Один или оба счета не найдены",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public void transferFromAccountToAccount(@Parameter(description = "Идентификатор счета отправителя", required = true)
                                         @PathVariable UUID formId,
                                         @Parameter(description = "Идентификатор счета получателя", required = true)
                                         @PathVariable UUID toId,
                                         @RequestBody @Valid OperationRequest request) {
        accountService.transferFromAccountToAccount(formId, toId, request.getAmount());
    }


    @GetMapping("/{id}/balance")
    @Operation(operationId = "getBalance",
            summary = "Получение баланса",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = BigDecimal.class))),
                    @ApiResponse(responseCode = "404", description = "Счет не найден",
                            content = @Content(mediaType = "application/json"))
            })
    public BigDecimal getBalance(@Parameter(description = "Идентификатор счета", required = true)
                                                 @PathVariable UUID id) {
        return accountService.getBalance(id);
    }


    @GetMapping("/{id}/statement")
    @Operation(operationId = "getStatement",
            summary = "Получение выписки",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Выписка успешно получена",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Transaction.class, type = "array"))),
                    @ApiResponse(responseCode = "400", description = "Некорректный ввод (даты указаны неверно)",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "404", description = "Счет не найден",
                            content = @Content(mediaType = "application/json"))
            })
    public List<Transaction> getStatement(@Parameter(description = "Идентификатор счета", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Начальная дата выписки в формате: 2025-02-13T00:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Конечная дата выписки в формате: 2025-02-15T00:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return accountService.getStatement(id, from, to);
    }
}


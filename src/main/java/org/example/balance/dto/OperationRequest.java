package org.example.balance.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public class OperationRequest {

    // Валидация операций со счетом
    @DecimalMin(value = "0.01", message = "Сумма операции должна быть больше или равна 0.01")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

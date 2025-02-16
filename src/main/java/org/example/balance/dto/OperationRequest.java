package org.example.balance.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationRequest {

    // Валидация операций со счетом
    @DecimalMin(value = "0.01", message = "Сумма операции должна быть больше или равна 0.01")
    private BigDecimal amount;

}

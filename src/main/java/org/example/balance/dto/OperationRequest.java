package org.example.balance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Запрос на финансовую операцию")
public class OperationRequest {

    // Валидация операций со счетом
    @DecimalMin(value = "0.01", message = "Сумма операции должна быть больше или равна 0.01")
    @Schema(description = "Сумма операции", example = "100.50")
    private BigDecimal amount;

}

package org.example.balance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import lombok.Data;

import java.math.BigDecimal;


@Schema(description = "Запрос на финансовую операцию")
@Data
public class OperationRequest {

    @DecimalMin(value = "0.01", message = "Сумма операции должна быть больше или равна 0.01")
    @Schema(description = "Сумма операции", example = "100.50")
    private BigDecimal amount;

}

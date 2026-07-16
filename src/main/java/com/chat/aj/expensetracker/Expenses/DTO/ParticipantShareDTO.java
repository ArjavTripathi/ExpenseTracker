package com.chat.aj.expensetracker.Expenses.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantShareDTO {
    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "shareAmount is required")
    @DecimalMin(value = "0.00", message = "shareAmount cannot be negative")
    private BigDecimal shareAmount;
}

package com.chat.aj.expensetracker.Expenses.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateExpenseDTO {
    @NotNull(message = "totalAmount is required")
    @DecimalMin(value = "0.01", message = "totalAmount must be greater than zero")
    private BigDecimal totalAmount;

    @NotBlank(message = "description is required")
    private String description;

    @NotEmpty(message = "participants must not be empty")
    @Valid
    private List<ParticipantShareDTO> participants;
}

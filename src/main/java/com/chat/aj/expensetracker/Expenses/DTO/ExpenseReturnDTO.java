package com.chat.aj.expensetracker.Expenses.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseReturnDTO {
    private String payerName;
    private String description;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private List<ExpenseParticipantsDTO> participants;
}

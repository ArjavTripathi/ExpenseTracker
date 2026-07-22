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
    private Long id;
    private Long groupId;
    private Long payerId;
    private String payerName;
    private String description;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<ExpenseParticipantsDTO> participants;
}

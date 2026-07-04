package com.chat.aj.expensetracker.Algorithm.DTO;

import com.chat.aj.expensetracker.Algorithm.Edge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SettlementDTO {
    private UserRef ower;
    private UserRef owed;
    private BigDecimal amount;

    public SettlementDTO(Edge e) {
        this.ower = new UserRef(e.getOwer().getId(), e.getOwer().getPersonName());
        this.owed = new UserRef(e.getOwed().getId(), e.getOwed().getPersonName());
        this.amount = e.getAmount();
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRef {
        private Long id;
        private String name;
    }
}

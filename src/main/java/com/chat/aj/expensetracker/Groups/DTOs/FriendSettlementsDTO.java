package com.chat.aj.expensetracker.Groups.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FriendSettlementsDTO {
    private String friend;
    /** Positive: this friend owes the user. Negative: the user owes this friend. */
    private BigDecimal amount;
}

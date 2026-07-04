package com.chat.aj.expensetracker.Groups.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private List<MemberDTO> members;
    private long expenseCount;
    private BigDecimal totalSpent;
}

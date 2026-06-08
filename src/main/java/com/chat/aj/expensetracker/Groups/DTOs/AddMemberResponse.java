package com.chat.aj.expensetracker.Groups.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddMemberResponse {
    public Long groupId;
    public String email;

}

package com.chat.aj.expensetracker.Groups.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupResponse {
    @NotBlank(message = "Group name is required")
    private String name;
}

package com.chat.aj.expensetracker.common.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data

public class ApiResponse {
    private String message;
    private Object data;
}

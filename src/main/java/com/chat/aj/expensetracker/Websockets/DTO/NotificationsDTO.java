package com.chat.aj.expensetracker.Websockets.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationsDTO {
    private String type;
    private String message;
    private Long groupId;
}

package com.chat.aj.expensetracker.Groups.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {
    private String name;
    private String owner;
    private List<String> members;

}

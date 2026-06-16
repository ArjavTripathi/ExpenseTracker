package com.chat.aj.expensetracker.Expenses;

import com.chat.aj.expensetracker.Expenses.DTO.ExpenseReturnDTO;
import com.chat.aj.expensetracker.common.Entities.Expenses;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/expenses")
public class ExpenseController {
    public final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<ExpenseReturnDTO> getExpenses(@RequestParam Long groupId, @RequestParam Long expenseId){
        ExpenseReturnDTO expenseToReturn = expenseService.getExpense(groupId, expenseId);
        return ResponseEntity.ok(expenseToReturn);
    }
}

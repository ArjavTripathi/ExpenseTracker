package com.chat.aj.expensetracker.Expenses;

import com.chat.aj.expensetracker.Expenses.DTO.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<String> createExpense(@RequestBody CreateExpenseDTO dto, Principal principal) {
        expenseService.createExpense(dto, principal.getName());
        return ResponseEntity.ok("Expense created successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<List<GetExpenseDTO>> getAllExpenses(@RequestParam Long groupId, Principal principal) {
        List<GetExpenseDTO> expenses = expenseService.getAllExpenses(groupId, principal.getName());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping
    public ResponseEntity<ExpenseReturnDTO> getExpense(@RequestParam Long groupId, @RequestParam Long expenseId, Principal principal) {
        ExpenseReturnDTO expense = expenseService.getExpense(groupId, expenseId, principal.getName());
        return ResponseEntity.ok(expense);
    }

    @PutMapping
    public ResponseEntity<String> updateExpense(@RequestBody UpdateExpenseDTO dto, Principal principal) {
        expenseService.updateExpense(dto, principal.getName());
        return ResponseEntity.ok("Expense updated successfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteExpense(@RequestParam Long groupId, @RequestParam Long expenseId, Principal principal) {
        expenseService.deleteExpense(groupId, expenseId, principal.getName());
        return ResponseEntity.ok("Expense deleted successfully");
    }
}

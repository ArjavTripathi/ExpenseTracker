package com.chat.aj.expensetracker.Expenses;

import com.chat.aj.expensetracker.Expenses.DTO.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<GetExpenseDTO>> getAllExpenses(@PathVariable Long groupId, Principal principal) {
        return ResponseEntity.ok(expenseService.getAllExpenses(groupId, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<String> createExpense(@PathVariable Long groupId,
                                                @Valid @RequestBody CreateExpenseDTO dto,
                                                Principal principal) {
        expenseService.createExpense(dto, groupId, principal.getName());
        return ResponseEntity.ok("Expense created successfully");
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseReturnDTO> getExpense(@PathVariable Long groupId,
                                                       @PathVariable Long expenseId,
                                                       Principal principal) {
        return ResponseEntity.ok(expenseService.getExpense(groupId, expenseId, principal.getName()));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<String> updateExpense(@PathVariable Long groupId,
                                                @PathVariable Long expenseId,
                                                @Valid @RequestBody UpdateExpenseDTO dto,
                                                Principal principal) {
        expenseService.updateExpense(dto, groupId, expenseId, principal.getName());
        return ResponseEntity.ok("Expense updated successfully");
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<String> deleteExpense(@PathVariable Long groupId,
                                                @PathVariable Long expenseId,
                                                Principal principal) {
        expenseService.deleteExpense(groupId, expenseId, principal.getName());
        return ResponseEntity.ok("Expense deleted successfully");
    }
}

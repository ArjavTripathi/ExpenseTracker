package com.chat.aj.expensetracker.Expenses;

import com.chat.aj.expensetracker.Expenses.DTO.ExpenseParticipantsDTO;
import com.chat.aj.expensetracker.Expenses.DTO.ExpenseRequestDTO;
import com.chat.aj.expensetracker.Expenses.DTO.ExpenseReturnDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/people")
    public ResponseEntity<List<ExpenseParticipantsDTO>> getExpenseParticipants(@RequestParam Long groupId, @RequestParam Long expenseId) {
        List<ExpenseParticipantsDTO> expenseParticipants = expenseService.getExpenseParticipants(groupId, expenseId);
        return ResponseEntity.ok(expenseParticipants);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteExpense(@RequestBody ExpenseRequestDTO expenseRequestDTO){
        expenseService.deleteExpense(expenseRequestDTO.getGroupId(), expenseRequestDTO.getExpenseId());
        return ResponseEntity.ok("Expense deleted successfully");
     }
}

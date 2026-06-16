package com.chat.aj.expensetracker.Expenses;

import com.chat.aj.expensetracker.Expenses.DTO.ExpenseParticipantsDTO;
import com.chat.aj.expensetracker.Expenses.DTO.ExpenseReturnDTO;
import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.common.Entities.ExpenseParticipants;
import com.chat.aj.expensetracker.common.Entities.ExpenseParticipantsRepository;
import com.chat.aj.expensetracker.common.Entities.Expenses;
import com.chat.aj.expensetracker.common.Entities.ExpensesRepository;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    public final GroupService groupService;
    public final ExpenseParticipantsRepository expenseParticipantsRepository;
    public final ExpensesRepository expensesRepository;

    public ExpenseReturnDTO getExpense(Long groupId, Long expenseId) {
        groupService.findGroupById(groupId);
        Expenses expense = expensesRepository.findExpenseById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find expense"));

        return new ExpenseReturnDTO(
                expense.getUser(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCreated_at()
        );
    }

    public List<ExpenseParticipantsDTO> getExpenseParticipants(Long groupId, Long expenseId) {
        groupService.findGroupById(groupId);
        Expenses expense = expensesRepository.findExpenseById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find expense"));
        List<ExpenseParticipants> allParticipants = expenseParticipantsRepository.findExpenseParticipantsByExpenses(expense);
        return allParticipants.stream()
                .map(p -> new ExpenseParticipantsDTO(p.getUser().getName(), p.getAmount()))
                .collect(Collectors.toList());
    }
}

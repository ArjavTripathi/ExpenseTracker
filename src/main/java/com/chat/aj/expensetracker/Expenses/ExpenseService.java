package com.chat.aj.expensetracker.Expenses;

import com.chat.aj.expensetracker.Expenses.DTO.ExpenseParticipantsDTO;
import com.chat.aj.expensetracker.Expenses.DTO.ExpenseReturnDTO;
import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.common.Entities.*;
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

    public void deleteExpense(Long groupId, Long expenseId){
        groupService.findGroupById(groupId);
        Expenses expense = expensesRepository.findExpenseById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find expense"));
        List<ExpenseParticipants> allParticipants = expenseParticipantsRepository.findExpenseParticipantsByExpenses(expense);
        expenseParticipantsRepository.deleteAll(allParticipants);
        expensesRepository.delete(expense);
    }

    public void deleteExpenseParticipantByUser(User user, Long groupId, Long expenseId){
        groupService.findGroupById(groupId);
        List<ExpenseParticipants> fullList = expenseParticipantsRepository.findExpenseParticipantsByUserAndExpenses(user, expensesRepository.findExpenseById(expenseId).orElseThrow(() -> new ResourceNotFoundException("Cannot find expense")));
        expenseParticipantsRepository.deleteAll(fullList);
    }
}

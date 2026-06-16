package com.chat.aj.expensetracker.Expenses;

import com.chat.aj.expensetracker.Expenses.DTO.ExpenseParticipantsDTO;
import com.chat.aj.expensetracker.Expenses.DTO.ExpenseReturnDTO;
import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.common.Entities.ExpenseParticipantsRepository;
import com.chat.aj.expensetracker.common.Entities.Expenses;
import com.chat.aj.expensetracker.common.Entities.ExpensesRepository;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public List<ExpenseParticipantsDTO>
}

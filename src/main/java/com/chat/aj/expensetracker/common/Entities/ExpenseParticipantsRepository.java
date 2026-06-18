package com.chat.aj.expensetracker.common.Entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseParticipantsRepository extends CrudRepository<ExpenseParticipants, Long> {
    List<ExpenseParticipants> findExpenseParticipantsByExpenses(Expenses expenses);

    List<ExpenseParticipants> findExpenseParticipantsByUserAndExpenses(User user, Expenses expenses);
}

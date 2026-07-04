package com.chat.aj.expensetracker.common.Entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseParticipantsRepository extends JpaRepository<ExpenseParticipants, Long> {
    List<ExpenseParticipants> findExpenseParticipantsByExpenses(Expenses expenses);
    List<ExpenseParticipants> findByExpensesGroup(Group group);
    List<ExpenseParticipants> findByUser(User user);
}

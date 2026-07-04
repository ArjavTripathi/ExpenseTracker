package com.chat.aj.expensetracker.common.Entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpensesRepository extends JpaRepository<Expenses, Long> {
    Optional<Expenses> findExpenseById(Long id);
    List<Expenses> findByGroup(Group group);
}

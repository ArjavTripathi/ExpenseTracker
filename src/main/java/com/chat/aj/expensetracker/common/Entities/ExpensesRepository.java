package com.chat.aj.expensetracker.common.Entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpensesRepository extends CrudRepository<Expenses, Long> {
    Optional<Expenses> findExpenseById(Long id);
    List<Expenses> findByGroup(Group group);
}

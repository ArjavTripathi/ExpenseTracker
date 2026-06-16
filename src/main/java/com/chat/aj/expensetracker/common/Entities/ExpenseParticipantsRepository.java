package com.chat.aj.expensetracker.common.Entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseParticipantsRepository extends CrudRepository<ExpenseParticipants, Long> {
}

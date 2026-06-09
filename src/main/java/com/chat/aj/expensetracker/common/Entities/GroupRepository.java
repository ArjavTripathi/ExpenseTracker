package com.chat.aj.expensetracker.common.Entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends CrudRepository<Group, Long> {
    List<Group> findByOwner(User owner);
}

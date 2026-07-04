package com.chat.aj.expensetracker.common.Entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByOwner(User owner);
    List<Group> findByMembers_MemberOrOwner(User member, User owner);
}

package com.chat.aj.expensetracker.common.Entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMembersRepository extends JpaRepository<GroupMembers, Long> {
    Optional<GroupMembers> findByGroupAndMember(Group group, User member);
    List<GroupMembers> findByGroup(Group group);
}

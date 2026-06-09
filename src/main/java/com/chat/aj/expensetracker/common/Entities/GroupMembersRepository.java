package com.chat.aj.expensetracker.common.Entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMembersRepository extends CrudRepository<GroupMembers, Long> {
    Optional<GroupMembers> findByGroupAndMember(Group group, User member);
    List<GroupMembers> findByMember(User member);
}

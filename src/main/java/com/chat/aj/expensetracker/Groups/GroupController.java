package com.chat.aj.expensetracker.Groups;

import com.chat.aj.expensetracker.Algorithm.Algorithm;
import com.chat.aj.expensetracker.Algorithm.DTO.SettlementDTO;
import com.chat.aj.expensetracker.Groups.DTOs.CreateGroupResponse;
import com.chat.aj.expensetracker.Groups.DTOs.GroupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;
    private final Algorithm algorithm;

    @PostMapping
    public ResponseEntity<String> createGroup(@RequestBody CreateGroupResponse newGroup, Principal principal) {
        groupService.createGroup(newGroup, principal.getName());
        return ResponseEntity.ok("Success!");
    }

    @GetMapping("/me")
    public ResponseEntity<List<GroupDTO>> getMyGroups(Principal principal) {
        return ResponseEntity.ok(groupService.getMyGroups(principal.getName()));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroup(groupId));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId, Principal principal) {
        groupService.deleteGroup(groupId, principal.getName());
        return ResponseEntity.ok("Success!");
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<String> addMember(@PathVariable Long groupId,
                                            @RequestBody Map<String, String> body,
                                            Principal principal) {
        groupService.addMemberToGroup(groupId, body.get("email"), principal.getName());
        return ResponseEntity.ok("Success!");
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<String> removeMember(@PathVariable Long groupId,
                                               @PathVariable Long memberId,
                                               Principal principal) {
        groupService.removeUser(groupId, memberId, principal.getName());
        return ResponseEntity.ok("Success!");
    }

    @GetMapping("/{groupId}/settlements")
    public ResponseEntity<List<SettlementDTO>> getSettlements(@PathVariable Long groupId) {
        return ResponseEntity.ok(algorithm.getOrComputeCache(groupId));
    }
}

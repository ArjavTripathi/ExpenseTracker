package com.chat.aj.expensetracker.Algorithm;

import com.chat.aj.expensetracker.Algorithm.DTO.SettlementDTO;
import com.chat.aj.expensetracker.Groups.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algorithm")
public class AlgorithmController {
    private final Algorithm algorithm;
    private final GroupService groupService;

    @GetMapping("/preprocessed")
    public ResponseEntity<Map<String, BigDecimal>> getPreprocessedData(@RequestParam Long groupId, Principal principal) {
        groupService.requireMember(groupId, principal.getName());
        return ResponseEntity.ok(algorithm.getBalances(groupId));
    }

    @GetMapping
    public ResponseEntity<List<SettlementDTO>> runAlgorithm(@RequestParam Long groupId, Principal principal) {
        groupService.requireMember(groupId, principal.getName());
        return ResponseEntity.ok(algorithm.getOrComputeCache(groupId));
    }
}

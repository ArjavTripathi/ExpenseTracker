package com.chat.aj.expensetracker.Algorithm;

import com.chat.aj.expensetracker.Algorithm.DTO.SettlementDTO;
import com.chat.aj.expensetracker.common.Entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algorithm")
public class AlgorithmController {
    public final Algorithm preprocessing;

    @GetMapping("/preprocessed")
    public ResponseEntity<Map<String, BigDecimal>> getPreprocessedData(@RequestParam Long groupId) {
        Map<User, BigDecimal> netBalances = preprocessing.populateMap(groupId);
        Map<String, BigDecimal> finalBalances = preprocessing.getBalances(netBalances, groupId);
        return ResponseEntity.ok(finalBalances);
    }

    @GetMapping
    public ResponseEntity<List<SettlementDTO>> runAlgorithm(@RequestParam Long groupId){
        Map<User, BigDecimal> netBalances = preprocessing.preprocess(groupId);
        List<SettlementDTO> settlements = preprocessing.algorithm(netBalances).stream()
                .map(SettlementDTO::new)
                .toList();
        return ResponseEntity.ok(settlements);
    }
}

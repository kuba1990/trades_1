package com.verygoodbank.tes.web.controller;

import com.verygoodbank.tes.port.GenerateCSV;
import com.verygoodbank.tes.port.ProcessCSV;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("api/v1")
@AllArgsConstructor
public class TradeEnrichmentController {

    private final GenerateCSV generate;

    private final ProcessCSV process;

    @PostMapping("/enrich")
    public String enrichTradeData(@RequestParam("file") MultipartFile file) {
        // Process trades
        String identifier = String.valueOf(UUID.randomUUID());
        process.startProcessCommand(file, identifier);
        return identifier;
    }

    @GetMapping("/enrich")
    public ResponseEntity<String> enrichTradeData(@RequestParam String identifier) {
        String csvContent = generate.processTrades(identifier);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=trades.csv");
        headers.add("Content-Type", "text/csv; charset=UTF-8");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
    }
}



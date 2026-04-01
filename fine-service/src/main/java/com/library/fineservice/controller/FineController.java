package com.library.fineservice.controller;

import com.library.fineservice.dto.FineDetailsDTO;
import com.library.fineservice.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    @GetMapping("/api/v1/fines/{id}/details")
    public ResponseEntity<FineDetailsDTO> getFineDetails(@PathVariable Long id) {
        return ResponseEntity.ok(fineService.getFineDetails(id));
    }

    // manual trigger
    @PostMapping("/api/v1/fines/calculate")
    public ResponseEntity<String> triggerFineCalculation() {
        fineService.triggerFineCalculation();
        return ResponseEntity.ok("Fine calculation triggered.");
    }
}
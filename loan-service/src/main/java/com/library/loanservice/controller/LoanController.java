package com.library.loanservice.controller;

import com.library.loanservice.dto.*;
import com.library.loanservice.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/api/v1/loans")
    public ResponseEntity<LoanResponseDTO> createLoan(@RequestBody LoanRequestDTO request) {
        return ResponseEntity.ok(loanService.createLoan(request));
    }

    @PutMapping("/api/v1/loans/{id}/return")
    public ResponseEntity<LoanResponseDTO> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnBook(id));
    }

}

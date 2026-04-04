package com.library.loanservice.controller;

import com.library.loanservice.client.BookClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CircuitBreakerController {

    private final BookClient bookClient;

    @GetMapping("/api/v1/loans/simulate-failure")
    @CircuitBreaker(name = "bookServiceBreaker", fallbackMethod = "fallback")
    public ResponseEntity<Map<String, Object>> simulateFailure(
            @RequestParam(defaultValue = "true") boolean fail) {

        if (fail) {
            log.info("Simulating failure — calling non-existent book...");
            bookClient.getBook(99999L);
        } else {
            log.info("Simulating success — calling valid book...");
            bookClient.getBook(1L);
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Book Service responded normally.",
                "circuitState", "CLOSED"
        ));
    }

    public ResponseEntity<Map<String, Object>> fallback(boolean fail, Throwable t) {
        log.warn("FALLBACK activated. Reason: {}", t.getMessage());

        return ResponseEntity.ok(Map.of(
                "status", "FALLBACK_ACTIVATED",
                "message", "Book catalog temporarily unavailable. The circuit breaker is currently OPEN to prevent cascading failures.",
                "error", t.getMessage() != null ? t.getMessage() : "Unknown error"
        ));
    }
}
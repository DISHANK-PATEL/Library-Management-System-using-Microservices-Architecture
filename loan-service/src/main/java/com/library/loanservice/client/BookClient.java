package com.library.loanservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "book-service")
public interface BookClient {

    @GetMapping("/api/v1/internal/books/{id}")
    BookClientResponse getBook(@PathVariable Long id);

    @PutMapping("/api/v1/internal/books/{id}/availability")
    void updateAvailability(@PathVariable Long id, @RequestParam boolean available);
}
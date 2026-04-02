package com.library.fineservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "book-service")
public interface BookClient {

    @GetMapping("/api/v1/internal/books/{id}")
    BookClientResponse getBook(@PathVariable Long id);
}
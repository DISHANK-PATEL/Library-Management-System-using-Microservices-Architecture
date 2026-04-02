package com.library.fineservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "loan-service")
public interface LoanClient {

    @GetMapping("/api/v1/internal/loans/{id}")
    LoanClientResponse getLoan(@PathVariable Long id);
}
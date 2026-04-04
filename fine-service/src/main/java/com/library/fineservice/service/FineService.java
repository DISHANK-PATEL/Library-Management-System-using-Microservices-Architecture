package com.library.fineservice.service;

import com.library.fineservice.client.*;
import com.library.fineservice.dto.FineDetailsDTO;
import com.library.fineservice.entity.Fine;
import com.library.fineservice.entity.FineStatus;
import com.library.fineservice.exception.ResourceNotFoundException;
import com.library.fineservice.repository.FineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class FineService {

    private final FineRepository fineRepository;
    private final LoanClient loanClient;
    private final MemberClient memberClient;
    private final BookClient bookClient;
    private final WebClient.Builder webClientBuilder;

    @Value("${fine.rate-per-day}")
    private BigDecimal ratePerDay;

    public FineDetailsDTO getFineDetails(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found with id: " + fineId));

        // Fire all three calls in parallel
        CompletableFuture<LoanClientResponse> loanFuture =
                CompletableFuture.supplyAsync(() -> loanClient.getLoan(fine.getLoanId()));

        CompletableFuture<MemberClientResponse> memberFuture =
                CompletableFuture.supplyAsync(() -> memberClient.getMember(fine.getMemberId()));

        CompletableFuture<BookClientResponse> bookFuture =
                loanFuture.thenApplyAsync(loan -> bookClient.getBook(loan.getBookId()));

        // Wait for all to complete
        CompletableFuture.allOf(loanFuture, memberFuture, bookFuture).join();

        LoanClientResponse loan = loanFuture.join();
        MemberClientResponse member = memberFuture.join();
        BookClientResponse book = bookFuture.join();

        return FineDetailsDTO.builder()
                .fineId(fine.getId())
                .amount(fine.getAmount())
                .status(fine.getStatus().name())
                .member(FineDetailsDTO.MemberInfo.builder()
                        .memberId(member.getId())
                        .name(member.getName())
                        .build())
                .loan(FineDetailsDTO.LoanInfo.builder()
                        .loanId(loan.getId())
                        .dueDate(loan.getDueDate().toString())
                        .build())
                .book(FineDetailsDTO.BookInfo.builder()
                        .bookId(book.getId())
                        .title(book.getTitle())
                        .servedByInstance(book.getServedByInstance())
                        .build())
                .build();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void calculateOverdueFines() {
        log.info("Starting overdue fine calculation cron job...");

        List<LoanClientResponse> activeLoans = webClientBuilder.build()
                .get()
                .uri("http://loan-service/api/v1/internal/loans/active")
                .retrieve()
                .bodyToFlux(LoanClientResponse.class)
                .collectList()
                .block();

        if (activeLoans == null || activeLoans.isEmpty()) {
            log.info("No active loans found.");
            return;
        }

        for (LoanClientResponse loan : activeLoans) {
            // Skip if not overdue
            if (!loan.getDueDate().isBefore(LocalDate.now())) {
                continue;
            }

            // Skip if fine already exists for this loan
            if (fineRepository.findByLoanId(loan.getId()).isPresent()) {
                log.info("Fine already exists for loanId={}, skipping.", loan.getId());
                continue;
            }

            // Calculate fine
            long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
            BigDecimal amount = ratePerDay.multiply(BigDecimal.valueOf(daysOverdue));

            Fine fine = Fine.builder()
                    .loanId(loan.getId())
                    .memberId(loan.getMemberId())
                    .amount(amount)
                    .status(FineStatus.UNPAID)
                    .build();
            fineRepository.save(fine);

            log.info("Fine created: loanId={}, memberId={}, amount={}, daysOverdue={}",
                    loan.getId(), loan.getMemberId(), amount, daysOverdue);
        }

        log.info("Overdue fine calculation completed.");
    }

    public void triggerFineCalculation() {
        calculateOverdueFines();
    }
}
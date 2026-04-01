package com.library.loanservice.service;

import com.library.loanservice.client.BookClient;
import com.library.loanservice.client.BookClientResponse;
import com.library.loanservice.client.MemberClient;
import com.library.loanservice.client.MemberClientResponse;
import com.library.loanservice.dto.*;
import com.library.loanservice.entity.*;
import com.library.loanservice.repository.LoanEventRepository;
import com.library.loanservice.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanEventRepository loanEventRepository;
    private final BookClient bookClient;
    private final MemberClient memberClient;

    @Transactional
    public LoanResponseDTO createLoan(LoanRequestDTO request) {

        BookClientResponse book = bookClient.getBook(request.getBookId());
        if (!book.isAvailable()) {
            throw new RuntimeException("Book with id " + request.getBookId() + " is not available");
        }

        MemberClientResponse member = memberClient.getMember(request.getMemberId());

        Loan loan = Loan.builder()
                .bookId(request.getBookId())
                .memberId(request.getMemberId())
                .status(LoanStatus.ACTIVE)
                .dueDate(LocalDate.now().plusDays(14))
                .build();
        loan = loanRepository.save(loan);

        bookClient.updateAvailability(request.getBookId(), false);

        LoanEvent event = LoanEvent.builder()
                .loanId(loan.getId())
                .eventType(EventType.LOAN_CREATED)
                .timestamp(LocalDateTime.now())
                .build();
        loanEventRepository.save(event);

        log.info("Loan created: loanId={}, bookId={}, memberId={}",
                loan.getId(), request.getBookId(), request.getMemberId());

        return LoanResponseDTO.builder()
                .id(loan.getId())
                .bookId(loan.getBookId())
                .memberId(loan.getMemberId())
                .status(loan.getStatus().name())
                .dueDate(loan.getDueDate())
                .message("Loan created successfully. LOAN_CREATED event stored.")
                .build();
    }

    @Transactional
    public LoanResponseDTO returnBook(Long loanId){
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new RuntimeException("Loan " + loanId + " is already returned");
        }

        // Update the loan status and save
        loan.setStatus(LoanStatus.RETURNED);
        loanRepository.save(loan);

        // Phirse available mark kardo
        bookClient.updateAvailability(loan.getBookId(), true);
        // Store event
        LoanEvent event = LoanEvent.builder()
                .loanId(loanId)
                .eventType(EventType.BOOK_RETURNED)
                .timestamp(LocalDateTime.now())
                .build();
        loanEventRepository.save(event);

        log.info("Book returned: loanId={}", loanId);

        return LoanResponseDTO.builder()
                .id(loan.getId())
                .bookId(loan.getBookId())
                .memberId(loan.getMemberId())
                .status(loan.getStatus().name())
                .dueDate(loan.getDueDate())
                .message("Book returned. BOOK_RETURNED event appended to Event Store.")
                .build();
    }

}
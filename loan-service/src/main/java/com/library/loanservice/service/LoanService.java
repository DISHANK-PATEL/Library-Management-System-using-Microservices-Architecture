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
import io.micrometer.tracing.Tracer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final Tracer tracer;
    private final LoanRepository loanRepository;
    private final LoanEventRepository loanEventRepository;
    private final BookClient bookClient;
    private final MemberClient memberClient;
    private final EventPublisher eventPublisher;

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

        LoanEventMessage eventMessage = LoanEventMessage.builder()
                .eventId("evt-" + loan.getId())
                .eventType("LOAN_CREATED")
                .loanId(loan.getId())
                .bookTitle(book.getTitle())
                .memberEmail(member.getEmail())
                .dueDate(loan.getDueDate().toString())
                .build();
        eventPublisher.publishLoanCreated(eventMessage);

        if (tracer != null && tracer.currentSpan() != null) {
            tracer.currentSpan().tag("loanId", loan.getId().toString());
            tracer.currentSpan().tag("memberId", request.getMemberId().toString());
            tracer.currentSpan().tag("bookId", request.getBookId().toString());
        }

        return LoanResponseDTO.builder()
                .id(loan.getId())
                .bookId(loan.getBookId())
                .memberId(loan.getMemberId())
                .status(loan.getStatus().name())
                .dueDate(loan.getDueDate())
                .message("Loan created successfully. LOAN_CREATED event published to RabbitMQ.")
                .build();
    }

    @Transactional
    public LoanResponseDTO returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new RuntimeException("Loan " + loanId + " is already returned");
        }

        BookClientResponse book = bookClient.getBook(loan.getBookId());

        loan.setStatus(LoanStatus.RETURNED);
        loanRepository.save(loan);

        bookClient.updateAvailability(loan.getBookId(), true);

        LoanEvent event = LoanEvent.builder()
                .loanId(loanId)
                .eventType(EventType.BOOK_RETURNED)
                .timestamp(LocalDateTime.now())
                .build();
        loanEventRepository.save(event);

        log.info("Book returned: loanId={}", loanId);

        LoanEventMessage eventMessage = LoanEventMessage.builder()
                .eventId("evt-return-" + loanId)
                .eventType("BOOK_RETURNED")
                .loanId(loanId)
                .bookTitle(book.getTitle())
                .dueDate(loan.getDueDate().toString())
                .build();
        eventPublisher.publishLoanReturned(eventMessage);

        if (tracer != null && tracer.currentSpan() != null) {
            tracer.currentSpan().tag("loanId", loanId.toString());
        }

        return LoanResponseDTO.builder()
                .id(loan.getId())
                .bookId(loan.getBookId())
                .memberId(loan.getMemberId())
                .status(loan.getStatus().name())
                .dueDate(loan.getDueDate())
                .message("Book returned. BOOK_RETURNED event appended to Event Store.")
                .build();
    }

    public RebuildResponseDTO rebuildLoanState(Long loanId) {
        Loan currentLoan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        List<LoanEvent> events = loanEventRepository.findByLoanIdOrderByTimestampAsc(loanId);

        if (events.isEmpty()) {
            throw new RuntimeException("No events found for loan: " + loanId);
        }

        LoanStatus trueStatus = null;
        for (LoanEvent event : events) {
            switch (event.getEventType()) {
                case LOAN_CREATED -> trueStatus = LoanStatus.ACTIVE;
                case BOOK_RETURNED -> trueStatus = LoanStatus.RETURNED;
            }
        }

        if (trueStatus == null) {
            throw new RuntimeException("Could not determine state from events for loan: " + loanId);
        }

        List<EventDTO> auditLog = events.stream()
                .map(e -> EventDTO.builder()
                        .eventType(e.getEventType().name())
                        .timestamp(e.getTimestamp())
                        .build())
                .toList();

        String action;
        String message;

        if (currentLoan.getStatus() == trueStatus) {
            action = "NO_ACTION";
            message = "Read Model is consistent with the Event Store. No changes needed.";
        } else {
            currentLoan.setStatus(trueStatus);
            loanRepository.save(currentLoan);
            action = "OVERWROTE_READ_MODEL";
            message = "State divergence detected. The Read Model was updated to match the Event Store.";
            log.warn("STATE_INCONSISTENCY_RESOLVED for Loan ID: {}", loanId);
        }

        if (tracer != null && tracer.currentSpan() != null) {
            tracer.currentSpan().tag("loanId", loanId.toString());
            tracer.currentSpan().tag("reconciliationResult", action);
        }

        return RebuildResponseDTO.builder()
                .loanId(loanId)
                .reconciliationAction(action)
                .message(message)
                .resolvedStatus(trueStatus.name())
                .eventsReplayed(events.size())
                .auditLog(auditLog)
                .build();
    }

    public LoanResponseDTO getLoanById(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        return LoanResponseDTO.builder()
                .id(loan.getId())
                .bookId(loan.getBookId())
                .memberId(loan.getMemberId())
                .status(loan.getStatus().name())
                .dueDate(loan.getDueDate())
                .build();
    }

    public List<LoanResponseDTO> getActiveLoans() {
        return loanRepository.findByStatus(LoanStatus.ACTIVE).stream()
                .map(loan -> LoanResponseDTO.builder()
                        .id(loan.getId())
                        .bookId(loan.getBookId())
                        .memberId(loan.getMemberId())
                        .status(loan.getStatus().name())
                        .dueDate(loan.getDueDate())
                        .build())
                .toList();
    }
}
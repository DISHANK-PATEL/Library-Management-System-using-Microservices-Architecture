package com.library.loanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// Immutable event log. Events are only INSERTED, never updated or deleted.
 // Event Sourcing ka "source of truth" he ye

@Entity
@Table(name = "loan_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long loanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
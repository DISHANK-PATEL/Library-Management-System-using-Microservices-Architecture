package com.library.loanservice.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponseDTO {
    private Long id;
    private Long bookId;
    private Long memberId;
    private String status;
    private LocalDate dueDate;
    private String message;
}
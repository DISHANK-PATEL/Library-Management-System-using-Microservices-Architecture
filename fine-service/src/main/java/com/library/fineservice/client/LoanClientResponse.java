package com.library.fineservice.client;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanClientResponse {
    private Long id;
    private Long bookId;
    private Long memberId;
    private String status;
    private LocalDate dueDate;
}
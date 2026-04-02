package com.library.loanservice.dto;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEventMessage implements Serializable {
    private String eventId;
    private String eventType;
    private Long loanId;
    private String bookTitle;
    private String memberEmail;
    private String dueDate;
}
package com.library.notificationservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LibraryEvent {
    private String eventId;
    private String eventType;
    private Long loanId;
    private String bookTitle;
    private String memberEmail;
    private String dueDate;
    private Long fineId;
    private String amount;
}
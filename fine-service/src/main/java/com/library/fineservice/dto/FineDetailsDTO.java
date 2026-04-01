package com.library.fineservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineDetailsDTO {
    private Long fineId;
    private BigDecimal amount;
    private String status;
    private MemberInfo member;
    private LoanInfo loan;
    private BookInfo book;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberInfo {
        private Long memberId;
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoanInfo {
        private Long loanId;
        private String dueDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookInfo {
        private Long bookId;
        private String title;
        private String servedByInstance;
    }
}
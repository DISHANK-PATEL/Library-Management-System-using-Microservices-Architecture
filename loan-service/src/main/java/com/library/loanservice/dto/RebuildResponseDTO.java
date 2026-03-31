package com.library.loanservice.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RebuildResponseDTO {
    private Long loanId;
    private String reconciliationAction;
    private String message;
    private String resolvedStatus;
    private int eventsReplayed;
    private List<EventDTO> auditLog;
}
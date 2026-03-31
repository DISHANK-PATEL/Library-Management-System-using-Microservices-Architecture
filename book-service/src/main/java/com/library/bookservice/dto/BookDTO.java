package com.library.bookservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDTO {
    private Long id;
    private String title;
    private boolean available;
    private String servedByInstance;
}

package com.library.loanservice.client;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookClientResponse {
    private Long id;
    private String title;
    private boolean available;
    private String servedByInstance;
}
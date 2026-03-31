package com.library.loanservice.client;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberClientResponse {
    private Long id;
    private String name;
    private String email;
}
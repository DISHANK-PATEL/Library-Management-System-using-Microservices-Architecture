package com.library.fineservice.client;

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
package com.saathi.backend.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String role;
    private String token;
    private String message;
}
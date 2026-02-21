package com.saathi.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RegisterResponse {
    private Long userId;
    private String name;
    private String phone;
    private String role;
    private String message;
}

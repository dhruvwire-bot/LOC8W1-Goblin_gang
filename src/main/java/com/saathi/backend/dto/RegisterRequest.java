package com.saathi.backend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String phone;
    private String email;
    private String password;
    private String language;
    private String role;

    // Only if WORKER
    private String skills;
    private String city;
    private Double latitude;
    private Double longitude;
    private Double pricePerHour;   // NEW
}
package com.saathi.backend.dto;

import lombok.Data;

@Data
public class ConfirmBookingRequest {
    private Long workerId;       // worker customer chose
    private String skillNeeded;
    private String description;
}
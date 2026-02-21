package com.saathi.backend.dto;

import lombok.Data;

@Data
public class BookingRequest {
    private String skillNeeded;
    private String description;
    private String city;
}
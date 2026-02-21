package com.saathi.backend.dto;

import lombok.Data;

@Data
public class RatingRequest {
    private Long bookingId;
    private Integer score;      // 1 to 5
    private String comment;
}
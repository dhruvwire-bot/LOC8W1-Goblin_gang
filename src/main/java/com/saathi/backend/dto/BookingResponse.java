package com.saathi.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private String skillNeeded;
    private String description;
    private String status;
    private String customerName;
    private String customerEmail;
    private String workerName;
    private String workerSkills;
    private String workerCity;
    private Double workerRating;
    private LocalDateTime createdAt;
}
package com.saathi.backend.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkerSummary {
    private Long workerId;
    private String name;
    private String skills;
    private String city;
    private Double rating;
    private Integer jobsCompleted;
    private Double pricePerHour;
}
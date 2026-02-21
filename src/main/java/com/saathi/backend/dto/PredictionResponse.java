package com.saathi.backend.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PredictionResponse {
    private String workerName;
    private Double predictedWeeklyIncome;
    private String analysis;
    private String tips;
    private Double currentWeekEarnings;
    private Double lastWeekEarnings;
    private Double averageWeeklyEarnings;
    private Integer totalJobsDone;
    private Double averageRating;
}
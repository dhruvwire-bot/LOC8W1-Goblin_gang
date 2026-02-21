package com.saathi.backend.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EarningsResponse {
    private String workerName;
    private Double totalEarnings;
    private Integer totalJobsDone;
    private Integer jobsThisWeek;
    private Double earningsThisWeek;
    private Double averageRating;
    private Double pricePerHour;
    private List<JobSummary> recentJobs;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobSummary {
        private Long bookingId;
        private String customerName;
        private String skillNeeded;
        private String completedAt;
        private Double earned;
    }
}
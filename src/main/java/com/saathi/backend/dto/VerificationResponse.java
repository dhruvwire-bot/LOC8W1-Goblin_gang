package com.saathi.backend.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerificationResponse {
    private Long workerId;
    private String workerName;
    private Boolean isVerified;
    private String verificationStatus;
    private String aadhaarLastFour;    // only show last 4 digits
    private String message;
}
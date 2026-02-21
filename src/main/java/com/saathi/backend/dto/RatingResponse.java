package com.saathi.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingResponse {
    private Long ratingId;
    private Long bookingId;
    private String workerName;
    private String customerName;
    private Integer score;
    private String comment;
    private LocalDateTime ratedAt;
}
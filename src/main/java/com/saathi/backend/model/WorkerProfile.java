package com.saathi.backend.model;

import com.saathi.backend.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "worker_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // stored as "PLUMBER,ELECTRICIAN" — matched from Gemini response
    private String skills;

    private String city;
    private Double latitude;
    private Double longitude;
    private Boolean isAvailable;

    @Column(columnDefinition = "DECIMAL(2,1) DEFAULT 5.0")
    private Double rating;

    private Integer jobsCompleted;
    private Double pricePerHour;
}
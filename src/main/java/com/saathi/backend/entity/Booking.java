package com.saathi.backend.entity;

import com.saathi.backend.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private com.saathi.backend.model.WorkerProfile worker;

    private String skillNeeded;
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public enum Status {
        PENDING, MATCHED, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
package com.saathi.backend.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;       // bcrypt hashed

    @Enumerated(EnumType.STRING)
    private Role role;             // CUSTOMER or WORKER

    private String language;       // "hindi", "marathi", etc.

    private LocalDateTime createdAt;

    public enum Role { CUSTOMER, WORKER }
}
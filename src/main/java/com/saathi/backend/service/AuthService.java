package com.saathi.backend.service;

import com.saathi.backend.dto.AuthResponse;
import com.saathi.backend.dto.RegisterRequest;
import com.saathi.backend.entity.User;
import com.saathi.backend.model.WorkerProfile;
import com.saathi.backend.repository.UserRepository;
import com.saathi.backend.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── REGISTER ─────────────────────────────────────
    public AuthResponse register(RegisterRequest req) {

        if (req.getRole() == null || req.getRole().isBlank()) {
            throw new RuntimeException("Role is required: CUSTOMER or WORKER");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByPhone(req.getPhone())) {
            throw new RuntimeException("Phone already registered");
        }

        User user = User.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.valueOf(req.getRole().toUpperCase()))
                .language(req.getLanguage())
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        if (req.getRole().equalsIgnoreCase("WORKER")) {
            WorkerProfile profile = WorkerProfile.builder()
                    .user(user)
                    .skills(req.getSkills())
                    .city(req.getCity())
                    .latitude(req.getLatitude())
                    .longitude(req.getLongitude())
                    .isAvailable(true)
                    .rating(5.0)
                    .jobsCompleted(0)
                    .pricePerHour(req.getPricePerHour())   // NEW
                    .build();

            workerRepository.save(profile);
        }

        return AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Registration successful")
                .build();
    }

    // ─── GET CURRENT USER ─────────────────────────────
    public AuthResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("User fetched")
                .build();
    }
}
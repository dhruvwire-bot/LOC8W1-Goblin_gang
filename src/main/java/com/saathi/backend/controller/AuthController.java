package com.saathi.backend.controller;


import com.saathi.backend.dto.AuthResponse;
import com.saathi.backend.dto.RegisterRequest;
import com.saathi.backend.repository.UserRepository;
import com.saathi.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import com.saathi.backend.entity.User;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {


    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // POST /api/auth/register — public, no auth needed
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    // GET /api/auth/me — Basic Auth handles verification automatically
    // Spring injects the logged in user via @AuthenticationPrincipal
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getMe(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(authService.getMe(userDetails.getUsername()));
    }

    @GetMapping("/api/auth/oauth2/success")
    public ResponseEntity<?> oauthSuccess(@AuthenticationPrincipal OAuth2User user) {
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");

        // Find or create user in DB
        User dbUser = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setPassword(passwordEncoder.encode("oauth_default_password"));
                    return userRepository.save(newUser);
                });

        return ResponseEntity.ok(Map.of(
                "email", dbUser.getEmail(),
                "name", dbUser.getName(),
                "message", "Login successful"
        ));
    }

    @GetMapping("/api/auth/oauth2/failure")
    public ResponseEntity<?> oauthFailure() {
        return ResponseEntity.status(401).body("Google login failed");
    }
}
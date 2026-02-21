package com.saathi.backend.controller;

import com.saathi.backend.dto.AuthResponse;
import com.saathi.backend.dto.RegisterRequest;
import com.saathi.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

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
}
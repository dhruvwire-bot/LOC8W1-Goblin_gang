package com.saathi.backend.controller;

import com.saathi.backend.dto.VerificationRequest;
import com.saathi.backend.dto.VerificationResponse;
import com.saathi.backend.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VerificationController {

    private final VerificationService verificationService;

    // POST /api/verification/submit
    // Worker submits Aadhaar number
    @PostMapping("/submit")
    public ResponseEntity<VerificationResponse> submitAadhaar(
            @RequestBody VerificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                verificationService.submitAadhaar(
                        request,
                        userDetails.getUsername()
                )
        );
    }

    // GET /api/verification/status
    // Worker checks their verification status
    @GetMapping("/status")
    public ResponseEntity<VerificationResponse> getStatus(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                verificationService.getStatus(userDetails.getUsername())
        );
    }

    // PUT /api/verification/approve/{workerId}
    // Admin approves — for demo just call this manually
    @PutMapping("/approve/{workerId}")
    public ResponseEntity<VerificationResponse> approve(
            @PathVariable Long workerId
    ) {
        return ResponseEntity.ok(
                verificationService.approveVerification(workerId)
        );
    }

    // PUT /api/verification/reject/{workerId}
    // Admin rejects
    @PutMapping("/reject/{workerId}")
    public ResponseEntity<VerificationResponse> reject(
            @PathVariable Long workerId
    ) {
        return ResponseEntity.ok(
                verificationService.rejectVerification(workerId)
        );
    }
}
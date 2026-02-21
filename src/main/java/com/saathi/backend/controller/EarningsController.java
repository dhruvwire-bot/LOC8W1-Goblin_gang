package com.saathi.backend.controller;

import com.saathi.backend.dto.EarningsResponse;
import com.saathi.backend.service.EarningsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/earnings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EarningsController {

    private final EarningsService earningsService;

    // GET /api/earnings — worker sees their dashboard
    @GetMapping
    public ResponseEntity<EarningsResponse> getEarnings(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                earningsService.getEarnings(userDetails.getUsername())
        );
    }
}
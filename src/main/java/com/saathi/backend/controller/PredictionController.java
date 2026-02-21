package com.saathi.backend.controller;

import com.saathi.backend.dto.PredictionResponse;
import com.saathi.backend.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prediction")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;

    // GET /api/prediction/income
    // Worker gets AI prediction for next week
    @GetMapping("/income")
    public ResponseEntity<PredictionResponse> predictIncome(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                predictionService.predictIncome(userDetails.getUsername())
        );
    }
}

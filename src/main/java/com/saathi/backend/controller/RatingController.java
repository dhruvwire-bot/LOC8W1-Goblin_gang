package com.saathi.backend.controller;

import com.saathi.backend.dto.RatingRequest;
import com.saathi.backend.dto.RatingResponse;
import com.saathi.backend.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RatingController {

    private final RatingService ratingService;

    // POST /api/ratings — customer submits rating after job done
    @PostMapping
    public ResponseEntity<RatingResponse> submitRating(
            @RequestBody RatingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(201).body(
                ratingService.submitRating(request, userDetails.getUsername())
        );
    }

    // GET /api/ratings/booking/{bookingId} — get rating for a booking
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<RatingResponse> getRating(
            @PathVariable Long bookingId
    ) {
        return ResponseEntity.ok(
                ratingService.getRatingByBooking(bookingId)
        );
    }
}

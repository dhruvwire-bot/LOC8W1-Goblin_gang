package com.saathi.backend.controller;

import com.saathi.backend.dto.*;
import com.saathi.backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    // STEP 1 — GET /api/bookings/workers?skill=plumber&city=Pune
    // Customer searches available workers to choose from
    @GetMapping("/workers")
    public ResponseEntity<List<WorkerSummary>> getAvailableWorkers(
            @RequestParam String skill,
            @RequestParam(required = false) String city,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                bookingService.getAvailableWorkers(skill, city)
        );
    }

    // STEP 2 — POST /api/bookings/confirm
    // Customer confirms booking with chosen worker
    @PostMapping("/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @RequestBody ConfirmBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(201).body(
                bookingService.confirmBooking(request, userDetails.getUsername())
        );
    }

    // GET /api/bookings/my — customer sees their bookings
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                bookingService.getMyBookings(userDetails.getUsername())
        );
    }

    // GET /api/bookings/jobs — worker sees their jobs
    @GetMapping("/jobs")
    public ResponseEntity<List<BookingResponse>> getMyJobs(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                bookingService.getMyJobs(userDetails.getUsername())
        );
    }

    // PUT /api/bookings/{id}/accept — worker accepts
    @PutMapping("/{id}/accept")
    public ResponseEntity<BookingResponse> acceptBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                bookingService.acceptBooking(id, userDetails.getUsername())
        );
    }

    // PUT /api/bookings/{id}/complete — worker completes
    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                bookingService.completeBooking(id, userDetails.getUsername())
        );
    }

    // PUT /api/bookings/{id}/cancel — customer cancels
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                bookingService.cancelBooking(id, userDetails.getUsername())
        );
    }
}
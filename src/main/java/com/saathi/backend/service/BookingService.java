package com.saathi.backend.service;

import com.saathi.backend.dto.*;
import com.saathi.backend.entity.Booking;
import com.saathi.backend.entity.User;
import com.saathi.backend.model.WorkerProfile;
import com.saathi.backend.repository.BookingRepository;
import com.saathi.backend.repository.UserRepository;
import com.saathi.backend.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;

    // ─── STEP 1: GET AVAILABLE WORKERS ────────────────
    // Customer searches by skill + city
    // Returns list of workers with price + rating to choose from
    public List<WorkerSummary> getAvailableWorkers(String skill, String city) {

        List<WorkerProfile> workers;

        if (city != null && !city.isBlank()) {
            workers = workerRepository.findBySkillAndCity(skill, city);
        } else {
            workers = workerRepository.findBySkillsContaining(skill);
        }

        if (workers.isEmpty()) {
            throw new RuntimeException(
                    "No workers available for skill: " + skill
            );
        }

        // Map to summary — sorted by rating descending
        return workers.stream()
                .sorted((a, b) -> Double.compare(b.getRating(), a.getRating()))
                .map(w -> WorkerSummary.builder()
                        .workerId(w.getId())
                        .userId(w.getUser().getId())
                        .name(w.getUser().getName())
                        .skills(w.getSkills())
                        .city(w.getCity())
                        .rating(w.getRating())
                        .jobsCompleted(w.getJobsCompleted())
                        .pricePerHour(w.getPricePerHour())
                        .isVerified(Boolean.TRUE.equals(w.getIsVerified()))  // NEW
                        .build()
                )
                .collect(Collectors.toList());
    }

    // ─── STEP 2: CONFIRM BOOKING ──────────────────────
    // Customer picks a worker and confirms
    public BookingResponse confirmBooking(
            ConfirmBookingRequest req,
            String customerEmail
    ) {
        // 1. Get customer
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // 2. Check if customer already has an active booking
        // Prevents duplicate bookings
        boolean hasActiveBooking = bookingRepository
                .existsByCustomerAndStatusIn(
                        customer,
                        List.of(
                                Booking.Status.MATCHED,
                                Booking.Status.ACCEPTED,
                                Booking.Status.IN_PROGRESS
                        )
                );

        if (hasActiveBooking) {
            throw new RuntimeException(
                    "You already have an active booking. " +
                            "Complete or cancel it before creating a new one."
            );
        }

        // 3. Get chosen worker
        WorkerProfile worker = workerRepository.findById(req.getWorkerId())
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        if (!worker.getIsAvailable()) {
            throw new RuntimeException("Worker is not available");
        }

        // 4. Create booking
        Booking booking = Booking.builder()
                .customer(customer)
                .worker(worker)
                .skillNeeded(req.getSkillNeeded())
                .description(req.getDescription())
                .status(Booking.Status.MATCHED)
                .createdAt(LocalDateTime.now())
                .build();

        bookingRepository.save(booking);

        return mapToResponse(booking);
    }

    // ─── GET MY BOOKINGS (customer) ───────────────────
    public List<BookingResponse> getMyBookings(String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return bookingRepository.findByCustomer(customer)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── GET MY JOBS (worker) ─────────────────────────
    public List<BookingResponse> getMyJobs(String workerEmail) {
        return bookingRepository.findByWorker_User_Email(workerEmail)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── ACCEPT BOOKING (worker) ──────────────────────
    public BookingResponse acceptBooking(Long bookingId, String workerEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getWorker().getUser().getEmail().equals(workerEmail)) {
            throw new RuntimeException("You are not assigned to this booking");
        }

        booking.setStatus(Booking.Status.ACCEPTED);
        bookingRepository.save(booking);

        return mapToResponse(booking);
    }

    // ─── COMPLETE BOOKING ─────────────────────────────
    public BookingResponse completeBooking(Long bookingId, String workerEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getWorker().getUser().getEmail().equals(workerEmail)) {
            throw new RuntimeException("You are not assigned to this booking");
        }

        booking.setStatus(Booking.Status.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());

        WorkerProfile worker = booking.getWorker();
        worker.setJobsCompleted(worker.getJobsCompleted() + 1);
        workerRepository.save(worker);

        bookingRepository.save(booking);

        return mapToResponse(booking);
    }

    // ─── CANCEL BOOKING ───────────────────────────────
    public BookingResponse cancelBooking(Long bookingId, String customerEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("You are not the customer for this booking");
        }

        if (booking.getStatus() == Booking.Status.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed booking");
        }

        booking.setStatus(Booking.Status.CANCELLED);
        bookingRepository.save(booking);

        return mapToResponse(booking);
    }

    // ─── MAP TO RESPONSE ──────────────────────────────
    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .skillNeeded(booking.getSkillNeeded())
                .description(booking.getDescription())
                .status(booking.getStatus().name())
                .customerName(booking.getCustomer().getName())
                .customerEmail(booking.getCustomer().getEmail())
                .workerName(booking.getWorker() != null
                        ? booking.getWorker().getUser().getName() : null)
                .workerSkills(booking.getWorker() != null
                        ? booking.getWorker().getSkills() : null)
                .workerCity(booking.getWorker() != null
                        ? booking.getWorker().getCity() : null)
                .workerRating(booking.getWorker() != null
                        ? booking.getWorker().getRating() : null)
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
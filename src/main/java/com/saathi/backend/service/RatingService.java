package com.saathi.backend.service;

import com.saathi.backend.dto.RatingRequest;
import com.saathi.backend.dto.RatingResponse;
import com.saathi.backend.entity.Booking;
import com.saathi.backend.entity.Rating;
import com.saathi.backend.model.WorkerProfile;
import com.saathi.backend.repository.BookingRepository;
import com.saathi.backend.repository.RatingRepository;
import com.saathi.backend.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final BookingRepository bookingRepository;
    private final WorkerRepository workerRepository;

    // ─── SUBMIT RATING ────────────────────────────────
    public RatingResponse submitRating(RatingRequest req, String customerEmail) {

        // 1. Get booking
        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 2. Only the customer of this booking can rate
        if (!booking.getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("You can only rate your own bookings");
        }

        // 3. Can only rate completed bookings
        if (booking.getStatus() != Booking.Status.COMPLETED) {
            throw new RuntimeException(
                    "You can only rate a completed booking"
            );
        }

        // 4. Check not already rated
        if (ratingRepository.existsByBookingId(req.getBookingId())) {
            throw new RuntimeException("You have already rated this booking");
        }

        // 5. Validate score
        if (req.getScore() < 1 || req.getScore() > 5) {
            throw new RuntimeException("Score must be between 1 and 5");
        }

        // 6. Save rating
        Rating rating = Rating.builder()
                .booking(booking)
                .score(req.getScore())
                .comment(req.getComment())
                .ratedAt(LocalDateTime.now())
                .build();

        ratingRepository.save(rating);

        // 7. Update worker average rating
        updateWorkerRating(booking.getWorker(), req.getScore());

        return mapToResponse(rating);
    }

    // ─── GET RATING FOR A BOOKING ─────────────────────
    public RatingResponse getRatingByBooking(Long bookingId) {
        Rating rating = ratingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException(
                        "No rating found for this booking"
                ));
        return mapToResponse(rating);
    }

    // ─── UPDATE WORKER AVERAGE RATING ─────────────────
    private void updateWorkerRating(WorkerProfile worker, Integer newScore) {

        // formula: new average = (old rating * jobs + new score) / (jobs + 1)
        // but jobs count was already incremented in completeBooking
        // so we use jobsCompleted directly

        double currentRating = worker.getRating();
        int totalJobs = worker.getJobsCompleted();

        double newAverage;

        if (totalJobs <= 1) {
            // first rating — just use the score
            newAverage = newScore;
        } else {
            // recalculate rolling average
            newAverage = ((currentRating * (totalJobs - 1)) + newScore)
                    / totalJobs;
        }

        // round to 1 decimal place
        worker.setRating(Math.round(newAverage * 10.0) / 10.0);
        workerRepository.save(worker);
    }

    // ─── MAP TO RESPONSE ──────────────────────────────
    private RatingResponse mapToResponse(Rating rating) {
        return RatingResponse.builder()
                .ratingId(rating.getId())
                .bookingId(rating.getBooking().getId())
                .workerName(rating.getBooking().getWorker().getUser().getName())
                .customerName(rating.getBooking().getCustomer().getName())
                .score(rating.getScore())
                .comment(rating.getComment())
                .ratedAt(rating.getRatedAt())
                .build();
    }
}
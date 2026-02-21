package com.saathi.backend.service;

import com.saathi.backend.dto.EarningsResponse;
import com.saathi.backend.entity.Booking;
import com.saathi.backend.model.WorkerProfile;
import com.saathi.backend.repository.BookingRepository;
import com.saathi.backend.repository.RatingRepository;
import com.saathi.backend.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EarningsService {

    private final WorkerRepository workerRepository;
    private final BookingRepository bookingRepository;
    private final RatingRepository ratingRepository;

    public EarningsResponse getEarnings(String workerEmail) {

        // 1. Get worker profile
        WorkerProfile worker = workerRepository
                .findByUserEmail(workerEmail)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        // 2. Get all completed bookings for this worker
        List<Booking> completedBookings = bookingRepository
                .findByWorker_User_EmailAndStatus(
                        workerEmail,
                        Booking.Status.COMPLETED
                );

        // 3. Calculate total earnings
        Double pricePerHour = worker.getPricePerHour() != null
                ? worker.getPricePerHour() : 0.0;

        Double totalEarnings = completedBookings.size() * pricePerHour;

        // 4. Jobs this week
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        List<Booking> jobsThisWeek = completedBookings.stream()
                .filter(b -> b.getCompletedAt() != null &&
                        b.getCompletedAt().isAfter(oneWeekAgo))
                .collect(Collectors.toList());

        Double earningsThisWeek = jobsThisWeek.size() * pricePerHour;

        // 5. Recent jobs — last 5
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd MMM yyyy, hh:mm a");

        List<EarningsResponse.JobSummary> recentJobs = completedBookings
                .stream()
                .sorted((a, b) -> {
                    if (a.getCompletedAt() == null) return 1;
                    if (b.getCompletedAt() == null) return -1;
                    return b.getCompletedAt().compareTo(a.getCompletedAt());
                })
                .limit(5)
                .map(b -> EarningsResponse.JobSummary.builder()
                        .bookingId(b.getId())
                        .customerName(b.getCustomer().getName())
                        .skillNeeded(b.getSkillNeeded())
                        .completedAt(b.getCompletedAt() != null
                                ? b.getCompletedAt().format(formatter) : "N/A")
                        .earned(pricePerHour)
                        .build()
                )
                .collect(Collectors.toList());

        return EarningsResponse.builder()
                .workerName(worker.getUser().getName())
                .totalEarnings(totalEarnings)
                .totalJobsDone(completedBookings.size())
                .jobsThisWeek(jobsThisWeek.size())
                .earningsThisWeek(earningsThisWeek)
                .averageRating(worker.getRating())
                .pricePerHour(pricePerHour)
                .recentJobs(recentJobs)
                .build();
    }
}
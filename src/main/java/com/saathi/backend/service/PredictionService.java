package com.saathi.backend.service;

import com.saathi.backend.dto.PredictionResponse;
import com.saathi.backend.entity.Booking;
import com.saathi.backend.model.WorkerProfile;
import com.saathi.backend.repository.BookingRepository;
import com.saathi.backend.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final WorkerRepository workerRepository;
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public PredictionResponse predictIncome(String workerEmail) {

        // 1. Get worker
        WorkerProfile worker = workerRepository
                .findByUserEmail(workerEmail)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        Double pricePerHour = worker.getPricePerHour() != null
                ? worker.getPricePerHour() : 0.0;

        // 2. Get all completed bookings
        List<Booking> allCompleted = bookingRepository
                .findByWorker_User_EmailAndStatus(
                        workerEmail,
                        Booking.Status.COMPLETED
                );

        // 3. Calculate weekly stats
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusWeeks(1);
        LocalDateTime twoWeeksAgo = now.minusWeeks(2);

        // this week
        long jobsThisWeek = allCompleted.stream()
                .filter(b -> b.getCompletedAt() != null &&
                        b.getCompletedAt().isAfter(oneWeekAgo))
                .count();

        // last week
        long jobsLastWeek = allCompleted.stream()
                .filter(b -> b.getCompletedAt() != null &&
                        b.getCompletedAt().isAfter(twoWeeksAgo) &&
                        b.getCompletedAt().isBefore(oneWeekAgo))
                .count();

        Double currentWeekEarnings = jobsThisWeek * pricePerHour;
        Double lastWeekEarnings    = jobsLastWeek * pricePerHour;

        // average weekly earnings
        Double averageWeeklyEarnings = allCompleted.isEmpty() ? 0.0
                : (allCompleted.size() * pricePerHour) / Math.max(1,
                allCompleted.stream()
                        .filter(b -> b.getCompletedAt() != null)
                        .mapToLong(b -> {
                            long weeks = java.time.temporal.ChronoUnit.WEEKS
                                    .between(b.getCompletedAt(), now);
                            return Math.max(1, weeks);
                        })
                        .max().orElse(1)
        );

        // 4. Build prompt for Gemini
        String prompt = buildPredictionPrompt(
                worker.getUser().getName(),
                worker.getSkills(),
                worker.getRating(),
                allCompleted.size(),
                jobsThisWeek,
                jobsLastWeek,
                currentWeekEarnings,
                lastWeekEarnings,
                averageWeeklyEarnings,
                pricePerHour
        );

        // 5. Call Gemini
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        String url = geminiApiUrl + "?key=" + geminiApiKey;

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url, entity, Map.class
        );

        // 6. Parse Gemini response
        String geminiText = extractText(response.getBody());

        // 7. Parse prediction amount and tips from response
        Double predictedIncome = parsePredictedIncome(
                geminiText, averageWeeklyEarnings
        );

        return PredictionResponse.builder()
                .workerName(worker.getUser().getName())
                .predictedWeeklyIncome(predictedIncome)
                .analysis(geminiText)
                .currentWeekEarnings(currentWeekEarnings)
                .lastWeekEarnings(lastWeekEarnings)
                .averageWeeklyEarnings(averageWeeklyEarnings)
                .totalJobsDone(allCompleted.size())
                .averageRating(worker.getRating())
                .build();
    }

    // ─── BUILD PROMPT ─────────────────────────────────
    private String buildPredictionPrompt(
            String name,
            String skills,
            Double rating,
            int totalJobs,
            long jobsThisWeek,
            long jobsLastWeek,
            Double currentWeekEarnings,
            Double lastWeekEarnings,
            Double avgWeeklyEarnings,
            Double pricePerHour
    ) {
        return String.format(
                "You are an AI income advisor for blue-collar workers in India. " +
                        "Analyze this worker's data and predict their income for next week.\n\n" +
                        "Worker: %s\n" +
                        "Skills: %s\n" +
                        "Rating: %.1f / 5.0\n" +
                        "Price per job: ₹%.0f\n" +
                        "Total jobs completed: %d\n" +
                        "Jobs this week: %d (₹%.0f earned)\n" +
                        "Jobs last week: %d (₹%.0f earned)\n" +
                        "Average weekly earnings: ₹%.0f\n\n" +
                        "Based on this data:\n" +
                        "1. Predict their income for next week in Indian Rupees\n" +
                        "2. Give 2-3 specific actionable tips to increase their income\n" +
                        "3. Keep response simple and encouraging\n\n" +
                        "Format your response EXACTLY like this:\n" +
                        "PREDICTED: <amount in numbers only>\n" +
                        "ANALYSIS: <2-3 sentences about their performance trend>\n" +
                        "TIPS: <2-3 specific tips to earn more next week>",
                name, skills, rating, pricePerHour, totalJobs,
                jobsThisWeek, currentWeekEarnings,
                jobsLastWeek, lastWeekEarnings,
                avgWeeklyEarnings
        );
    }

    // ─── EXTRACT TEXT FROM GEMINI ─────────────────────
    private String extractText(Map responseBody) {
        try {
            List candidates = (List) responseBody.get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            Map part = (Map) parts.get(0);
            return part.get("text").toString().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response");
        }
    }

    // ─── PARSE PREDICTED INCOME FROM GEMINI TEXT ──────
    private Double parsePredictedIncome(
            String geminiText,
            Double fallback
    ) {
        try {
            // look for "PREDICTED: 1500" in response
            String[] lines = geminiText.split("\n");
            for (String line : lines) {
                if (line.startsWith("PREDICTED:")) {
                    String amount = line.replace("PREDICTED:", "")
                            .trim()
                            .replaceAll("[^0-9.]", "");
                    return Double.parseDouble(amount);
                }
            }
        } catch (Exception e) {
            // fallback to average if parsing fails
        }
        return fallback;
    }
}
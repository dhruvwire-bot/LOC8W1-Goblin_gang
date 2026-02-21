package com.saathi.backend.service;

import com.saathi.backend.dto.VerificationRequest;
import com.saathi.backend.dto.VerificationResponse;
import com.saathi.backend.model.WorkerProfile;
import com.saathi.backend.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final WorkerRepository workerRepository;

    // ─── WORKER SUBMITS AADHAAR ───────────────────────
    public VerificationResponse submitAadhaar(
            VerificationRequest req,
            String workerEmail
    ) {
        // 1. Get worker
        WorkerProfile worker = workerRepository
                .findByUserEmail(workerEmail)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        // 2. Validate aadhaar — must be 12 digits
        String aadhaar = req.getAadhaarNumber().replaceAll("\\s+", "");
        if (!aadhaar.matches("\\d{12}")) {
            throw new RuntimeException(
                    "Invalid Aadhaar number — must be 12 digits"
            );
        }

        // 3. Check not already verified
        if (Boolean.TRUE.equals(worker.getIsVerified())) {
            throw new RuntimeException("Worker is already verified");
        }

        // 4. Store only last 4 digits — never store full Aadhaar
        String lastFour = aadhaar.substring(8);

        worker.setAadhaarNumber(lastFour);
        worker.setVerificationStatus("PENDING");
        worker.setIsVerified(false);

        workerRepository.save(worker);

        return VerificationResponse.builder()
                .workerId(worker.getId())
                .workerName(worker.getUser().getName())
                .isVerified(false)
                .verificationStatus("PENDING")
                .aadhaarLastFour(lastFour)
                .message("Aadhaar submitted successfully. " +
                        "Verification is pending admin approval.")
                .build();
    }

    // ─── ADMIN APPROVES VERIFICATION ──────────────────
    public VerificationResponse approveVerification(Long workerId) {
        WorkerProfile worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        worker.setIsVerified(true);
        worker.setVerificationStatus("APPROVED");

        workerRepository.save(worker);

        return VerificationResponse.builder()
                .workerId(worker.getId())
                .workerName(worker.getUser().getName())
                .isVerified(true)
                .verificationStatus("APPROVED")
                .aadhaarLastFour(worker.getAadhaarNumber())
                .message("Worker verified successfully. Badge awarded.")
                .build();
    }

    // ─── ADMIN REJECTS VERIFICATION ───────────────────
    public VerificationResponse rejectVerification(Long workerId) {
        WorkerProfile worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        worker.setIsVerified(false);
        worker.setVerificationStatus("REJECTED");

        workerRepository.save(worker);

        return VerificationResponse.builder()
                .workerId(worker.getId())
                .workerName(worker.getUser().getName())
                .isVerified(false)
                .verificationStatus("REJECTED")
                .aadhaarLastFour(worker.getAadhaarNumber())
                .message("Verification rejected.")
                .build();
    }

    // ─── GET VERIFICATION STATUS ──────────────────────
    public VerificationResponse getStatus(String workerEmail) {
        WorkerProfile worker = workerRepository
                .findByUserEmail(workerEmail)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        return VerificationResponse.builder()
                .workerId(worker.getId())
                .workerName(worker.getUser().getName())
                .isVerified(Boolean.TRUE.equals(worker.getIsVerified()))
                .verificationStatus(worker.getVerificationStatus() != null
                        ? worker.getVerificationStatus() : "NOT_SUBMITTED")
                .aadhaarLastFour(worker.getAadhaarNumber())
                .message(Boolean.TRUE.equals(worker.getIsVerified())
                        ? "✅ Verified Worker"
                        : "❌ Not verified yet")
                .build();
    }
}
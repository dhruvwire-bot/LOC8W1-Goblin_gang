package com.saathi.backend.service;

import com.saathi.backend.dto.SkillExtractionResponse;
import com.saathi.backend.model.WorkerProfile;
import com.saathi.backend.repository.UserRepository;
import com.saathi.backend.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillExtractionService {

    private final GeminiService geminiService;
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;

    public SkillExtractionResponse extractAndSaveSkills(
            MultipartFile audioFile,
            String workerEmail
    ) {
        // 1. Get worker profile
        WorkerProfile worker = workerRepository
                .findByUserEmail(workerEmail)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        // 2. Call Gemini to extract skills from audio
        List<String> matchedSkills =
                geminiService.extractSkillsFromAudio(audioFile);

        if (matchedSkills.isEmpty()) {
            return SkillExtractionResponse.builder()
                    .detectedSkills(List.of())
                    .message("No recognizable skills found in audio. " +
                            "Please try again and mention your work clearly.")
                    .build();
        }

        // 3. Merge with existing skills — don't overwrite, add new ones
        String existingSkills = worker.getSkills();

        List<String> existingList = (existingSkills != null &&
                !existingSkills.isBlank())
                ? List.of(existingSkills.split(","))
                : List.of();

        // combine and deduplicate
        List<String> combined = new java.util.ArrayList<>(existingList);
        for (String skill : matchedSkills) {
            if (!combined.contains(skill)) {
                combined.add(skill);
            }
        }

        String updatedSkills = combined.stream()
                .collect(Collectors.joining(","));

        // 4. Save to DB
        worker.setSkills(updatedSkills);
        workerRepository.save(worker);

        return SkillExtractionResponse.builder()
                .detectedSkills(matchedSkills)
                .message("Skills detected and saved: " +
                        String.join(", ", matchedSkills))
                .build();
    }
}
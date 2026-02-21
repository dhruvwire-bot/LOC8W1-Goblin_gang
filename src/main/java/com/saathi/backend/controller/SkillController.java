package com.saathi.backend.controller;

import com.saathi.backend.dto.AuthResponse;
import com.saathi.backend.dto.RegisterRequest;
import com.saathi.backend.dto.SkillExtractionResponse;
import com.saathi.backend.service.AuthService;
import com.saathi.backend.service.SkillExtractionService;
import com.saathi.backend.service.VoiceRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SkillController {

    private final SkillExtractionService skillExtractionService;
    private final VoiceRegistrationService voiceRegistrationService;
    private final AuthService authService;

    // POST /api/skills/extract
    // Worker uploads audio → skills extracted → saved to DB
    @PostMapping("/extract")
    public ResponseEntity<SkillExtractionResponse> extractSkills(
            @RequestParam("audio") MultipartFile audioFile,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                skillExtractionService.extractAndSaveSkills(
                        audioFile,
                        userDetails.getUsername()
                )
        );
    }

    // GET /api/skills/valid — show all valid skill categories
    @GetMapping("/valid")
    public ResponseEntity<?> getValidSkills() {
        return ResponseEntity.ok(
                java.util.Arrays.stream(
                                com.saathi.backend.enums.Skill.values()
                        )
                        .map(Enum::name)
                        .collect(java.util.stream.Collectors.toList())
        );
    }

    // POST /api/skills/voice-register/parse
    // Send audio → Gemini extracts registration fields → returns preview (no DB write)
    // Use this to show the worker what was detected before confirming
    // Supports: Hindi, English, Telugu, Marathi, Gujarati
    // No auth required — worker is not registered yet
    @PostMapping("/voice-register/parse")
    public ResponseEntity<RegisterRequest> parseVoiceRegistration(
            @RequestParam("audio") MultipartFile audioFile
    ) {
        RegisterRequest extracted = voiceRegistrationService.extractRegistrationFromVoice(audioFile);
        return ResponseEntity.ok(extracted);
    }

    // POST /api/skills/voice-register/confirm
    // Send audio → Gemini extracts → registers the worker in DB
    // Returns same AuthResponse as normal register
    // No auth required — worker is not registered yet
    @PostMapping("/voice-register/confirm")
    public ResponseEntity<AuthResponse> confirmVoiceRegistration(
            @RequestParam("audio") MultipartFile audioFile
    ) {
        RegisterRequest extracted = voiceRegistrationService.extractRegistrationFromVoice(audioFile);
        AuthResponse response = authService.register(extracted);
        return ResponseEntity.status(201).body(response);
    }
}
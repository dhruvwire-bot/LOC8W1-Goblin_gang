package com.saathi.backend.controller;

import com.saathi.backend.dto.SkillExtractionResponse;
import com.saathi.backend.service.SkillExtractionService;
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
}
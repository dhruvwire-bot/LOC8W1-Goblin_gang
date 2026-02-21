package com.saathi.backend.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkillExtractionResponse {
    private List<String> detectedSkills;    // matched enums
    private List<String> unrecognizedSkills; // what Gemini found but didn't match
    private String rawGeminiResponse;        // for debugging
    private String message;
}
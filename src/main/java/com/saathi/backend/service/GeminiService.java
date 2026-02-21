package com.saathi.backend.service;

import com.saathi.backend.enums.Skill;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public static final List<String> VALID_SKILLS = Arrays.stream(Skill.values())
            .map(Enum::name)
            .collect(Collectors.toList());

    public List<String> extractSkillsFromAudio(MultipartFile audioFile) {
        try {
            // 1. Convert audio to base64
            String base64Audio = Base64.getEncoder()
                    .encodeToString(audioFile.getBytes());

            // 2. Detect mime type correctly
            String mimeType = detectMimeType(audioFile);

            // 3. Build request
            String skillList = String.join(", ", VALID_SKILLS);
            String prompt = String.format(
                    "Listen to this audio carefully. The person is describing " +
                            "what kind of work they do. Identify their skills and match " +
                            "them ONLY from this list: [%s]. " +
                            "Return ONLY the matched skill names in UPPERCASE separated " +
                            "by commas. Do not add any explanation. " +
                            "If nothing matches return NONE. " +
                            "Example output: PLUMBER,ELECTRICIAN",
                    skillList
            );

            // 4. Build body
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", base64Audio);

            Map<String, Object> audioPart = new HashMap<>();
            audioPart.put("inline_data", inlineData);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(audioPart, textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(content));

            // 5. Call Gemini
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(requestBody, headers);

            String url = geminiApiUrl + "?key=" + geminiApiKey;

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url, entity, Map.class
            );

            // 6. Parse response
            String geminiText = extractTextFromResponse(response.getBody());
            System.out.println("Gemini raw response: " + geminiText);

            // 7. Match to enums
            return matchSkillsToEnums(geminiText);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to process audio: " + e.getMessage()
            );
        }
    }

    // Detect correct mime type from file
    private String detectMimeType(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName != null) {
            if (originalName.endsWith(".mp3"))  return "audio/mp3";
            if (originalName.endsWith(".wav"))  return "audio/wav";
            if (originalName.endsWith(".m4a"))  return "audio/mp4";
            if (originalName.endsWith(".ogg"))  return "audio/ogg";
            if (originalName.endsWith(".flac")) return "audio/flac";
            if (originalName.endsWith(".aac"))  return "audio/aac";
            if (originalName.endsWith(".webm")) return "audio/webm";
        }
        // fallback
        return "audio/mp3";
    }

    private String extractTextFromResponse(Map responseBody) {
        try {
            List candidates = (List) responseBody.get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            Map part = (Map) parts.get(0);
            return part.get("text").toString().trim().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Gemini response: " + responseBody
            );
        }
    }

    public List<String> matchSkillsToEnums(String geminiText) {
        if (geminiText == null ||
                geminiText.isBlank() ||
                geminiText.contains("NONE")) {
            return new ArrayList<>();
        }

        return Arrays.stream(geminiText.split(","))
                .map(s -> s.trim().toUpperCase())
                .filter(VALID_SKILLS::contains)
                .distinct()
                .collect(Collectors.toList());
    }
}

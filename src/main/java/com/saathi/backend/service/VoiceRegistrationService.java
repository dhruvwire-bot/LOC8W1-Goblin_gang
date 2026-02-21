package com.saathi.backend.service;

import com.google.gson.Gson;
import com.saathi.backend.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class VoiceRegistrationService {

    private final RestTemplate restTemplate;
    // ← ObjectMapper REMOVED — using Gson instead, no bean needed

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public RegisterRequest extractRegistrationFromVoice(MultipartFile audioFile) {
        try {
            System.out.println("=== VOICE REGISTER HIT ===");
            System.out.println("File: " + audioFile.getOriginalFilename() + " | Size: " + audioFile.getSize());

            // 1. Convert audio to base64
            String base64Audio = Base64.getEncoder()
                    .encodeToString(audioFile.getBytes());

            String mimeType = detectMimeType(audioFile);
            System.out.println("Mime type: " + mimeType);

            // 2. Build prompt
            String prompt = """
                Listen to this audio carefully. A blue-collar worker is registering on a service platform.
                They may speak in Hindi, English, Telugu, Marathi, or Gujarati — or a mix.
                
                Extract the following details from what they say:
                - name: their full name
                - phone: their 10-digit mobile number (digits only, no spaces)
                - email: their email address (if mentioned, else generate one as name@saathi.com using their name in lowercase with no spaces)
                - password: if they mention one, else use "Saathi@1234" as default
                - language: detect which language they spoke most — return one of: HINDI, ENGLISH, TELUGU, MARATHI, GUJARATI
                - city: which city or town they are in
                - skills: what kind of work they do — match ONLY from this list: PLUMBER, ELECTRICIAN, CARPENTER, PAINTER, CLEANER, COOK, DRIVER, MASON
                - pricePerHour: how much they charge per job in rupees (number only) — if not mentioned use 300
                - role: always return "WORKER"
                
                Return ONLY a valid JSON object with exactly these fields, no explanation, no markdown:
                {
                  "name": "",
                  "phone": "",
                  "email": "",
                  "password": "",
                  "language": "",
                  "city": "",
                  "skills": "",
                  "pricePerHour": 300,
                  "role": "WORKER",
                  "latitude": 18.5204,
                  "longitude": 73.8567
                }
                
                Examples of what they might say:
                - Hindi: "Mera naam Ramesh hai, main Mumbai mein rehta hoon, plumber hoon, 9876543210"
                - Telugu: "Naa peru Suresh, Hyderabad lo untanu, electrician pani chestanu"
                - Marathi: "Maza nav Ganesh aahe, Pune madhye rahto, 8765432109, sutar kaam karto"
                """;

            // 3. Build Gemini request body
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

            // 4. Call Gemini
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            System.out.println("Gemini status: " + response.getStatusCode());

            // 5. Extract text from Gemini response
            String jsonText = extractTextFromResponse(response.getBody());
            System.out.println("Gemini raw: " + jsonText);

            // 6. Clean JSON — remove markdown code fences if present
            String cleanJson = jsonText
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            // 7. Parse with Gson — no bean needed, just new instance
            RegisterRequest req = new Gson().fromJson(cleanJson, RegisterRequest.class);

            // 8. Validate and fill defaults
            validateExtractedRequest(req);

            System.out.println("Parsed: " + req.getName() + " | " + req.getPhone() + " | " + req.getSkills());
            return req;

        } catch (Exception e) {
            System.out.println("VOICE ERROR: " + e.getMessage());
            throw new RuntimeException("Voice registration failed: " + e.getMessage());
        }
    }

    private void validateExtractedRequest(RegisterRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new RuntimeException("Could not detect your name. Please speak clearly and try again.");
        }
        if (req.getPhone() == null || req.getPhone().length() != 10) {
            throw new RuntimeException("Could not detect a valid 10-digit phone number. Please try again.");
        }
        if (req.getSkills() == null || req.getSkills().isBlank()) {
            throw new RuntimeException("Could not detect your skills. Please mention what work you do.");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            String emailBase = req.getName().toLowerCase()
                    .replaceAll("\\s+", "")
                    .replaceAll("[^a-z0-9]", "");
            req.setEmail(emailBase + "@saathi.com");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            req.setPassword("Saathi@1234");
        }
        if (req.getPricePerHour() == null) {
            req.setPricePerHour(300.0);
        }
        req.setRole("WORKER");
    }

    private String detectMimeType(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name != null) {
            if (name.endsWith(".mp3"))  return "audio/mp3";
            if (name.endsWith(".wav"))  return "audio/wav";
            if (name.endsWith(".m4a"))  return "audio/mp4";
            if (name.endsWith(".ogg"))  return "audio/ogg";
            if (name.endsWith(".webm")) return "audio/webm";
            if (name.endsWith(".aac"))  return "audio/aac";
            if (name.endsWith(".flac")) return "audio/flac";
        }
        return "audio/webm";
    }

    private String extractTextFromResponse(Map responseBody) {
        try {
            List candidates = (List) responseBody.get("candidates");
            Map candidate   = (Map) candidates.get(0);
            Map content     = (Map) candidate.get("content");
            List parts      = (List) content.get("parts");
            Map part        = (Map) parts.get(0);
            return part.get("text").toString().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + responseBody);
        }
    }
}
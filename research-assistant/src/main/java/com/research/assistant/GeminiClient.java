package com.research.assistant;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;

@Component
public class GeminiClient {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private static final String API_KEY = System.getenv("GEMINI_KEY");

    private static final RestTemplate restTemplate = new RestTemplate();

    public static String summarize(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = String.format("{\"contents\": [{\"parts\": [{\"text\": \"Provide clear and concise summary of the following text in a few sentences\\n%s\"}]}]}", text);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            String url = GEMINI_API_URL + "?key=" + API_KEY;
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            Map candidates = (Map) ((java.util.List<?>) response.getBody().get("candidates")).get(0);
            Map content = (Map) candidates.get("content");
            java.util.List<?> parts = (java.util.List<?>) content.get("parts");
            String summary = (String) ((Map<?, ?>) parts.get(0)).get("text");

            return summary;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while summarizing: " + e.getMessage();
        }
    }
}

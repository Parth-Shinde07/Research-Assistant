package com.research.assistant;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ResearchService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String getGeminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final TranslationService translationService;
    private final GeminiClient geminiClient;

    public ResearchService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,TranslationService translationService,GeminiClient geminiClient) {
        this.webClient = webClientBuilder.build();
        this.objectMapper=objectMapper;
        this.translationService=translationService;
        this.geminiClient=geminiClient;
    }

    public  String summarizeText(String inputText) {
        // Step 1: Detect language
        String lang = translationService.detectLanguage(inputText);

        String textForSummary = inputText;

        // Step 2: Translate to English if not already
        if (!lang.equals("en")) {
            textForSummary = translationService.translateToEnglish(inputText);
        }

        // Step 3: Summarize with Gemini
        String summary = GeminiClient.summarize(textForSummary);

        // Step 4: Translate summary back
        if (!lang.equals("en")) {
            summary = translationService.translateToOriginal(summary, lang);
        }

        return summary;
    }

    public String processContent(ResearchRequest request) {
        //Build the prompt
        String prompt= buildPrompt(request);
        //Query the AI model API
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text",prompt)
                        })
                }
        );

        String response= webClient.post()
                .uri(geminiApiUrl+getGeminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        //Pass the response
        //Return response
        
        return extractTextFromResponse(response);
    }

    private String extractTextFromResponse(String response) {
        try{
            GeminiResponse geminiResponse= objectMapper.readValue(response, GeminiResponse.class);
            if(geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()){
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if(firstCandidate.getContent().getParts() != null && !firstCandidate.getContent().getParts().isEmpty()){
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }
            return "No content found in response";
        } catch (Exception e) {
            return "Error Parsing:" + e.getMessage();
        }
    }

    private String buildPrompt(ResearchRequest request){
        StringBuilder prompt= new StringBuilder();
        switch (request.getOperation()){
            case "summarize":
                prompt.append("Provide clear and concise summary of the following text in a few sentences:\n\n");
                break;
            case "suggest":
                prompt.append("Based on the following content: suggest related topics and further reading. Format the response with clear headings and bullet points:\n\n");
                break;
            default:
                throw new IllegalArgumentException("Unknown Operation: " + request.getOperation() );
        }
        prompt.append(request.getContent());
        return prompt.toString();
    }
}

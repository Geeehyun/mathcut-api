package com.vision.mathcut.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class AISketchService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AISketchService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Object analyze(String imageDataUrl, String forcedShapeType, String userHint) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OPENAI_API_KEY is not configured.");
        }

        String systemPrompt = PromptBuilder.buildSystemPrompt(forcedShapeType, userHint);
        String userInstruction = PromptBuilder.buildUserInstruction(forcedShapeType, userHint);
        Map<String, Object> requestBody = buildOpenAIRequest(systemPrompt, userInstruction, imageDataUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    OPENAI_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if (body == null) throw new RuntimeException("Empty response from OpenAI.");

            JsonNode contentNode = body.at("/choices/0/message/content");
            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                throw new RuntimeException("AI response content was empty.");
            }

            String content = contentNode.asText();
            log.debug("[AISketch] raw response: {}", content);
            return objectMapper.readValue(content, Object.class);

        } catch (HttpClientErrorException e) {
            String msg = "OpenAI API error: " + e.getResponseBodyAsString();
            log.error(msg);
            throw new RuntimeException(msg);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI sketch analysis failed", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, Object> buildOpenAIRequest(String systemPrompt, String userInstruction, String imageDataUrl) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", 0);
        body.put("response_format", Map.of("type", "json_object"));

        Map<String, Object> systemMessage = Map.of("role", "system", "content", systemPrompt);

        List<Map<String, Object>> userContent = List.of(
                Map.of("type", "text", "text", userInstruction),
                Map.of("type", "image_url", "image_url", Map.of("url", imageDataUrl, "detail", "high"))
        );
        Map<String, Object> userMessage = Map.of("role", "user", "content", userContent);

        body.put("messages", List.of(systemMessage, userMessage));
        return body;
    }
}

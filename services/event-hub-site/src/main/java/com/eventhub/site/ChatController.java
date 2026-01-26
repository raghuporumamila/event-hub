package com.eventhub.site;

import com.eventhub.model.Source;
import com.eventhub.model.User;
import com.eventhub.site.config.ApiEndPointUri;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/genAIChat")
public class ChatController {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ApiEndPointUri apiEndPointUri;

    @Data
    public static class QueryRequest {
        private String user_id;
        private String question;
    }

    @Data // Generates getters and setters
    @NoArgsConstructor
    public static class Answer {
        private String query;
        private String result;
    }

    @Data // Generates getters and setters
    @NoArgsConstructor // Generates the empty constructor Jackson needs
    public static class QueryResponse {
        private Answer answer;
    }

    @PostMapping
    public String handleChat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        // Calls the AI model (OpenAI, Ollama, etc.)
        String url = apiEndPointUri.getGenAIAPIEndpoint() + "/query";
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setUser_id("test");
        queryRequest.setQuestion(userMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // Add this
        HttpEntity<QueryRequest> requestUpdate = new HttpEntity<>(queryRequest, headers);
        ResponseEntity<QueryResponse> response = restTemplate.exchange(url, HttpMethod.POST, requestUpdate , QueryResponse.class );

        return response.getBody().getAnswer().getResult();
    }
}

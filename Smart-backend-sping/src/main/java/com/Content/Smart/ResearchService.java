package com.Content.Smart;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.StringBuilder;
import java.util.Map;
@Service

public class ResearchService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ResearchService(WebClient.Builder webClientBuilder,ObjectMapper objectMapper){
        this.webClient=webClientBuilder.build();
        this.objectMapper=objectMapper;
    }
    public String processContent(ResearchRequest request){
        String Prompt= buildPrompt(request);
        Map<String,Object> requestBody= Map.of( "contents",new Object[]{
                Map.of("parts", new Object[]{
                        Map.of("text",Prompt)
                })
        });
        String response= webClient.post()
                .uri(geminiApiUrl+geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractTextFromResponse(response);

    }

    private String extractTextFromResponse(String response) {
        try{
            GeminiResponse geminiResponse=objectMapper.readValue(response,GeminiResponse.class);
            if(geminiResponse.getCandidates()!=null&& !geminiResponse.getCandidates().isEmpty()) {
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if (firstCandidate.getContent() != null && firstCandidate.getContent().getParts() != null &&
                        !firstCandidate.getContent().getParts().isEmpty()) {
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            } return " no content is Provided";
        } catch(Exception e){
            return "error parsing: "+ e.getMessage();
        }

    }

    private String buildPrompt(ResearchRequest request){
        StringBuilder Prompt = new StringBuilder();
        switch (request.getOperation()){
            case "summarize":
                Prompt.append("Provide a clear and concise Summary of the following text in a few sentence:\n\n");
                break;
            case "suggest"  :
                Prompt.append("Based on the following content:provide a clear and concise points:\n\n");
                break;
            default:
                throw new IllegalArgumentException("unknown operition"+request.getOperation());

        }
        Prompt.append(request.getContent());
        return Prompt.toString();
    }
}

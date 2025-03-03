package com.Content.Smart;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/Content")
@RestController
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ResearchController {
    private final ResearchService researchService;

    @PostMapping("/process")
    public ResponseEntity<String> processContent(@RequestBody ResearchRequest request){
        String result= researchService.processContent(request);
        return ResponseEntity.ok(result);
    }
}

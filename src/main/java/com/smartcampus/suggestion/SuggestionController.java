package com.smartcampus.suggestion;

import com.smartcampus.suggestion.SuggestionDtos.SubmissionRequest;
import com.smartcampus.suggestion.SuggestionDtos.SuggestionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {
    private final LocationSuggestionService suggestions;

    public SuggestionController(LocationSuggestionService suggestions) {
        this.suggestions = suggestions;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuggestionResponse submit(Authentication authentication,
                                    @Valid @RequestBody SubmissionRequest request) {
        return suggestions.submit(authentication.getName(), request);
    }

    @GetMapping("/mine")
    public List<SuggestionResponse> mine(Authentication authentication) {
        return suggestions.findMine(authentication.getName());
    }
}

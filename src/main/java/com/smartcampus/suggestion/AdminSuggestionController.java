package com.smartcampus.suggestion;

import com.smartcampus.suggestion.SuggestionDtos.ReviewRequest;
import com.smartcampus.suggestion.SuggestionDtos.SuggestionResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suggestions")
public class AdminSuggestionController {
    private final LocationSuggestionService suggestions;

    public AdminSuggestionController(LocationSuggestionService suggestions) {
        this.suggestions = suggestions;
    }

    @GetMapping
    public List<SuggestionResponse> pending() {
        return suggestions.findPending();
    }

    @PostMapping("/{id}/review")
    public SuggestionResponse review(@PathVariable Long id, Authentication authentication,
                                     @Valid @RequestBody ReviewRequest request) {
        return suggestions.review(id, authentication.getName(), request);
    }
}

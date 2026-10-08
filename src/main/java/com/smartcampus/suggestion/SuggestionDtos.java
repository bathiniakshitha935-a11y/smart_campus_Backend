package com.smartcampus.suggestion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class SuggestionDtos {
    private SuggestionDtos() {}

    public record SubmissionRequest(
            @NotNull SuggestionKind kind,
            Long existingLocationId,
            @NotBlank @Size(max = 255) String name,
            @NotBlank @Pattern(regexp = "Block|Facility|Entrance", message = "Type must be Block, Facility, or Entrance")
            String type,
            @Size(max = 255) String description
    ) {}

    public record ReviewRequest(
            @NotNull SuggestionStatus decision,
            @Size(max = 500) String reviewNotes
    ) {}

    public record SuggestionResponse(
            Long id,
            SuggestionKind kind,
            Long existingLocationId,
            String name,
            String type,
            String description,
            SuggestionStatus status,
            String reviewNotes,
            String submittedBy,
            String reviewedBy,
            Instant createdAt,
            Instant reviewedAt
    ) {
        public static SuggestionResponse from(LocationSuggestion suggestion) {
            return new SuggestionResponse(
                    suggestion.getId(), suggestion.getKind(), suggestion.getExistingLocationId(),
                    suggestion.getLocationName(), suggestion.getLocationType(), suggestion.getDescription(),
                    suggestion.getStatus(), suggestion.getReviewNotes(),
                    suggestion.getSubmittedBy().getEmail(),
                    suggestion.getReviewedBy() == null ? null : suggestion.getReviewedBy().getEmail(),
                    suggestion.getCreatedAt(), suggestion.getReviewedAt());
        }
    }
}

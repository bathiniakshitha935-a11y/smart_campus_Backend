package com.smartcampus.suggestion;

import com.smartcampus.auth.UserAccount;
import com.smartcampus.auth.UserAccountRepository;
import com.smartcampus.location.Location;
import com.smartcampus.location.LocationRepository;
import com.smartcampus.location.LocationRequest;
import com.smartcampus.location.LocationService;
import com.smartcampus.suggestion.SuggestionDtos.ReviewRequest;
import com.smartcampus.suggestion.SuggestionDtos.SubmissionRequest;
import com.smartcampus.suggestion.SuggestionDtos.SuggestionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LocationSuggestionService {
    private final LocationSuggestionRepository suggestions;
    private final UserAccountRepository users;
    private final LocationRepository locations;
    private final LocationService locationService;

    public LocationSuggestionService(LocationSuggestionRepository suggestions, UserAccountRepository users,
                                     LocationRepository locations, LocationService locationService) {
        this.suggestions = suggestions;
        this.users = users;
        this.locations = locations;
        this.locationService = locationService;
    }

    @Transactional
    public SuggestionResponse submit(String studentEmail, SubmissionRequest request) {
        UserAccount student = findUser(studentEmail);
        String name = request.name().trim();
        if (request.kind() == SuggestionKind.ADD) {
            if (request.existingLocationId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "An add suggestion must not include an existing location id");
            }
            if (locations.existsByNameIgnoreCase(name)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A location with this name already exists; submit an update suggestion instead");
            }
        } else {
            if (request.existingLocationId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "An update suggestion must identify the existing location");
            }
            if (!locations.existsById(request.existingLocationId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The location to update no longer exists");
            }
            if (locations.existsByNameIgnoreCaseAndIdNot(name, request.existingLocationId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Another location already uses this name");
            }
        }

        LocationSuggestion suggestion = new LocationSuggestion(request.kind(), request.existingLocationId(),
                name, request.type(), cleanDescription(request.description()), student);
        return SuggestionResponse.from(suggestions.save(suggestion));
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponse> findMine(String studentEmail) {
        UserAccount student = findUser(studentEmail);
        return suggestions.findBySubmittedBy_IdOrderByCreatedAtDesc(student.getId())
                .stream().map(SuggestionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponse> findPending() {
        return suggestions.findByStatusOrderByCreatedAtAsc(SuggestionStatus.PENDING)
                .stream().map(SuggestionResponse::from).toList();
    }

    @Transactional
    public SuggestionResponse review(Long suggestionId, String adminEmail, ReviewRequest request) {
        if (request.decision() != SuggestionStatus.APPROVED && request.decision() != SuggestionStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be APPROVED or REJECTED");
        }
        LocationSuggestion suggestion = suggestions.findById(suggestionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Suggestion not found"));
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This suggestion has already been reviewed");
        }

        UserAccount admin = findUser(adminEmail);
        if (request.decision() == SuggestionStatus.APPROVED) {
            LocationRequest officialData = new LocationRequest(suggestion.getLocationName(),
                    suggestion.getLocationType(), suggestion.getDescription());
            if (suggestion.getKind() == SuggestionKind.ADD) {
                locationService.create(officialData);
            } else {
                Long targetId = suggestion.getExistingLocationId();
                if (targetId == null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "The location for this update no longer exists");
                }
                locationService.update(targetId, officialData);
            }
        }

        suggestion.review(request.decision(), cleanDescription(request.reviewNotes()), admin);
        return SuggestionResponse.from(suggestion);
    }

    private UserAccount findUser(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
    }

    private String cleanDescription(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}

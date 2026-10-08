package com.smartcampus.suggestion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationSuggestionRepository extends JpaRepository<LocationSuggestion, Long> {
    List<LocationSuggestion> findBySubmittedBy_IdOrderByCreatedAtDesc(Long userId);
    List<LocationSuggestion> findByStatusOrderByCreatedAtAsc(SuggestionStatus status);
}

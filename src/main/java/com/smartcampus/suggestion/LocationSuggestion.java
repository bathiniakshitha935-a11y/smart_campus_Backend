package com.smartcampus.suggestion;

import com.smartcampus.auth.UserAccount;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "location_suggestions")
public class LocationSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SuggestionKind kind;

    @Column(name = "existing_location_id")
    private Long existingLocationId;

    @Column(name = "location_name", nullable = false, length = 255)
    private String locationName;

    @Column(name = "location_type", nullable = false, length = 255)
    private String locationType;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SuggestionStatus status;

    @Column(name = "review_notes", length = 500)
    private String reviewNotes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private UserAccount submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private UserAccount reviewedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected LocationSuggestion() {}

    public LocationSuggestion(SuggestionKind kind, Long existingLocationId, String locationName,
                              String locationType, String description, UserAccount submittedBy) {
        this.kind = kind;
        this.existingLocationId = existingLocationId;
        this.locationName = locationName;
        this.locationType = locationType;
        this.description = description;
        this.submittedBy = submittedBy;
        this.status = SuggestionStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void review(SuggestionStatus status, String notes, UserAccount reviewer) {
        this.status = status;
        this.reviewNotes = notes;
        this.reviewedBy = reviewer;
        this.reviewedAt = Instant.now();
    }

    public Long getId() { return id; }
    public SuggestionKind getKind() { return kind; }
    public Long getExistingLocationId() { return existingLocationId; }
    public String getLocationName() { return locationName; }
    public String getLocationType() { return locationType; }
    public String getDescription() { return description; }
    public SuggestionStatus getStatus() { return status; }
    public String getReviewNotes() { return reviewNotes; }
    public UserAccount getSubmittedBy() { return submittedBy; }
    public UserAccount getReviewedBy() { return reviewedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReviewedAt() { return reviewedAt; }
}

package com.smartcampus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.auth.Role;
import com.smartcampus.auth.UserAccount;
import com.smartcampus.auth.UserAccountRepository;
import com.smartcampus.location.Location;
import com.smartcampus.location.LocationRepository;
import com.smartcampus.suggestion.LocationSuggestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:campus_auth_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.h2.console.enabled=false"
})
class AuthAuthorizationIntegrationTest {
    private static final String ADMIN_EMAIL = "admin@campus.test";
    private static final String ADMIN_PASSWORD = "AdminTestPassword123";
    private static final String STUDENT_PASSWORD = "StudentTestPassword123";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserAccountRepository users;
    @Autowired private LocationSuggestionRepository suggestions;
    @Autowired private LocationRepository locations;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void resetData() {
        suggestions.deleteAll();
        locations.deleteAll();
        users.deleteAll();
        users.save(new UserAccount(ADMIN_EMAIL, passwordEncoder.encode(ADMIN_PASSWORD), Role.ADMIN));
    }

    @Test
    void anonymousAndStudentCannotModifyOfficialLocations() throws Exception {
        mvc.perform(post("/api/locations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Unauthorized Block\",\"type\":\"Block\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/locations/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Unauthorized Block\",\"type\":\"Block\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/locations/1")).andExpect(status().isUnauthorized());

        String studentToken = registerStudent("student-one@campus.test");
        mvc.perform(post("/api/locations").header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Student Block\",\"type\":\"Block\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/locations/1").header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Student Block\",\"type\":\"Block\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/locations/1").header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/admin/suggestions").header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateUpdateAndDeleteOfficialLocation() throws Exception {
        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
        String created = mvc.perform(post("/api/locations").header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Admin Block\",\"type\":\"Block\",\"description\":\"North campus\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Admin Block")))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mvc.perform(put("/api/locations/{id}", id).header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Admin Block Updated\",\"type\":\"Block\",\"description\":\"Updated details\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Admin Block Updated")));

        mvc.perform(delete("/api/locations/{id}", id).header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void studentCanSubmitAndTrackSuggestionsAndAdminCanApproveOrReject() throws Exception {
        String studentToken = registerStudent("student-two@campus.test");
        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
        Location existing = locations.save(new Location("Existing Block", "Block", "Original description"));

        long approvalId = submitSuggestion(studentToken, "ADD", null, "Library Annex");
        mvc.perform(get("/api/suggestions/mine").header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("PENDING")));
        mvc.perform(get("/api/admin/suggestions").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) approvalId)));

        review(adminToken, approvalId, "APPROVED", "Verified on campus");
        org.junit.jupiter.api.Assertions.assertTrue(locations.existsByNameIgnoreCase("Library Annex"));
        mvc.perform(get("/api/suggestions/mine").header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("APPROVED")));

        long updateId = submitSuggestion(studentToken, "UPDATE", existing.getId(), "Existing Block Updated");
        review(adminToken, updateId, "APPROVED", "Correction confirmed");
        org.junit.jupiter.api.Assertions.assertTrue(locations.existsByNameIgnoreCase("Existing Block Updated"));

        long rejectionId = submitSuggestion(studentToken, "ADD", null, "Unverified Building");
        review(adminToken, rejectionId, "REJECTED", "Please provide a campus location");
        org.junit.jupiter.api.Assertions.assertFalse(locations.existsByNameIgnoreCase("Unverified Building"));
        mvc.perform(get("/api/suggestions/mine").header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("REJECTED")));
    }

    private String registerStudent(String email) throws Exception {
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, STUDENT_PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private String login(String email, String password) throws Exception {
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private long submitSuggestion(String token, String kind, Long locationId, String name) throws Exception {
        String locationIdJson = locationId == null ? "null" : locationId.toString();
        String response = mvc.perform(post("/api/suggestions").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kind\":\"" + kind + "\",\"existingLocationId\":" + locationIdJson
                                + ",\"name\":\"" + name + "\",\"type\":\"Block\",\"description\":\"Student suggestion\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void review(String token, long id, String decision, String notes) throws Exception {
        mvc.perform(post("/api/admin/suggestions/{id}/review", id).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"" + decision + "\",\"reviewNotes\":\"" + notes + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(decision)));
    }

    private String credentials(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}

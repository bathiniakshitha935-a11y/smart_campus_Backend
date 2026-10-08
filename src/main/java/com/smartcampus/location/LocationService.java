package com.smartcampus.location;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LocationService {
    private final LocationRepository repository;

    public LocationService(LocationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Location> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Location findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }

    @Transactional
    public Location create(LocationRequest input) {
        String name = input.name().trim();
        ensureNameAvailable(name, null);
        return repository.save(new Location(name, input.type(), cleanDescription(input.description())));
    }

    @Transactional
    public Location update(Long id, LocationRequest input) {
        Location location = findById(id);
        String name = input.name().trim();
        ensureNameAvailable(name, id);
        location.setName(name);
        location.setType(input.type());
        location.setDescription(cleanDescription(input.description()));
        return repository.save(location);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found");
        }
        repository.deleteById(id);
    }

    private void ensureNameAvailable(String name, Long excludedId) {
        boolean duplicate = excludedId == null
                ? repository.existsByNameIgnoreCase(name)
                : repository.existsByNameIgnoreCaseAndIdNot(name, excludedId);
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A location with this name already exists");
        }
    }

    private String cleanDescription(String description) {
        if (description == null) return null;
        String cleaned = description.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}

package com.smartcampus.location;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "${app.frontend-url}")
public class LocationController {

    private final LocationRepository repository;

    public LocationController(LocationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Location> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Location getById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Location create(@RequestBody Location location) {
        return repository.save(location);
    }

    @PutMapping("/{id}")
    public Location update(@PathVariable Long id, @RequestBody Location input) {
        Location location = getById(id);
        location.setName(input.getName());
        location.setType(input.getType());
        location.setDescription(input.getDescription());
        return repository.save(location);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}

package com.smartcampus.location;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "${app.frontend-url}")
public class LocationController {

    private final LocationService locations;

    public LocationController(LocationService locations) {
        this.locations = locations;
    }

    @GetMapping
    public List<Location> getAll() {
        return locations.findAll();
    }

    @GetMapping("/{id}")
    public Location getById(@PathVariable Long id) {
        return locations.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Location create(@Valid @RequestBody LocationRequest location) {
        return locations.create(location);
    }

    @PutMapping("/{id}")
    public Location update(@PathVariable Long id, @Valid @RequestBody LocationRequest input) {
        return locations.update(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        locations.delete(id);
    }
}

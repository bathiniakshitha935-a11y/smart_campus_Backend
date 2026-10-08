package com.smartcampus;

import com.smartcampus.location.Location;
import com.smartcampus.location.LocationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedLocations(LocationRepository repository) {
        return args -> {
            if (repository.count() > 0) return;

            repository.save(new Location("Main Entrance", "Entrance", "Main entrance opposite A Block."));
            repository.save(new Location("A Block", "Block", "Straight ahead from the main entrance."));
            repository.save(new Location("B Block", "Block", "Reach B Block by taking the left route from A Block."));
            repository.save(new Location("C Block", "Block", "Left from B Block; near the ground and football court."));
            repository.save(new Location("D Block", "Block", "Continue beyond C Block toward D Block."));
            repository.save(new Location("E Block", "Block", "On the E–F–G road/row."));
            repository.save(new Location("F Block", "Block", "On the E–F–G road/row."));
            repository.save(new Location("G Block", "Block", "On the E–F–G road/row."));
            repository.save(new Location("H Block", "Block", "Beside the side road near D Block."));
            repository.save(new Location("I Block", "Block", "Reached by turning toward I Block from the E–F–G route."));
            repository.save(new Location("ABJ Auditorium", "Facility", "Left at the split beyond C Block."));
            repository.save(new Location("Football Court", "Facility", "Near C Block."));
            repository.save(new Location("Botanical Park", "Facility", "Straight from the H/D side road."));
            repository.save(new Location("Sports Block", "Facility", "Beside Botanical Park."));
            repository.save(new Location("Maker Lab", "Facility", "Opposite H Block."));
            repository.save(new Location("Fruit Juice Shop", "Facility", "Opposite H Block."));
            repository.save(new Location("Parking", "Facility", "Near ABJ Auditorium and H Block."));
        };
    }
}

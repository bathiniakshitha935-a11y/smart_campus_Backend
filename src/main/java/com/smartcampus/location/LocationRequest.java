package com.smartcampus.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LocationRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Pattern(regexp = "Block|Facility|Entrance", message = "Type must be Block, Facility, or Entrance")
        String type,
        @Size(max = 255) String description
) {}

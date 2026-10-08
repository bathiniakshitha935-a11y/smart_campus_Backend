package com.smartcampus.location;

import jakarta.persistence.*;

@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String type;

    private String description;

    public Location() {}

    public Location(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
}

package com.swer313.projectstep1.catalog.amenities;
import com.swer313.projectstep1.catalog.amenities.Amenity;
import java.time.LocalDateTime;
public class AmenityResponseDTO {

    private final Long id;
    private final String name;
    private final String description;
    private final Amenity.AmenityCategory category;
    private final boolean premium;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AmenityResponseDTO(
            Long id,
            String name,
            String description,
            Amenity.AmenityCategory category,
            boolean premium,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.premium = premium;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Amenity.AmenityCategory getCategory() { return category; }
    public boolean isPremium() { return premium; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
package com.swer313.projectstep1.catalog.amenities;

import org.springframework.stereotype.Component;

@Component
public class AmenityMapper {

    /**
     * Create new Entity from Request DTO
     */
    public Amenity toEntity(AmenityRequestDTO dto) {
        if (dto == null) return null;

        Amenity amenity = new Amenity(
                dto.getName(),
                dto.getDescription(),
                dto.getCategory()
        );

        // override defaults only if provided
        if (dto.getPremium() != null) {
            amenity.setPremium(dto.getPremium());
        }

        if (dto.getActive() != null) {
            amenity.setActive(dto.getActive());
        }
        return amenity;
    }

    /**
     * Update existing Entity from Request DTO
     * Used in PUT operations
     */
    public void updateEntity(Amenity entity, AmenityRequestDTO dto) {
        if (entity == null || dto == null) return;

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());

        // only update flags if provided (important!)
        if (dto.getPremium() != null) {
            entity.setPremium(dto.getPremium());
        }

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
    }

    /**
     * Convert Entity to Response DTO
     */
    public AmenityResponseDTO toDto(Amenity entity) {
        if (entity == null) return null;

        return new AmenityResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory(),
                entity.isPremium(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
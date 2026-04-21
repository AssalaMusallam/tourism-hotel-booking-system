package com.swer313.projectstep1.catalog.hotel;

import com.swer313.projectstep1.catalog.amenities.Amenity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HotelMapper {

    private HotelMapper() {}

    public static Hotel toEntity(HotelRequestDto dto) {
        Hotel h = new Hotel();
        updateEntity(h, dto);
        return h;
    }

    public static void updateEntity(Hotel h, HotelRequestDto dto) {
        h.setName(dto.getName() != null ? dto.getName().trim() : null);
        h.setAddress(dto.getAddress() != null ? dto.getAddress().trim() : null);
        h.setCity(dto.getCity() != null ? dto.getCity().trim() : null);
        h.setCountry(dto.getCountry() != null ? dto.getCountry().trim() : null);
        h.setDescription(dto.getDescription());
        h.setPhoneNumber(dto.getPhoneNumber());
        h.setEmail(dto.getEmail());
        h.setWebsiteUrl(dto.getWebsiteUrl());
        h.setRating(dto.getRating());
        h.setLatitude(dto.getLatitude());
        h.setLongitude(dto.getLongitude());
        h.setCheckInTime(dto.getCheckInTime());
        h.setCheckOutTime(dto.getCheckOutTime());
        h.setPolicies(dto.getPolicies());
        h.setCancellationPolicySummary(dto.getCancellationPolicySummary());
    }

    public static HotelResponseDto toDto(Hotel h) {
        HotelResponseDto dto = new HotelResponseDto();
        dto.setId(h.getId());
        dto.setName(h.getName());
        dto.setAddress(h.getAddress());
        dto.setDescription(h.getDescription());
        dto.setPhoneNumber(h.getPhoneNumber());
        dto.setEmail(h.getEmail());
        dto.setWebsiteUrl(h.getWebsiteUrl());
        dto.setRating(h.getRating());
        dto.setCity(h.getCity());
        dto.setCountry(h.getCountry());
        dto.setLatitude(h.getLatitude());
        dto.setLongitude(h.getLongitude());
        dto.setCheckInTime(h.getCheckInTime());
        dto.setCheckOutTime(h.getCheckOutTime());
        dto.setPolicies(h.getPolicies());
        dto.setCancellationPolicySummary(h.getCancellationPolicySummary());
        dto.setStatus(h.getStatus());

        Set<String> amenityNames = h.getAmenities() == null ? Set.of() :
                h.getAmenities().stream()
                        .map(Amenity::getName)
                        .collect(Collectors.toSet());
        dto.setAmenityNames(amenityNames);

        List<HotelImageResponseDto> images = h.getImages() == null ? List.of() :
                h.getImages().stream().map(HotelMapper::toImageDto).toList();
        dto.setImages(images);

        return dto;
    }

    public static HotelImageResponseDto toImageDto(HotelImage image) {
        HotelImageResponseDto dto = new HotelImageResponseDto();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setFileName(image.getFileName());
        return dto;
    }
}
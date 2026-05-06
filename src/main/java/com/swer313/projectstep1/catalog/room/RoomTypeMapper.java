package com.swer313.projectstep1.catalog.room;

import com.swer313.projectstep1.catalog.hotel.Hotel;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RoomTypeMapper {

    private RoomTypeMapper() {}

    public static RoomType toEntity(RoomTypeRequestDto dto, Hotel hotel) {
        if (dto == null) return null;

        RoomType rt = new RoomType();
        rt.setHotel(hotel);
        fill(rt, dto);
        return rt;
    }

    // overload جديد حتى يشتغل مع service الحالي
    public static RoomType toEntity(RoomTypeRequestDto dto) {
        if (dto == null) return null;

        RoomType rt = new RoomType();
        Hotel hotel = new Hotel();
        hotel.setId(dto.getHotelId());
        rt.setHotel(hotel);
        fill(rt, dto);
        return rt;
    }

    public static void apply(RoomType existing, RoomTypeRequestDto dto) {
        fill(existing, dto);
    }

    private static void fill(RoomType target, RoomTypeRequestDto dto) {
        target.setName(dto.getName().trim());
        target.setCapacity(dto.getCapacity());
        target.setBedType(dto.getBedType());
        target.setBedCount(dto.getBedCount());
        target.setMaxAdults(dto.getMaxAdults());
        target.setMaxChildren(dto.getMaxChildren());
        target.setBasePrice(dto.getBasePrice());
        target.setTotalUnits(dto.getTotalUnits());
        target.setDescription(dto.getDescription());
        target.setPolicies(dto.getPolicies());


        if (dto.getStatus() != null) {
            target.setStatus(dto.getStatus());
        }
    }

    public static RoomTypeResponseDto toDto(RoomType rt) {
        if (rt == null) return null;

        Set<Long> amenityIds = rt.getAmenities() == null
                ? Set.of()
                : rt.getAmenities().stream()
                .map(a -> a.getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<RoomTypeImageResponseDto> images = rt.getImages() == null
                ? List.of()
                : rt.getImages().stream().map(RoomTypeMapper::toImageDto).toList();

        return new RoomTypeResponseDto(
                rt.getId(),
                rt.getHotelId(),
                rt.getName(),
                rt.getCapacity(),
                rt.getBedType(),
                rt.getBedCount(),
                rt.getMaxAdults(),
                rt.getMaxChildren(),
                rt.getBasePrice(),
                rt.getTotalUnits(),
                rt.getDescription(),
                rt.getPolicies(),
                images,
                rt.getStatus(),
                amenityIds
        );
    }

    private static RoomTypeImageResponseDto toImageDto(RoomTypeImage image) {
        RoomTypeImageResponseDto dto = new RoomTypeImageResponseDto();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setFileName(image.getFileName());
        return dto;
    }

    private static List<String> safeImages(List<String> images) {
        if (images == null) return List.of();
        return images.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .toList();
    }
}

package com.swer313.projectstep1.catalog.room;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Response DTO – يرجع id و hotelId دايماً للعميل.
 */
public class RoomTypeResponseDto {

    private final Long id;
    private final Long hotelId;
    private final String name;
    private final int capacity;
    private final BedType bedType;
    private final int bedCount;
    private final int maxAdults;
    private final int maxChildren;
    private final BigDecimal basePrice;
    private final int totalUnits;
    private final String description;
    private final String policies;
    private final List<RoomTypeImageResponseDto> images;
    private final RoomTypeStatus status;
    private final Set<Long> amenityIds;

    public RoomTypeResponseDto(
            Long id,
            Long hotelId,
            String name,
            int capacity,
            BedType bedType,
            int bedCount,
            int maxAdults,
            int maxChildren,
            BigDecimal basePrice,
            int totalUnits,
            String description,
            String policies,
            RoomTypeStatus status,
            Set<Long> amenityIds
    ) {
        this(id, hotelId, name, capacity, bedType, bedCount, maxAdults, maxChildren, basePrice, totalUnits, description, policies, List.of(), status, amenityIds);
    }

    public RoomTypeResponseDto(
            Long id,
            Long hotelId,
            String name,
            int capacity,
            BedType bedType,
            int bedCount,
            int maxAdults,
            int maxChildren,
            BigDecimal basePrice,
            int totalUnits,
            String description,
            String policies,
            List<RoomTypeImageResponseDto> images,
            RoomTypeStatus status,
            Set<Long> amenityIds
    ) {
        this.id = id;
        this.hotelId = hotelId;
        this.name = name;
        this.capacity = capacity;
        this.bedType = bedType;
        this.bedCount = bedCount;
        this.maxAdults = maxAdults;
        this.maxChildren = maxChildren;
        this.basePrice = basePrice;
        this.totalUnits = totalUnits;
        this.description = description;
        this.policies = policies;
        this.images = images != null ? images : List.of();
        this.status = status;
        this.amenityIds = amenityIds;
    }

    public Long getId()             { return id; }
    public Long getHotelId()        { return hotelId; }
    public String getName()         { return name; }
    public int getCapacity()        { return capacity; }
    public BedType getBedType()     { return bedType; }
    public int getBedCount()        { return bedCount; }
    public int getMaxAdults()       { return maxAdults; }
    public int getMaxChildren()     { return maxChildren; }
    public BigDecimal getBasePrice(){ return basePrice; }
    public int getTotalUnits()      { return totalUnits; }
    public String getDescription()  { return description; }
    public String getPolicies()     { return policies; }
   // public List<String> getImagesUrl() { return imagesUrl; }
    public List<RoomTypeImageResponseDto> getImages() { return images; }
    public RoomTypeStatus getStatus() { return status; }
    public Set<Long> getAmenityIds() { return amenityIds; }
}

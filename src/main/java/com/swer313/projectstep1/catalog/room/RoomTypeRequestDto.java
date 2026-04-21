package com.swer313.projectstep1.catalog.room;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class RoomTypeRequestDto {

    @NotNull(message = "hotelId is required")
    private Long hotelId;

    @NotBlank(message = "name is required")
    @Size(max = 80, message = "name must be <= 80 chars")
    private String name;

    @Min(value = 1, message = "capacity must be >= 1")
    private int capacity;

    @NotNull(message = "bedType is required")
    private BedType bedType;

    @Min(value = 1, message = "bedCount must be >= 1")
    private int bedCount;

    @Min(value = 1, message = "maxAdults must be >= 1")
    private int maxAdults;

    @Min(value = 0, message = "maxChildren must be >= 0")
    private int maxChildren;

    @NotNull(message = "basePrice is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "basePrice must be >= 0")
    private BigDecimal basePrice;

    @Min(value = 0, message = "totalUnits must be >= 0")
    private int totalUnits;

    @Size(max = 2000, message = "description too long")
    private String description;

    @Size(max = 4000, message = "policies too long")
    private String policies;

    private List<@NotBlank(message = "image url must not be blank") @Size(max = 500) String> imagesUrl;

    private RoomTypeStatus status;

    private Set<Long> amenityIds;

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public BedType getBedType() { return bedType; }
    public void setBedType(BedType bedType) { this.bedType = bedType; }

    public int getBedCount() { return bedCount; }
    public void setBedCount(int bedCount) { this.bedCount = bedCount; }

    public int getMaxAdults() { return maxAdults; }
    public void setMaxAdults(int maxAdults) { this.maxAdults = maxAdults; }

    public int getMaxChildren() { return maxChildren; }
    public void setMaxChildren(int maxChildren) { this.maxChildren = maxChildren; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public int getTotalUnits() { return totalUnits; }
    public void setTotalUnits(int totalUnits) { this.totalUnits = totalUnits; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPolicies() { return policies; }
    public void setPolicies(String policies) { this.policies = policies; }

    public List<String> getImagesUrl() { return imagesUrl; }
    public void setImagesUrl(List<String> imagesUrl) { this.imagesUrl = imagesUrl; }

    public RoomTypeStatus getStatus() { return status; }
    public void setStatus(RoomTypeStatus status) { this.status = status; }

    public Set<Long> getAmenityIds() { return amenityIds; }
    public void setAmenityIds(Set<Long> amenityIds) { this.amenityIds = amenityIds; }
}
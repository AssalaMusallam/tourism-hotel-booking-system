package com.swer313.projectstep1.catalog.room;

import com.swer313.projectstep1.catalog.amenities.Amenity;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "room_types")
public class RoomType {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(STRING)
    @Column(name = "bed_type", nullable = false, length = 10)
    private BedType bedType;

    @Column(name = "bed_count", nullable = false)
    private int bedCount;

    @Column(name = "max_adults", nullable = false)
    private int maxAdults;

    @Column(name = "max_children", nullable = false)
    private int maxChildren;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "total_units", nullable = false)
    private int totalUnits;

    @Column(length = 2000)
    private String description;

    @Column(length = 4000)
    private String policies;
    @OneToMany(mappedBy = "roomType",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<RoomTypeImage> images = new ArrayList<>();
/*
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "room_type_images",
            joinColumns = @JoinColumn(name = "room_type_id")
    )
    @Column(name = "image_url", length = 500, nullable = false)
    private List<String> imagesUrl = new ArrayList<>();
*/
    @Enumerated(STRING)
    @Column(nullable = false, length = 12)
    private RoomTypeStatus status = RoomTypeStatus.ACTIVE;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "room_type_amenities",
            joinColumns = @JoinColumn(name = "room_type_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new HashSet<>();

    public RoomType() {}

    // ===== Getters / Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<RoomTypeImage> getImages()                     { return images; }
    public void setImages(List<RoomTypeImage> images)          { this.images = images; }
    public Set<Amenity> getAmenities() { return amenities; }
    public void setAmenities(Set<Amenity> amenities) { this.amenities = amenities; }
    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public Long getHotelId() { return hotel != null ? hotel.getId() : null; }

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
/*
    public List<String> getImagesUrl() { return imagesUrl; }
    public void setImagesUrl(List<String> imagesUrl) {
        this.imagesUrl = imagesUrl != null ? new ArrayList<>(imagesUrl) : new ArrayList<>();
    }
*/
    public RoomTypeStatus getStatus() { return status; }
    public void setStatus(RoomTypeStatus status) { this.status = status; }

    public boolean isActive() { return status == RoomTypeStatus.ACTIVE; }

    public boolean isActiveAndAvailable() { return isActive() && totalUnits > 0; }

    public int getMaxCapacity() { return maxAdults + maxChildren; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomType)) return false;
        RoomType other = (RoomType) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}
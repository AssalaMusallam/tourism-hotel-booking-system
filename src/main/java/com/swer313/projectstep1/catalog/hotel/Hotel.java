package com.swer313.projectstep1.catalog.hotel;
import com.swer313.projectstep1.catalog.amenities.Amenity;
import com.swer313.projectstep1.catalog.room.RoomType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "hotels",
        uniqueConstraints = @UniqueConstraint(name = "uk_hotels_name", columnNames = "name"),
        indexes = {
                @Index(name = "idx_hotels_city", columnList = "city"),
                @Index(name = "idx_hotels_country", columnList = "country"),
                @Index(name = "idx_hotels_status", columnList = "status")
        }
)
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200, unique = true)
    private String name;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String address;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HotelImage> images = new ArrayList<>();

    @Size(max = 30)
    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Email
    @Size(max = 320)
    @Column(length = 320)
    private String email;

    @Size(max = 500)
    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private Double rating;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String country;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    @Size(max = 2000)
    private String policies;

    @Size(max = 1000)
    @Column(name = "cancellation_policy_summary", length = 1000)
    private String cancellationPolicySummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    // ✅ Many-to-Many with Amenity entity (join table hotel_amenities)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotel_amenities",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_hotel_amenity",
                    columnNames = {"hotel_id", "amenity_id"}
            )
    )
    private Set<Amenity> amenities = new HashSet<>();

    public enum Status { ACTIVE, INACTIVE }


    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomType> roomTypes = new ArrayList<>();

    public Hotel() {}


    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<HotelImage> getImages() {
        return images;
    }

    public void setImages(List<HotelImage> images) {
        this.images = images;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getPolicies() { return policies; }
    public void setPolicies(String policies) { this.policies = policies; }

    public String getCancellationPolicySummary() { return cancellationPolicySummary; }
    public void setCancellationPolicySummary(String cancellationPolicySummary) { this.cancellationPolicySummary = cancellationPolicySummary; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public void setRoomTypes(List<RoomType> roomTypes) { this.roomTypes = roomTypes; }
    public Set<Amenity> getAmenities() { return amenities; }
    public void setAmenities(Set<Amenity> amenities) { this.amenities = amenities; }
    public List<RoomType> getRoomTypes() { return roomTypes; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hotel)) return false;
        Hotel other = (Hotel) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
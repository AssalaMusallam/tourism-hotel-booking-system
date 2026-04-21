package com.swer313.projectstep1.catalog.hotel;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;


public class HotelResponseDto {

    private Long id;
    private String name;
    private String address;
    private String description;
    private List<HotelImageResponseDto> images;
    private String phoneNumber;
    private String email;
    private String websiteUrl;
    private Double rating;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String policies;
    private String cancellationPolicySummary;
    private Hotel.Status status;
    private Set<String> amenityNames;

    public Set<String> getAmenityNames() {
        return amenityNames;
    }

    public void setAmenityNames(Set<String> amenityNames) {
        this.amenityNames = amenityNames;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<HotelImageResponseDto> getImages() {
        return images;
    }

    public void setImages(List<HotelImageResponseDto> images) {
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
    public void setCancellationPolicySummary(String cancellationPolicySummary) {
        this.cancellationPolicySummary = cancellationPolicySummary;
    }

    public Hotel.Status getStatus() { return status; }
    public void setStatus(Hotel.Status status) { this.status = status; }

//    public Set<Long> getAmenityIds() { return amenityIds; }
//    public void setAmenityIds(Set<Long> amenityIds) { this.amenityIds = amenityIds; }
}
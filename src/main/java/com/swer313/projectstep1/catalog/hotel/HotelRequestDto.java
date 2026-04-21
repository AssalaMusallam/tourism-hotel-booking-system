package com.swer313.projectstep1.catalog.hotel;

import jakarta.validation.constraints.*;

import java.time.LocalTime;
import java.util.Set;

public class HotelRequestDto {

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Size(max = 500)
    private String address;

    @Size(max = 2000)
    private String description;


    @Size(max = 30)
    private String phoneNumber;

    @Email
    @Size(max = 320)
    private String email;

    @Size(max = 500)
    private String websiteUrl;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private Double rating;

    @NotBlank
    @Size(max = 100)
    private String city;

    @NotBlank
    @Size(max = 100)
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
    private String cancellationPolicySummary;

    private Hotel.Status status; // optional, default ACTIVE in service

    // ✅ optional: amenity ids to attach
    private Set<Long> amenityIds;

    // getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }



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

    public Hotel.Status getStatus() { return status; }
    public void setStatus(Hotel.Status status) { this.status = status; }

    public Set<Long> getAmenityIds() { return amenityIds; }
    public void setAmenityIds(Set<Long> amenityIds) { this.amenityIds = amenityIds; }
}
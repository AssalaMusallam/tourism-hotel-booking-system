package com.swer313.projectstep1.catalog.amenities;
import com.swer313.projectstep1.catalog.amenities.Amenity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AmenityRequestDTO {

    @NotBlank(message = "Amenity name cannot be blank")
    @Size(min = 3, max = 100, message = "Amenity name must be between 3 and 100 characters")
    private String name;

    @Size(max = 100, message = "English amenity name must be at most 100 characters")
    private String nameEn;

    @NotBlank(message = "Amenity description cannot be blank")
    @Size(min = 10, max = 500, message = "Amenity description must be between 10 and 500 characters")
    private String description;

    @NotNull(message = "Amenity category cannot be null")
    private Amenity.AmenityCategory category;

    // optional flags (if null -> keep defaults / don't change)
    private Boolean premium;
    private Boolean active;

    public AmenityRequestDTO() {}

    public AmenityRequestDTO(String name, String description, Amenity.AmenityCategory category, Boolean premium, Boolean active) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.premium = premium;
        this.active = active;
    }

    public AmenityRequestDTO(String name, String nameEn, String description, Amenity.AmenityCategory category, Boolean premium, Boolean active) {
        this.name = name;
        this.nameEn = nameEn;
        this.description = description;
        this.category = category;
        this.premium = premium;
        this.active = active;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Amenity.AmenityCategory getCategory() { return category; }
    public void setCategory(Amenity.AmenityCategory category) { this.category = category; }

    public Boolean getPremium() { return premium; }
    public void setPremium(Boolean premium) { this.premium = premium; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

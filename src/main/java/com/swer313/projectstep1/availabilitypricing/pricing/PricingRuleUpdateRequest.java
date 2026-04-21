package com.swer313.projectstep1.availabilitypricing.pricing;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PricingRuleUpdateRequest {

    @Size(min = 2, max = 100, message = "name must be 2–100 characters")
    private String name;

    @Size(max = 500, message = "description cannot exceed 500 characters")
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @DecimalMin(value = "0.1", message = "priceMultiplier must be ≥ 0.1")
    @DecimalMax(value = "10.0", message = "priceMultiplier must be ≤ 10.0")
    private BigDecimal priceMultiplier;

    private Boolean active;

    public String getName()                               { return name; }
    public void setName(String name)                      { this.name = name; }
    public String getDescription()                        { return description; }
    public void setDescription(String description)        { this.description = description; }
    public LocalDate getStartDate()                       { return startDate; }
    public void setStartDate(LocalDate startDate)         { this.startDate = startDate; }
    public LocalDate getEndDate()                         { return endDate; }
    public void setEndDate(LocalDate endDate)             { this.endDate = endDate; }
    public BigDecimal getPriceMultiplier()                { return priceMultiplier; }
    public void setPriceMultiplier(BigDecimal multiplier) { this.priceMultiplier = multiplier; }
    public Boolean getActive()                            { return active; }
    public void setActive(Boolean active)                 { this.active = active; }
}
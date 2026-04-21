package com.swer313.projectstep1.availabilitypricing.pricing;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PricingRuleResponse {

    private final Long          id;
    private final String        name;
    private final String        description;
    private final LocalDate     startDate;
    private final LocalDate     endDate;
    private final BigDecimal    priceMultiplier;
    private final boolean       active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PricingRuleResponse(
            Long id, String name, String description,
            LocalDate startDate, LocalDate endDate,
            BigDecimal priceMultiplier, boolean active,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id              = id;
        this.name            = name;
        this.description     = description;
        this.startDate       = startDate;
        this.endDate         = endDate;
        this.priceMultiplier = priceMultiplier;
        this.active          = active;
        this.createdAt       = createdAt;
        this.updatedAt       = updatedAt;
    }

    public Long          getId()              { return id; }
    public String        getName()            { return name; }
    public String        getDescription()     { return description; }
    public LocalDate     getStartDate()       { return startDate; }
    public LocalDate     getEndDate()         { return endDate; }
    public BigDecimal    getPriceMultiplier() { return priceMultiplier; }
    public boolean       isActive()           { return active; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public LocalDateTime getUpdatedAt()       { return updatedAt; }
}
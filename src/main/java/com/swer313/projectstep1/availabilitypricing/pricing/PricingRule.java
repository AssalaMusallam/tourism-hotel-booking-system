package com.swer313.projectstep1.availabilitypricing.pricing;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "pricing_rules",
        indexes = {
                @Index(name = "idx_pricing_start_date", columnList = "start_date"),
                @Index(name = "idx_pricing_end_date",   columnList = "end_date"),
                @Index(name = "idx_pricing_active",     columnList = "active")
        }
)
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * 1.0 = سعر عادي | 1.5 = +50% | 0.8 = -20%
     */
    @NotNull
    @DecimalMin(value = "0.1", message = "multiplier must be at least 0.1")
    @DecimalMax(value = "10.0", message = "multiplier must not exceed 10.0")
    @Column(name = "price_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal priceMultiplier;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PricingRule() {}

    // ── Business helpers ──────────────────────────────────────────────────────

    /** هل هاد الـ rule فاعل ليوم معين؟ */
    public boolean isActiveOn(LocalDate date) {
        return active
                && !date.isBefore(startDate)
                && !date.isAfter(endDate);
    }

    /** هل فترة هاد الـ rule تتداخل مع فترة ثانية؟ */
    public boolean overlapsWith(LocalDate otherStart, LocalDate otherEnd) {
        return !endDate.isBefore(otherStart)
                && !startDate.isAfter(otherEnd);
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId()                                    { return id; }
    public String getName()                                { return name; }
    public void setName(String name)                       { this.name = name; }
    public String getDescription()                         { return description; }
    public void setDescription(String description)         { this.description = description; }
    public LocalDate getStartDate()                        { return startDate; }
    public void setStartDate(LocalDate startDate)          { this.startDate = startDate; }
    public LocalDate getEndDate()                          { return endDate; }
    public void setEndDate(LocalDate endDate)              { this.endDate = endDate; }
    public BigDecimal getPriceMultiplier()                 { return priceMultiplier; }
    public void setPriceMultiplier(BigDecimal multiplier)  { this.priceMultiplier = multiplier; }
    public boolean isActive()                              { return active; }
    public void setActive(boolean active)                  { this.active = active; }
    public LocalDateTime getCreatedAt()                    { return createdAt; }
    public LocalDateTime getUpdatedAt()                    { return updatedAt; }

    @Override
    public String toString() {
        return "PricingRule{id=" + id + ", name='" + name + "', "
                + startDate + " → " + endDate
                + ", multiplier=" + priceMultiplier
                + ", active=" + active + '}';
    }
}
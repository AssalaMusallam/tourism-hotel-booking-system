package com.swer313.projectstep1.availabilitypricing.pricing;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * تفاصيل السعر الكاملة لفترة حجز.
 * بيستخدمه كل من:
 *   - Availability module  (preview قبل الحجز)
 *   - Booking module       (تخزين السعر النهائي)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceBreakdownDTO {

    private final BigDecimal        basePrice;
    private final int               nights;
    private final List<NightDetail> breakdown;
    private final BigDecimal        subtotal;
    private final BigDecimal        taxRate;
    private final BigDecimal        taxAmount;
    private final BigDecimal        totalPrice;

    public PriceBreakdownDTO(BigDecimal basePrice,
                             int nights,
                             List<NightDetail> breakdown,
                             BigDecimal subtotal,
                             BigDecimal taxRate,
                             BigDecimal taxAmount,
                             BigDecimal totalPrice) {
        this.basePrice  = basePrice;
        this.nights     = nights;
        this.breakdown  = breakdown;
        this.subtotal   = subtotal;
        this.taxRate    = taxRate;
        this.taxAmount  = taxAmount;
        this.totalPrice = totalPrice;
    }

    // ── Nested: تفاصيل ليلة واحدة ─────────────────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NightDetail {

        private final LocalDate  date;
        private final BigDecimal baseRate;
        private final BigDecimal seasonMultiplier;
        private final String     appliedRuleName;   // null لو ما في rule
        private final BigDecimal weekendMultiplier;
        private final boolean    isWeekend;
        private final BigDecimal nightTotal;

        public NightDetail(LocalDate date,
                           BigDecimal baseRate,
                           BigDecimal seasonMultiplier,
                           String appliedRuleName,
                           BigDecimal weekendMultiplier,
                           boolean isWeekend,
                           BigDecimal nightTotal) {
            this.date              = date;
            this.baseRate          = baseRate;
            this.seasonMultiplier  = seasonMultiplier;
            this.appliedRuleName   = appliedRuleName;
            this.weekendMultiplier = weekendMultiplier;
            this.isWeekend         = isWeekend;
            this.nightTotal        = nightTotal;
        }

        public LocalDate  getDate()              { return date; }
        public BigDecimal getBaseRate()          { return baseRate; }
        public BigDecimal getSeasonMultiplier()  { return seasonMultiplier; }
        public String     getAppliedRuleName()   { return appliedRuleName; }
        public BigDecimal getWeekendMultiplier() { return weekendMultiplier; }
        public boolean    isWeekend()            { return isWeekend; }
        public BigDecimal getNightTotal()        { return nightTotal; }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public BigDecimal        getBasePrice()  { return basePrice; }
    public int               getNights()     { return nights; }
    public List<NightDetail> getBreakdown()  { return breakdown; }
    public BigDecimal        getSubtotal()   { return subtotal; }
    public BigDecimal        getTaxRate()    { return taxRate; }
    public BigDecimal        getTaxAmount()  { return taxAmount; }
    public BigDecimal        getTotalPrice() { return totalPrice; }
}
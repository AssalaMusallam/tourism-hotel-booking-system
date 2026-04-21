package com.swer313.projectstep1.availabilitypricing.pricing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Component
public class PricingCalculator {

    private static final BigDecimal ONE = BigDecimal.ONE;

    @Value("${pricing.weekend.multiplier:1.25}")
    private BigDecimal weekendMultiplier;

    @Value("${pricing.tax.rate:0.16}")
    private BigDecimal taxRate;

    @Value("${pricing.weekend.days:FRIDAY,SATURDAY}")
    private String weekendDaysConfig;

    private final PricingRuleRepository pricingRuleRepository;

    public PricingCalculator(PricingRuleRepository pricingRuleRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * احسب السعر الكامل مع التفاصيل.
     * الـ method الرئيسي — بيستخدمه Availability + Booking.
     */
    public PriceBreakdownDTO calculateBreakdown(BigDecimal basePrice,
                                                LocalDate checkIn,
                                                LocalDate checkOut) {
        // ✅ query واحدة للـ DB
        List<PricingRule> rules = pricingRuleRepository
                .findActiveRulesInRange(checkIn, checkOut.minusDays(1));

        Set<DayOfWeek> weekendDays = parseWeekendDays();
        List<PriceBreakdownDTO.NightDetail> details = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        LocalDate night = checkIn;
        while (night.isBefore(checkOut)) {

            final LocalDate currentNight = night;
            PricingRule matchedRule = rules.stream()
                    .filter(r -> r.isActiveOn(currentNight))
                    .findFirst()
                    .orElse(null);

            BigDecimal seasonMult = matchedRule != null
                    ? matchedRule.getPriceMultiplier()
                    : ONE;

            boolean    isWeekend  = weekendDays.contains(night.getDayOfWeek());
            BigDecimal wkndMult   = isWeekend ? weekendMultiplier : ONE;

            BigDecimal nightTotal = basePrice
                    .multiply(seasonMult)
                    .multiply(wkndMult)
                    .setScale(2, RoundingMode.HALF_UP);

            details.add(new PriceBreakdownDTO.NightDetail(
                    currentNight,
                    basePrice,
                    seasonMult,
                    matchedRule != null ? matchedRule.getName() : null,
                    wkndMult,
                    isWeekend,
                    nightTotal
            ));

            subtotal = subtotal.add(nightTotal);
            night    = night.plusDays(1);
        }

        BigDecimal taxAmount  = subtotal
                .multiply(taxRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPrice = subtotal
                .add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return new PriceBreakdownDTO(
                basePrice,
                details.size(),
                details,
                subtotal,
                taxRate,
                taxAmount,
                totalPrice
        );
    }

    /**
     * نسخة مبسّطة — بترجع السعر الكلي بس.
     * للاستخدام في الـ Booking عند الحفظ.
     */
    public BigDecimal calculateTotalPrice(BigDecimal basePrice,
                                          LocalDate checkIn,
                                          LocalDate checkOut) {
        return calculateBreakdown(basePrice, checkIn, checkOut).getTotalPrice();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Set<DayOfWeek> parseWeekendDays() {
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (String day : weekendDaysConfig.split(",")) {
            days.add(DayOfWeek.valueOf(day.trim().toUpperCase()));
        }
        return days;
    }
}
package com.swer313.projectstep1.availabilitypricing.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PricingRuleTest {

    @Test
    void isActiveOn_activeAndWithinRange_returnsTrue() {
        PricingRule r = new PricingRule();
        r.setActive(true);
        r.setStartDate(LocalDate.of(2026, 6, 1));
        r.setEndDate(LocalDate.of(2026, 6, 10));

        assertTrue(r.isActiveOn(LocalDate.of(2026, 6, 5)));
    }

    @Test
    void isActiveOn_inactive_returnsFalse() {
        PricingRule r = new PricingRule();
        r.setActive(false);
        r.setStartDate(LocalDate.of(2026, 6, 1));
        r.setEndDate(LocalDate.of(2026, 6, 10));

        assertFalse(r.isActiveOn(LocalDate.of(2026, 6, 5)));
    }

    @Test
    void isActiveOn_beforeOrAfter_returnsFalse() {
        PricingRule r = new PricingRule();
        r.setActive(true);
        r.setStartDate(LocalDate.of(2026, 6, 5));
        r.setEndDate(LocalDate.of(2026, 6, 10));

        assertFalse(r.isActiveOn(LocalDate.of(2026, 6, 4)));
        assertFalse(r.isActiveOn(LocalDate.of(2026, 6, 11)));
    }

    @Test
    void overlapsWith_overlappingRanges_returnTrue() {
        PricingRule r = new PricingRule();
        r.setStartDate(LocalDate.of(2026, 6, 5));
        r.setEndDate(LocalDate.of(2026, 6, 15));

        // overlaps with 1..10
        assertTrue(r.overlapsWith(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10)));
        // overlaps with 10..20 (end touching)
        assertTrue(r.overlapsWith(LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 20)));
        // overlaps fully inside
        assertTrue(r.overlapsWith(LocalDate.of(2026, 6, 6), LocalDate.of(2026, 6, 14)));
    }

    @Test
    void overlapsWith_nonOverlapping_returnFalse() {
        PricingRule r = new PricingRule();
        r.setStartDate(LocalDate.of(2026, 6, 5));
        r.setEndDate(LocalDate.of(2026, 6, 15));

        assertFalse(r.overlapsWith(LocalDate.of(2026, 6, 16), LocalDate.of(2026, 6, 20)));
        assertFalse(r.overlapsWith(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 4)));
    }
}


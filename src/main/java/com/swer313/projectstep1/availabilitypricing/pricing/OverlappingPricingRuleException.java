package com.swer313.projectstep1.availabilitypricing.pricing;

import com.swer313.projectstep1.errors.ConflictException;

import java.time.LocalDate;

public class OverlappingPricingRuleException extends ConflictException {
    public OverlappingPricingRuleException(String name,
                                           LocalDate start,
                                           LocalDate end) {
        super(String.format(
                "Pricing rule '%s' already covers %s → %s. Periods cannot overlap.",
                name, start, end));
    }
}
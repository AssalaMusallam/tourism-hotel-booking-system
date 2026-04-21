package com.swer313.projectstep1.availabilitypricing.pricing;

import com.swer313.projectstep1.errors.NotFoundException;

public class PricingRuleNotFoundException extends NotFoundException {
    public PricingRuleNotFoundException(Long id) {
        super("Pricing rule not found with id: " + id);
    }
}
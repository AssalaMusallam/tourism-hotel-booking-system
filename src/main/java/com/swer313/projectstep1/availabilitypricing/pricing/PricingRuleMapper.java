package com.swer313.projectstep1.availabilitypricing.pricing;

import org.springframework.stereotype.Component;

@Component
public class PricingRuleMapper {

    public PricingRule toEntity(PricingRuleRequest dto) {
        PricingRule rule = new PricingRule();
        rule.setName(dto.getName());
        rule.setDescription(dto.getDescription());
        rule.setStartDate(dto.getStartDate());
        rule.setEndDate(dto.getEndDate());
        rule.setPriceMultiplier(dto.getPriceMultiplier());
        rule.setActive(true);
        return rule;
    }

    public PricingRuleResponse toResponse(PricingRule rule) {
        return new PricingRuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getDescription(),
                rule.getStartDate(),
                rule.getEndDate(),
                rule.getPriceMultiplier(),
                rule.isActive(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
package com.swer313.projectstep1.availabilitypricing.pricing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PricingRuleMapperTest {

    private final PricingRuleMapper mapper = new PricingRuleMapper();

    @Test
    void toEntity_validRequest_mapsFieldsAndSetsActiveTrue() {
        PricingRuleRequest req = new PricingRuleRequest();
        req.setName("Holiday");
        req.setDescription("Holiday pricing");
        req.setStartDate(LocalDate.of(2026, 12, 20));
        req.setEndDate(LocalDate.of(2026, 12, 31));
        req.setPriceMultiplier(new BigDecimal("1.20"));

        PricingRule entity = mapper.toEntity(req);

        assertEquals("Holiday", entity.getName());
        assertEquals("Holiday pricing", entity.getDescription());
        assertEquals(LocalDate.of(2026, 12, 20), entity.getStartDate());
        assertEquals(LocalDate.of(2026, 12, 31), entity.getEndDate());
        assertEquals(new BigDecimal("1.20"), entity.getPriceMultiplier());
        assertTrue(entity.isActive());
    }

    @Test
    void toResponse_entity_mapsFields() {
        PricingRule rule = new PricingRule();
        rule.setName("Promo");
        rule.setDescription("Desc");
        rule.setStartDate(LocalDate.of(2026, 6, 1));
        rule.setEndDate(LocalDate.of(2026, 6, 10));
        rule.setPriceMultiplier(new BigDecimal("1.50"));
        rule.setActive(true);

        PricingRuleResponse resp = mapper.toResponse(rule);

        assertNull(resp.getId()); // id not set
        assertEquals("Promo", resp.getName());
        assertEquals("Desc", resp.getDescription());
        assertEquals(LocalDate.of(2026, 6, 1), resp.getStartDate());
        assertEquals(LocalDate.of(2026, 6, 10), resp.getEndDate());
        assertEquals(new BigDecimal("1.50"), resp.getPriceMultiplier());
        assertTrue(resp.isActive());
    }
}


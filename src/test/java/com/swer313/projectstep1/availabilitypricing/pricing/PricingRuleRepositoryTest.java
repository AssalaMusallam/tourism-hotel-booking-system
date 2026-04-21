package com.swer313.projectstep1.availabilitypricing.pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PricingRuleRepositoryTest {

    @Autowired
    private PricingRuleRepository repository;

    @BeforeEach
    void cleanUp() {
        // لأنك تختبرين على MySQL الحقيقي، الأفضل تنظيف الجدول قبل كل test
        repository.deleteAll();
    }

    @Test
    void findActiveRuleForDate_returnsRule() {
        PricingRule r = new PricingRule();
        r.setName("R1");
        r.setStartDate(LocalDate.of(2026, 6, 1));
        r.setEndDate(LocalDate.of(2026, 6, 5));
        r.setPriceMultiplier(new BigDecimal("1.0"));
        r.setActive(true);

        repository.save(r);

        var opt = repository.findActiveRuleForDate(LocalDate.of(2026, 6, 3));

        assertTrue(opt.isPresent());
        assertEquals("R1", opt.get().getName());
    }

    @Test
    void findActiveRulesInRange_returnsRulesInRange() {
        PricingRule r1 = new PricingRule();
        r1.setName("AA");
        r1.setStartDate(LocalDate.of(2026, 6, 1));
        r1.setEndDate(LocalDate.of(2026, 6, 3));
        r1.setPriceMultiplier(new BigDecimal("1.0"));
        r1.setActive(true);

        PricingRule r2 = new PricingRule();
        r2.setName("BB");
        r2.setStartDate(LocalDate.of(2026, 6, 4));
        r2.setEndDate(LocalDate.of(2026, 6, 6));
        r2.setPriceMultiplier(new BigDecimal("1.0"));
        r2.setActive(true);

        repository.saveAll(List.of(r1, r2));

        List<PricingRule> found =
                repository.findActiveRulesInRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 6));

        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(rr -> "AA".equals(rr.getName())));
        assertTrue(found.stream().anyMatch(rr -> "BB".equals(rr.getName())));
    }

    @Test
    void findOverlapping_excludeId_omitsExcluded() {
        PricingRule r = new PricingRule();
        r.setName("XX"); // لازم يكون 2 أحرف أو أكثر
        r.setStartDate(LocalDate.of(2026, 6, 1));
        r.setEndDate(LocalDate.of(2026, 6, 10));
        r.setPriceMultiplier(new BigDecimal("1.0"));
        r.setActive(true);

        PricingRule saved = repository.save(r);

        List<PricingRule> overlappingWithExclude = repository.findOverlapping(
                LocalDate.of(2026, 6, 5),
                LocalDate.of(2026, 6, 6),
                saved.getId()
        );

        assertTrue(overlappingWithExclude.isEmpty());

        List<PricingRule> overlappingWithoutExclude = repository.findOverlapping(
                LocalDate.of(2026, 6, 5),
                LocalDate.of(2026, 6, 6),
                null
        );

        assertFalse(overlappingWithoutExclude.isEmpty());
    }

    @Test
    void findByActiveTrue_returnsOnlyActive() {
        PricingRule a = new PricingRule();
        a.setName("active");
        a.setStartDate(LocalDate.of(2026, 6, 1));
        a.setEndDate(LocalDate.of(2026, 6, 2));
        a.setPriceMultiplier(new BigDecimal("1.0"));
        a.setActive(true);

        PricingRule b = new PricingRule();
        b.setName("inactive");
        b.setStartDate(LocalDate.of(2026, 6, 1));
        b.setEndDate(LocalDate.of(2026, 6, 2));
        b.setPriceMultiplier(new BigDecimal("1.0"));
        b.setActive(false);

        repository.saveAll(List.of(a, b));

        List<PricingRule> actives = repository.findByActiveTrue();

        assertTrue(actives.stream().allMatch(PricingRule::isActive));
        assertTrue(actives.stream().anyMatch(rr -> "active".equals(rr.getName())));
        assertFalse(actives.stream().anyMatch(rr -> "inactive".equals(rr.getName())));
    }

    @Test
    void pagingAndOrdering_workAsExpected() {
        PricingRule r1 = new PricingRule();
        r1.setName("one");
        r1.setStartDate(LocalDate.of(2026, 6, 1));
        r1.setEndDate(LocalDate.of(2026, 6, 2));
        r1.setPriceMultiplier(new BigDecimal("1.0"));
        r1.setActive(true);

        PricingRule r2 = new PricingRule();
        r2.setName("two");
        r2.setStartDate(LocalDate.of(2026, 6, 3));
        r2.setEndDate(LocalDate.of(2026, 6, 4));
        r2.setPriceMultiplier(new BigDecimal("1.0"));
        r2.setActive(true);

        repository.saveAll(List.of(r1, r2));

        var page = repository.findAllByOrderByStartDateDesc(PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
        assertEquals("two", page.getContent().get(0).getName());

        var activePage = repository.findByActiveTrueOrderByStartDateDesc(PageRequest.of(0, 10));
        assertEquals(2, activePage.getTotalElements());
    }
}
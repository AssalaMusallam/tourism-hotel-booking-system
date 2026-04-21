package com.swer313.projectstep1.availabilitypricing.pricing;
import com.swer313.projectstep1.availabilitypricing.availability.PagedResponse;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface PricingRuleService {

    // ── Write ─────────────────────────────────────────────────────────────────
    PricingRuleResponse createRule(PricingRuleRequest request);
    PricingRuleResponse updateRule(Long id, PricingRuleUpdateRequest update);
    void                deleteRule(Long id);

    // ── Read ──────────────────────────────────────────────────────────────────
    PricingRuleResponse getById(Long id);

    // ✅ هدول الاثنين صاروا Paged
    PagedResponse<PricingRuleResponse> getAllRules(Pageable pageable);
    PagedResponse<PricingRuleResponse> getActiveRules(Pageable pageable);

    // ── Pricing ───────────────────────────────────────────────────────────────
    PriceBreakdownDTO previewPrice(BigDecimal basePrice,
                                   LocalDate checkIn,
                                   LocalDate checkOut);
}
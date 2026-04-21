package com.swer313.projectstep1.availabilitypricing.pricing;

import com.swer313.projectstep1.availabilitypricing.availability.PagedResponse;
import com.swer313.projectstep1.errors.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PricingRuleServiceImpl implements PricingRuleService {

    private final PricingRuleRepository pricingRuleRepository;
    private final PricingRuleMapper     pricingRuleMapper;
    private final PricingCalculator     pricingCalculator;

    public PricingRuleServiceImpl(PricingRuleRepository pricingRuleRepository,
                                  PricingRuleMapper pricingRuleMapper,
                                  PricingCalculator pricingCalculator) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.pricingRuleMapper     = pricingRuleMapper;
        this.pricingCalculator     = pricingCalculator;
    }

    @Override
    public PricingRuleResponse createRule(PricingRuleRequest request) {
        validateDateRange(request.getStartDate(), request.getEndDate());
        validateNoOverlap(request.getStartDate(), request.getEndDate(), null);
        return pricingRuleMapper.toResponse(
                pricingRuleRepository.save(pricingRuleMapper.toEntity(request)));
    }

    @Override
    public PricingRuleResponse updateRule(Long id, PricingRuleUpdateRequest update) {
        PricingRule rule = findOrThrow(id);

        if (update.getName()            != null) rule.setName(update.getName());
        if (update.getDescription()     != null) rule.setDescription(update.getDescription());
        if (update.getPriceMultiplier() != null) rule.setPriceMultiplier(update.getPriceMultiplier());
        if (update.getActive()          != null) rule.setActive(update.getActive());
        if (update.getStartDate()       != null) rule.setStartDate(update.getStartDate());
        if (update.getEndDate()         != null) rule.setEndDate(update.getEndDate());

        if (update.getStartDate() != null || update.getEndDate() != null) {
            validateDateRange(rule.getStartDate(), rule.getEndDate());
            validateNoOverlap(rule.getStartDate(), rule.getEndDate(), id);
        }

        return pricingRuleMapper.toResponse(pricingRuleRepository.save(rule));
    }

    @Override
    public void deleteRule(Long id) {
        pricingRuleRepository.delete(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PricingRuleResponse getById(Long id) {
        return pricingRuleMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PricingRuleResponse> getAllRules(Pageable pageable) {
        Page<PricingRule> page = pricingRuleRepository
                .findAllByOrderByStartDateDesc(pageable);
        return toPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PricingRuleResponse> getActiveRules(Pageable pageable) {
        Page<PricingRule> page = pricingRuleRepository
                .findByActiveTrueOrderByStartDateDesc(pageable);
        return toPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PriceBreakdownDTO previewPrice(BigDecimal basePrice,
                                          LocalDate checkIn,
                                          LocalDate checkOut) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("basePrice must be greater than 0");
        if (!checkOut.isAfter(checkIn))
            throw new BadRequestException("checkOut must be after checkIn");
        return pricingCalculator.calculateBreakdown(basePrice, checkIn, checkOut);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private PricingRule findOrThrow(Long id) {
        return pricingRuleRepository.findById(id)
                .orElseThrow(() -> new PricingRuleNotFoundException(id));
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (!end.isAfter(start))
            throw new BadRequestException("endDate must be strictly after startDate");
    }

    private void validateNoOverlap(LocalDate start, LocalDate end, Long excludeId) {
        List<PricingRule> overlapping =
                pricingRuleRepository.findOverlapping(start, end, excludeId);
        if (!overlapping.isEmpty()) {
            PricingRule c = overlapping.get(0);
            throw new OverlappingPricingRuleException(
                    c.getName(), c.getStartDate(), c.getEndDate());
        }

    }


    private PagedResponse<PricingRuleResponse> toPagedResponse(Page<PricingRule> page) {
        List<PricingRuleResponse> mapped = page.getContent()
                .stream()
                .map(pricingRuleMapper::toResponse)
                .toList();
        return PagedResponse.from(page, mapped);
    }
    }

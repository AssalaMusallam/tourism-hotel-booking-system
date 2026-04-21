package com.swer313.projectstep1.availabilitypricing.pricing;

import com.swer313.projectstep1.availabilitypricing.availability.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/pricing-rules")
@Tag(name = "Pricing Rules", description = "Manage seasonal pricing rules + price preview")
public class PricingRuleController {

    private final PricingRuleService pricingRuleService;

    public PricingRuleController(PricingRuleService pricingRuleService) {
        this.pricingRuleService = pricingRuleService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new pricing rule")
    public ResponseEntity<PricingRuleResponse> create(
            @Valid @RequestBody PricingRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pricingRuleService.createRule(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing pricing rule")
    public ResponseEntity<PricingRuleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PricingRuleUpdateRequest update) {
        return ResponseEntity.ok(pricingRuleService.updateRule(id, update));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a pricing rule")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pricingRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Get pricing rule by ID")
    public ResponseEntity<PricingRuleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pricingRuleService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Get all pricing rules (paginated)")
    public ResponseEntity<PagedResponse<PricingRuleResponse>> getAll(
            @PageableDefault(size = 10, sort = "startDate",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pricingRuleService.getAllRules(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active")
    @Operation(summary = "Get active pricing rules (paginated)")
    public ResponseEntity<PagedResponse<PricingRuleResponse>> getActive(
            @PageableDefault(size = 10, sort = "startDate",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pricingRuleService.getActiveRules(pageable));
    }

    @GetMapping("/preview")
    @Operation(summary = "Preview price breakdown before booking")
    public ResponseEntity<PriceBreakdownDTO> previewPrice(
            @RequestParam BigDecimal basePrice,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(
                pricingRuleService.previewPrice(basePrice, checkIn, checkOut));
    }
}
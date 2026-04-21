package com.swer313.projectstep1.availabilitypricing.availability;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Availability", description = "Check room availability and pricing")
public class AvailabilityController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE     = 50;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "capacity", "basePrice", "totalUnits", "status"
    );

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * فحص availability لـ roomType واحد مع السعر الكامل والتفاصيل.
     * GET /api/v1/availability?roomTypeId=1&checkIn=2025-08-01&checkOut=2025-08-05&guests=2
     */
    @GetMapping("/availability")
    @Operation(summary = "Check availability and price for a single room type")
    public ResponseEntity<AvailabilityResponseDto> checkAvailability(
            @RequestParam @NotNull Long roomTypeId,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) @Min(1) Integer guests
    ) {
        return ResponseEntity.ok(
                availabilityService.checkAvailability(
                        roomTypeId, checkIn, checkOut, guests)
        );
    }

    /**
     * فحص availability لكل roomTypes في فندق مع pagination وفلاتر.
     * GET /api/v1/hotels/{hotelId}/availability?checkIn=...&checkOut=...&availableOnly=true
     */
    @GetMapping("/hotels/{hotelId}/availability")
    @Operation(summary = "Check availability for all room types in a hotel")
    public ResponseEntity<PagedResponse<AvailabilitySummaryDto>> checkHotelAvailability(
            @PathVariable Long hotelId,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) @Min(1) Integer guests,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean availableOnly,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(
                availabilityService.checkHotelAvailability(
                        hotelId, checkIn, checkOut,
                        guests, q, availableOnly, pageable)
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sort) {
        if (page < 0)
            throw new IllegalArgumentException("page must be >= 0");
        if (size <= 0)
            throw new IllegalArgumentException("size must be > 0");
        if (size > MAX_SIZE)
            throw new IllegalArgumentException("size must be <= " + MAX_SIZE);

        return PageRequest.of(page, size, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank())
            return Sort.by(Sort.Direction.ASC, "id");

        String[] parts     = sort.split(",");
        String   field     = parts[0].trim();
        Sort.Direction dir = Sort.Direction.ASC;

        if (!ALLOWED_SORT_FIELDS.contains(field))
            throw new IllegalArgumentException("Invalid sort field: " + field);

        if (parts.length > 1) {
            String d = parts[1].trim().toLowerCase();
            if ("desc".equals(d))       dir = Sort.Direction.DESC;
            else if (!"asc".equals(d))
                throw new IllegalArgumentException(
                        "Invalid sort direction: " + parts[1]);
        }

        return Sort.by(dir, field);
    }
}
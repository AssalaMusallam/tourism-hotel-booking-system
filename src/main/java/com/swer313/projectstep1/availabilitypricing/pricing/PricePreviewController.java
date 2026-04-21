package com.swer313.projectstep1.availabilitypricing.pricing;

import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeNotFoundException;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import com.swer313.projectstep1.errors.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/room-types")
@Validated
@Tag(name = "Price Preview",
        description = "Get full price breakdown for a room type before booking")
public class PricePreviewController {

    private static final int MAX_NIGHTS = 365;

    private final RoomTypeRepository roomTypeRepository;
    private final PricingCalculator  pricingCalculator;

    public PricePreviewController(RoomTypeRepository roomTypeRepository,
                                  PricingCalculator pricingCalculator) {
        this.roomTypeRepository = roomTypeRepository;
        this.pricingCalculator  = pricingCalculator;
    }

    @GetMapping("/{roomTypeId}/price-preview")
    @Operation(
            summary = "Get full price breakdown for a room type before booking",
            description = """
            Returns a night-by-night price breakdown including:
            • Base price per night from the room type
            • Seasonal pricing rule applied per night (if any)
            • Weekend surcharge (Friday & Saturday = ×1.25 by default)
            • Subtotal before tax
            • Tax amount (16%)
            • Final total price
            
            Validations:
            • checkOut must be after checkIn
            • Maximum stay is 365 nights
            • Room type must exist and be ACTIVE
            """
    )
    public ResponseEntity<PriceBreakdownDTO> getPricePreview(
            @PathVariable Long roomTypeId,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        // 1. checkOut لازم يكون بعد checkIn
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException(
                    "checkOut must be after checkIn."
            );
        }

        // 2. منع مدة أكثر من 365 ليلة
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights > MAX_NIGHTS) {
            throw new BadRequestException(
                    "Booking period cannot exceed " + MAX_NIGHTS + " nights. " +
                            "Requested: " + nights + " nights."
            );
        }

        // 3. تحقق إن الـ RoomType موجود
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RoomTypeNotFoundException(roomTypeId));

        // 4. تحقق إن الـ RoomType فاعل (ACTIVE)
        if (roomType.getStatus() != RoomTypeStatus.ACTIVE) {
            throw new BadRequestException(
                    "Room type '" + roomType.getName() +
                            "' is not available for booking. Status: " + roomType.getStatus()
            );
        }

        // 5. احسب السعر باستخدام الـ PricingCalculator الموجود
        PriceBreakdownDTO breakdown = pricingCalculator.calculateBreakdown(
                roomType.getBasePrice(),
                checkIn,
                checkOut
        );

        return ResponseEntity.ok(breakdown);
    }
}

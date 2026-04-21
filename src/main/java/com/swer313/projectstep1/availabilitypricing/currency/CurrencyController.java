package com.swer313.projectstep1.availabilitypricing.currency;

import com.swer313.projectstep1.availabilitypricing.pricing.PricingCalculator;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@Validated
@Tag(name = "Currency",
        description = "Currency conversion and price display in multiple currencies")
public class CurrencyController {

    private final CurrencyService    currencyService;
    private final RoomTypeRepository roomTypeRepository;
    private final PricingCalculator  pricingCalculator;
    private final ExchangeRateRepository exchangeRateRepository;

    public CurrencyController(CurrencyService currencyService,
                              RoomTypeRepository roomTypeRepository,
                              PricingCalculator pricingCalculator,
                              ExchangeRateRepository exchangeRateRepository) {
        this.currencyService         = currencyService;
        this.roomTypeRepository      = roomTypeRepository;
        this.pricingCalculator       = pricingCalculator;
        this.exchangeRateRepository  = exchangeRateRepository;
    }

    // ── Endpoint 1: كل العملات المدعومة ──────────────────────────────────────

    /**
     * GET /api/currencies
     *
     * يرجع كل العملات المتاحة للتحويل من USD.
     * الـ guest يستخدمه عشان يعرف شو العملات يقدر يختار.
     *
     * Response مثال:
     * [
     *   {"fromCurrency": "USD", "toCurrency": "EUR", "rate": 0.92},
     *   {"fromCurrency": "USD", "toCurrency": "ILS", "rate": 3.67},
     *   ...
     * ]
     */
    @GetMapping
    @Operation(summary = "List all supported currencies and their exchange rates from USD")
    public ResponseEntity<List<ExchangeRate>> getSupportedCurrencies() {
        return ResponseEntity.ok(currencyService.getSupportedCurrencies());
    }

    // ── Endpoint 2: سعر الغرفة بعملة معينة ──────────────────────────────────

    /**
     * GET /api/currencies/room-types/{roomTypeId}/price
     *       ?checkIn=2026-05-01&checkOut=2026-05-03&currency=ILS
     *
     * يحسب السعر الكامل للغرفة (مع seasonal + weekend + tax)
     * ويحوّله للعملة المطلوبة.
     *
     * الـ flow:
     * 1. جيب الـ RoomType وتحقق إنه ACTIVE
     * 2. احسب السعر الكامل بالـ USD باستخدام PricingCalculator
     * 3. جيب سعر الصرف من الداتابيس
     * 4. حوّل السعر للعملة المطلوبة
     * 5. ارجع السعرين (USD والعملة) مع سعر الصرف للشفافية
     *
     * Response مثال (currency=ILS):
     * {
     *   "roomTypeId": 1,
     *   "roomTypeName": "Deluxe Suite",
     *   "nights": 2,
     *   "originalTotalUSD": 232.00,
     *   "convertedTotal": 851.44,
     *   "currency": "ILS",
     *   "exchangeRate": 3.670000
     * }
     */
    @GetMapping("/room-types/{roomTypeId}/price")
    @Operation(
            summary = "Get room price converted to requested currency",
            description = """
            Calculates the full price (with seasonal rules, weekend surcharge and tax)
            then converts it to the requested currency.
            Always shows the original USD price for transparency.
            """
    )
    public ResponseEntity<PriceInCurrencyResponseDto> getRoomPriceInCurrency(
            @PathVariable Long roomTypeId,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "USD") String currency) {

        // 1. تحقق التواريخ
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("checkOut must be after checkIn.");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights > 365) {
            throw new BadRequestException(
                    "Booking period cannot exceed 365 nights. Requested: " + nights
            );
        }

        // 2. جيب الـ RoomType وتحقق إنه ACTIVE
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RoomTypeNotFoundException(roomTypeId));

        if (roomType.getStatus() != RoomTypeStatus.ACTIVE) {
            throw new BadRequestException(
                    "Room type '" + roomType.getName() + "' is not available. " +
                            "Status: " + roomType.getStatus()
            );
        }

        // 3. احسب السعر الكامل بالـ USD
        //    PricingCalculator يحسب: basePrice × seasonMultiplier × weekendMultiplier + tax
        BigDecimal totalUSD = pricingCalculator
                .calculateBreakdown(roomType.getBasePrice(), checkIn, checkOut)
                .getTotalPrice();

        // 4. حوّل للعملة المطلوبة
        //    لو currency=USD → نفس المبلغ بدون حساب
        BigDecimal convertedTotal = currencyService.convert(totalUSD, "USD", currency);

        // 5. جيب سعر الصرف للشفافية في الـ response
        //    لو USD → rate = 1.0
        BigDecimal exchangeRate = currency.equalsIgnoreCase("USD")
                ? BigDecimal.ONE
                : exchangeRateRepository
                .findByFromCurrencyAndToCurrency("USD", currency.toUpperCase())
                .map(ExchangeRate::getRate)
                .orElse(BigDecimal.ONE);

        return ResponseEntity.ok(new PriceInCurrencyResponseDto(
                roomTypeId,
                roomType.getName(),
                nights,
                totalUSD,
                convertedTotal,
                currency.toUpperCase(),
                exchangeRate
        ));
    }
}
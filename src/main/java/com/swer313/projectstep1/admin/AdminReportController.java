package com.swer313.projectstep1.admin;

import com.swer313.projectstep1.errors.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@Validated
@Tag(name = "Admin Reports",
        description = "Revenue, occupancy and booking analytics for hotel admins")
public class AdminReportController {

    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    // ── Revenue ───────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/reports/revenue?hotelId=1&from=2026-01-01&to=2026-03-31
     *
     * مثال الـ response:
     * {
     *   "hotelId": 1,
     *   "hotelName": "Grand Hotel",
     *   "from": "2026-01-01",
     *   "to": "2026-03-31",
     *   "totalRevenue": 15400.00,
     *   "currency": "USD",
     *   "totalBookings": 42
     * }
     */
    @GetMapping("/revenue")
    @Operation(
            summary = "Revenue report for a hotel in a date range",
            description = "Returns total confirmed revenue and booking count between two dates"
    )
    public ResponseEntity<RevenueReportDto> getRevenue(
            @RequestParam @NotNull Long hotelId,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // validation إضافي — الـ @Future/@Past ما بتشتغل على query params
        if (!to.isAfter(from)) {
            throw new BadRequestException("'to' date must be after 'from' date.");
        }

        return ResponseEntity.ok(
                adminReportService.getRevenueReport(hotelId, from, to)
        );
    }

    // ── Occupancy ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/reports/occupancy?hotelId=1&month=2026-03
     *
     * مثال الـ response:
     * {
     *   "hotelId": 1,
     *   "month": "2026-03",
     *   "totalRooms": 35,
     *   "occupancyRate": 47.6
     * }
     */
    @GetMapping("/occupancy")
    @Operation(
            summary = "Occupancy rate for a hotel in a given month",
            description = "Month format: 2026-03 — Returns occupancy % based on actual room units"
    )
    public ResponseEntity<OccupancyReportDto> getOccupancy(
            @RequestParam @NotNull Long hotelId,
            @RequestParam @NotNull String month) {

        // تحقق من format الشهر قبل ما نمرره للـ service
        // لو ما تحققنا → YearMonth.parse بيطلع exception غير واضح
        if (!month.matches("\\d{4}-\\d{2}")) {
            throw new BadRequestException(
                    "Invalid month format. Use YYYY-MM (e.g. 2026-03)."
            );
        }

        return ResponseEntity.ok(
                adminReportService.getOccupancyReport(hotelId, month)
        );
    }

    // ── Popular Rooms ─────────────────────────────────────────────────────────

    /**
     * GET /api/admin/reports/popular-rooms?hotelId=1
     *
     * مثال الـ response:
     * [
     *   {"roomTypeName": "Deluxe Suite",  "bookingsCount": 18},
     *   {"roomTypeName": "Standard Room", "bookingsCount": 12},
     *   {"roomTypeName": "Family Room",   "bookingsCount": 7}
     * ]
     */
    @GetMapping("/popular-rooms")
    @Operation(
            summary = "Most booked room types for a hotel",
            description = "Returns room types sorted by confirmed bookings — highest first"
    )
    public ResponseEntity<List<PopularRoomDto>> getPopularRooms(
            @RequestParam @NotNull Long hotelId) {

        return ResponseEntity.ok(
                adminReportService.getPopularRooms(hotelId)
        );
    }
}
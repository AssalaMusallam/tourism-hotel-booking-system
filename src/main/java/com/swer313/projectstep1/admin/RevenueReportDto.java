package com.swer313.projectstep1.admin;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO لتقرير الإيرادات.
 * الـ Admin يسأل: "قديش ربحنا من فندق X بين تاريخين؟"
 * الجواب: إجمالي الإيرادات + عدد الحجوزات المؤكدة.
 */
public class RevenueReportDto {

    private final Long       hotelId;       // رقم الفندق
    private final String     hotelName;     // اسم الفندق
    private final LocalDate  from;          // بداية الفترة
    private final LocalDate  to;            // نهاية الفترة
    private final BigDecimal totalRevenue;  // مجموع totalPrice للحجوزات CONFIRMED
    private final String     currency;      // دايماً USD حالياً
    private final Long       totalBookings; // عدد الحجوزات المؤكدة

    public RevenueReportDto(Long hotelId, String hotelName,
                            LocalDate from, LocalDate to,
                            BigDecimal totalRevenue,
                            String currency, Long totalBookings) {
        this.hotelId       = hotelId;
        this.hotelName     = hotelName;
        this.from          = from;
        this.to            = to;
        // لو ما في حجوزات → SUM بترجع null من الـ DB
        // COALESCE في الـ DTO عشان ما نرجع null للـ frontend
        this.totalRevenue  = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.currency      = currency;
        this.totalBookings = totalBookings != null ? totalBookings : 0L;
    }

    public Long       getHotelId()       { return hotelId; }
    public String     getHotelName()     { return hotelName; }
    public LocalDate  getFrom()          { return from; }
    public LocalDate  getTo()            { return to; }
    public BigDecimal getTotalRevenue()  { return totalRevenue; }
    public String     getCurrency()      { return currency; }
    public Long       getTotalBookings() { return totalBookings; }
}
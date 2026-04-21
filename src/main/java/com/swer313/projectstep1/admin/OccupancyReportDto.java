package com.swer313.projectstep1.admin;

/**
 * DTO لتقرير نسبة الإشغال.
 *
 * الـ Admin يسأل: "قديش كانت غرفنا ممتلئة هالشهر؟"
 *
 * الحساب:
 * totalRooms = مجموع totalUnits لكل room types في الفندق
 * مثال: Standard(20) + Deluxe(10) + Suite(5) = 35 غرفة
 *
 * totalAvailableDays = totalRooms × أيام الشهر
 * مثال: 35 غرفة × 30 يوم = 1050 يوم متاح
 *
 * bookedDays = مجموع ليالي الحجوزات CONFIRMED داخل الشهر
 * مثال: 500 ليلة محجوزة
 *
 * occupancyRate = (500 / 1050) × 100 = 47.6%
 */
public class OccupancyReportDto {

    private final Long   hotelId;
    private final String month;         // format: "2026-03"
    private final int    totalRooms;    // مجموع الغرف الفعلية (totalUnits)
    private final double occupancyRate; // نسبة مئوية مثل 47.6

    public OccupancyReportDto(Long hotelId, String month,
                              int totalRooms, double occupancyRate) {
        this.hotelId       = hotelId;
        this.month         = month;
        this.totalRooms    = totalRooms;
        this.occupancyRate = occupancyRate;
    }

    public Long   getHotelId()       { return hotelId; }
    public String getMonth()         { return month; }
    public int    getTotalRooms()    { return totalRooms; }
    public double getOccupancyRate() { return occupancyRate; }
}
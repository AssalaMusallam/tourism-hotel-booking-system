package com.swer313.projectstep1.admin;


import java.time.LocalDate;
import java.util.List;

/**
 * Interface للـ Admin Reports.
 * بيفصل الـ contract عن الـ implementation —
 * لو بدنا نغير طريقة الحساب مستقبلاً بنعدل الـ Impl بس.
 */
public interface AdminReportService {

    // تقرير الإيرادات لفندق في فترة معينة
    RevenueReportDto getRevenueReport(Long hotelId, LocalDate from, LocalDate to);

    // نسبة الإشغال لفندق في شهر معين (format: "2026-03")
    OccupancyReportDto getOccupancyReport(Long hotelId, String month);

    // أشهر أنواع الغرف لفندق — مرتبة تنازلياً
    List<PopularRoomDto> getPopularRooms(Long hotelId);
}
package com.swer313.projectstep1.admin;

import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.catalog.hotel.HotelNotFoundException;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true) // كل العمليات قراءة فقط — أسرع وأأمن
public class AdminReportServiceImpl implements AdminReportService {

    private final BookingRepository  bookingRepository;
    private final HotelRepository    hotelRepository;
    private final RoomTypeRepository roomTypeRepository;

    // Constructor Injection — أفضل من @Autowired
    public AdminReportServiceImpl(BookingRepository bookingRepository,
                                  HotelRepository hotelRepository,
                                  RoomTypeRepository roomTypeRepository) {
        this.bookingRepository  = bookingRepository;
        this.hotelRepository    = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Revenue Report
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public RevenueReportDto getRevenueReport(Long hotelId,
                                             LocalDate from,
                                             LocalDate to) {
        // 1. تحقق إن الفندق موجود — لو مش موجود يرجع 404 تلقائياً
        var hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        // 2. مجموع totalPrice لكل الحجوزات CONFIRMED في الفترة
        //    لو ما في حجوزات → SUM ترجع null من الـ DB
        //    الـ DTO بيحوّلها لـ 0
        BigDecimal total = bookingRepository
                .sumRevenueByHotelAndDateRange(hotelId, from, to);

        // 3. عدد الحجوزات المؤكدة في نفس الفترة
        Long count = bookingRepository
                .countConfirmedByHotelAndDateRange(hotelId, from, to);

        return new RevenueReportDto(
                hotelId,
                hotel.getName(),
                from,
                to,
                total,   // الـ DTO بيتعامل مع null
                "USD",
                count    // الـ DTO بيتعامل مع null
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Occupancy Report
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public OccupancyReportDto getOccupancyReport(Long hotelId, String month) {
        // 1. تحقق إن الفندق موجود
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        // 2. parse الشهر — "2026-03" → YearMonth object
        YearMonth ym = YearMonth.parse(month);

        // 3. أول يوم في الشهر: 2026-03-01
        LocalDate start = ym.atDay(1);

        // 4. أول يوم من الشهر التالي: 2026-04-01
        //    ليش مش آخر يوم الشهر الحالي (2026-03-31)؟
        //    لأن حجز: checkIn=30-Mar, checkOut=02-Apr
        //    → DATEDIFF(LEAST(02-Apr, 01-Apr), GREATEST(30-Mar, 01-Mar))
        //    → DATEDIFF(01-Apr, 30-Mar) = 2 يوم ✅
        //    لو استخدمنا 31-Mar:
        //    → DATEDIFF(31-Mar, 30-Mar) = 1 يوم ❌ ناقص
        LocalDate endExclusive = ym.plusMonths(1).atDay(1);

        // 5. عدد أيام الشهر: 28/29/30/31
        int daysInMonth = ym.lengthOfMonth();

        // 6. مجموع الغرف الفعلية — مش عدد أنواع الغرف
        //    مثال: Standard(20) + Deluxe(10) + Suite(5) = 35 غرفة
        //    لو استخدمنا countByHotel_Id → كان يرجع 3 (عدد الأنواع) ❌
        int totalRooms = roomTypeRepository.sumTotalUnitsByHotelId(hotelId);

        // 7. إجمالي الأيام المتاحة = عدد الغرف × أيام الشهر
        //    مثال: 35 غرفة × 30 يوم = 1050 يوم متاح
        int totalAvailableDays = totalRooms * daysInMonth;

        // 8. مجموع الأيام المحجوزة مع حساب التداخل الصحيح
        //    حجوزات تبدأ قبل الشهر وتنتهي داخله تُحسب بشكل صحيح
        Long bookedDays = bookingRepository
                .sumBookedDaysByHotelAndMonth(hotelId, start, endExclusive);

        // 9. حساب النسبة المئوية
        //    لو ما في غرف أو ما في حجوزات → 0%
        double rate = (totalAvailableDays == 0 || bookedDays == null)
                ? 0.0
                : (bookedDays * 100.0) / totalAvailableDays;

        // 10. تقريب لـ decimal واحد: 47.666 → 47.7
        double rounded = Math.round(rate * 10.0) / 10.0;

        return new OccupancyReportDto(hotelId, month, totalRooms, rounded);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Popular Rooms
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public List<PopularRoomDto> getPopularRooms(Long hotelId) {
        // 1. تحقق إن الفندق موجود
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        // 2. الـ query بترجع أنواع الغرف مرتبة من الأكثر حجزاً للأقل
        //    الـ JPQL بيبني PopularRoomDto مباشرة من الـ query
        return bookingRepository.findPopularRoomTypesByHotel(hotelId);
    }
}
package com.swer313.projectstep1.admin;

import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelNotFoundException;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
    هذا التست هو Unit Test للكلاس AdminReportServiceImpl

    ما معنى Unit Test هنا؟
    يعني نحن نختبر منطق هذا الكلاس وحده فقط، بدون تشغيل Spring كامل،
    وبدون Database حقيقية.

    لذلك سنعمل Mock للـ repositories:
    - BookingRepository
    - HotelRepository
    - RoomTypeRepository

    حتى نتحكم نحن بما ترجع هذه الـ dependencies،
    ثم نرى هل الـ service تتصرف بشكل صحيح أم لا.
*/
@ExtendWith(MockitoExtension.class)
class AdminReportServiceImplTest {

    /*
        @Mock
        تعني: اعملي نسخة وهمية Mock من هذا الـ repository.

        لماذا؟
        لأننا لا نريد أن نصل لقاعدة البيانات فعلياً في Unit Test.
        نحن فقط نريد أن نقول:
        "إذا الريبو رجع كذا... هل السيرفس سترجع النتيجة الصح؟"
     */
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    /*
        @InjectMocks
        يعني أن Mockito سيبني لنا object من AdminReportServiceImpl
        ويحقن داخله الـ mocks السابقة بدل الـ dependencies الحقيقية.

        كأننا عملنا:
        service = new AdminReportServiceImpl(bookingRepository, hotelRepository, roomTypeRepository);
     */
    @InjectMocks
    private AdminReportServiceImpl service;


    // =========================================================
    // Revenue Report Tests
    // =========================================================

    @Test
    void getRevenueReport_shouldReturnRevenueReport_whenHotelExistsAndReposReturnValues() {
        /*
            هذا التست يختبر الحالة الطبيعية الناجحة Success Case.

            السيناريو:
            - الفندق موجود
            - مجموع الإيرادات موجود
            - عدد الحجوزات موجود

            المتوقع:
            - السيرفس ترجع RevenueReportDto فيه كل القيم الصحيحة
         */

        Long hotelId = 1L;

        // نجهز Hotel وهمي لأن السيرفس تتأكد أولاً أن الفندق موجود
        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("Test Hotel");

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        /*
            when(...).thenReturn(...)
            معناها:
            لما السيرفس تنادي هذا الميثود من الريبو، رجع لها هذه القيمة.

            هنا نقول:
            - لو طلب الفندق بالـ id = 1 رجع الفندق الذي جهزناه
            - لو طلب مجموع الإيرادات رجع 1234.56
            - لو طلب عدد الحجوزات رجع 5
         */
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(bookingRepository.sumRevenueByHotelAndDateRange(hotelId, from, to))
                .thenReturn(new BigDecimal("1234.56"));
        when(bookingRepository.countConfirmedByHotelAndDateRange(hotelId, from, to))
                .thenReturn(5L);

        // Act = استدعاء الميثود التي نختبرها
        RevenueReportDto dto = service.getRevenueReport(hotelId, from, to);

        /*
            Assert = نتحقق من النتيجة

            assertNotNull
            للتأكد أن النتيجة نفسها ليست null

            assertEquals
            للتأكد أن كل قيمة تم بناؤها بشكل صحيح داخل DTO
         */
        assertNotNull(dto);
        assertEquals(hotelId, dto.getHotelId());
        assertEquals("Test Hotel", dto.getHotelName());
        assertEquals(from, dto.getFrom());
        assertEquals(to, dto.getTo());
        assertEquals(new BigDecimal("1234.56"), dto.getTotalRevenue());
        assertEquals("USD", dto.getCurrency());
        assertEquals(5L, dto.getTotalBookings());

        /*
            verify
            هنا نتحقق أن السيرفس فعلًا استدعت الميثودات المطلوبة من الـ repositories.

            هذا مفيد لأنه يثبت أن السيرفس لم تتجاهل أي خطوة أساسية في المنطق.
         */
        verify(hotelRepository).findById(hotelId);
        verify(bookingRepository).sumRevenueByHotelAndDateRange(hotelId, from, to);
        verify(bookingRepository).countConfirmedByHotelAndDateRange(hotelId, from, to);
    }

    @Test
    void getRevenueReport_shouldThrowHotelNotFoundException_whenHotelDoesNotExist() {
        /*
            هذا التست يختبر حالة الخطأ:
            إذا الفندق غير موجود، يجب أن ترمي السيرفس HotelNotFoundException

            هذا مهم لأن الكود الحقيقي يفعل:
            hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));
         */

        Long hotelId = 2L;

        // هنا نقول: الفندق غير موجود
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now();

        /*
            assertThrows
            نستخدمها عندما نتوقع Exception.

            إذا لم يرمِ الكود exception، يفشل التست.
         */
        assertThrows(HotelNotFoundException.class, () ->
                service.getRevenueReport(hotelId, from, to));

        verify(hotelRepository).findById(hotelId);

        /*
            verifyNoMoreInteractions(bookingRepository)
            معناها:
            بما أن الفندق غير موجود، المفروض السيرفس لا تكمل وتروح تسأل bookingRepository.
            إذا راحت، فهذا يدل على خلل في التسلسل المنطقي.
         */
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getRevenueReport_shouldReturnZeroTotalRevenue_whenRepositoryReturnsNull() {
        /*
            هذا التست مهم جدًا لأنه يغطي null handling.

            في الكود الحقيقي:
            bookingRepository.sumRevenueByHotelAndDateRange(...) قد ترجع null
            لو لم توجد حجوزات.

            والسيرفس تمرر هذه القيمة إلى RevenueReportDto،
            والـ DTO نفسه يحول null إلى BigDecimal.ZERO. :contentReference[oaicite:2]{index=2}

            إذًا نحن هنا نختبر هذا السيناريو.
         */

        Long hotelId = 3L;

        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("H3");

        LocalDate from = LocalDate.of(2026, 2, 1);
        LocalDate to = LocalDate.of(2026, 2, 28);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        // هنا الإيرادات null
        when(bookingRepository.sumRevenueByHotelAndDateRange(hotelId, from, to)).thenReturn(null);

        // لكن عدد الحجوزات موجود
        when(bookingRepository.countConfirmedByHotelAndDateRange(hotelId, from, to)).thenReturn(2L);

        RevenueReportDto dto = service.getRevenueReport(hotelId, from, to);

        // المتوقع أن totalRevenue تتحول إلى ZERO بدل null
        assertEquals(BigDecimal.ZERO, dto.getTotalRevenue());
        assertEquals(2L, dto.getTotalBookings());
    }

    @Test
    void getRevenueReport_shouldReturnZeroTotalBookings_whenRepositoryReturnsNull() {
        /*
            نفس الفكرة السابقة، لكن هذه المرة countConfirmed... ترجع null

            والـ RevenueReportDto يجب أن يحول null إلى 0L. :contentReference[oaicite:3]{index=3}
         */

        Long hotelId = 4L;

        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("H4");

        LocalDate from = LocalDate.of(2026, 2, 1);
        LocalDate to = LocalDate.of(2026, 2, 28);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(bookingRepository.sumRevenueByHotelAndDateRange(hotelId, from, to)).thenReturn(new BigDecimal("10"));
        when(bookingRepository.countConfirmedByHotelAndDateRange(hotelId, from, to)).thenReturn(null);

        RevenueReportDto dto = service.getRevenueReport(hotelId, from, to);

        assertEquals(0L, dto.getTotalBookings());
        assertEquals(new BigDecimal("10"), dto.getTotalRevenue());
    }


    // =========================================================
    // Occupancy Report Tests
    // =========================================================

    @Test
    void getOccupancyReport_shouldParseMonthCorrectlyAndCallRepositoryWithMonthRange() {
        /*
            هذا من أفضل التستات في هذا الكلاس.

            لماذا؟
            لأن السيرفس تستقبل month كـ String مثل:
            "2026-03"

            ثم تحولها داخلياً إلى:
            start = 2026-03-01
            endExclusive = 2026-04-01

            وبعدها ترسل هذه القيم إلى bookingRepository. :contentReference[oaicite:4]{index=4}

            نحن هنا لا نكتفي بالنتيجة النهائية،
            بل نتأكد أن التحويل نفسه تم بشكل صحيح.
         */

        Long hotelId = 13L;

        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("Month Hotel");

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.sumTotalUnitsByHotelId(hotelId)).thenReturn(10);
        when(bookingRepository.sumBookedDaysByHotelAndMonth(eq(hotelId), any(), any())).thenReturn(50L);

        service.getOccupancyReport(hotelId, "2026-03");

        /*
            هنا نتحقق أن bookingRepository تم استدعاؤها بالضبط بهذه التواريخ:
            start = 2026-03-01
            endExclusive = 2026-04-01
         */
        verify(bookingRepository).sumBookedDaysByHotelAndMonth(
                eq(hotelId),
                eq(LocalDate.of(2026, 3, 1)),
                eq(LocalDate.of(2026, 4, 1))
        );
    }

    @Test
    void getOccupancyReport_shouldThrowHotelNotFoundException_whenHotelDoesNotExist() {
        /*
            مثل revenue:
            إذا الفندق غير موجود، يجب أن يرمى exception مباشرة.
         */

        Long hotelId = 5L;
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class, () ->
                service.getOccupancyReport(hotelId, "2026-03"));
    }

    @Test
    void getOccupancyReport_shouldReturnReportWithCorrectTotalRoomsAndOccupancyRate() {
        /*
            هذا هو الـ success case لتقرير الإشغال.

            الكود الحقيقي يحسب:
            totalRooms = مجموع total units
            totalAvailableDays = totalRooms * daysInMonth
            occupancyRate = bookedDays * 100 / totalAvailableDays
            ثم يقرب النتيجة إلى منزلة عشرية واحدة. :contentReference[oaicite:5]{index=5}
         */

        Long hotelId = 6L;

        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("Occ Hotel");

        String month = "2026-03"; // مارس = 31 يوم
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.sumTotalUnitsByHotelId(hotelId)).thenReturn(35);

        int daysInMonth = 31;
        int totalRooms = 35;
        int totalAvailableDays = totalRooms * daysInMonth;

        // لنفترض أن مجموع الأيام المحجوزة = 500
        Long bookedDays = 500L;

        when(bookingRepository.sumBookedDaysByHotelAndMonth(eq(hotelId), any(), any()))
                .thenReturn(bookedDays);

        OccupancyReportDto dto = service.getOccupancyReport(hotelId, month);

        assertEquals(hotelId, dto.getHotelId());
        assertEquals(month, dto.getMonth());
        assertEquals(totalRooms, dto.getTotalRooms());

        double expectedRate = (bookedDays * 100.0) / totalAvailableDays;
        double expectedRounded = Math.round(expectedRate * 10.0) / 10.0;

        assertEquals(expectedRounded, dto.getOccupancyRate());
    }

    @Test
    void getOccupancyReport_shouldReturnZeroOccupancyRate_whenTotalRoomsIsZero() {
        /*
            حالة Edge Case مهمة:
            لو الفندق لا يملك غرفًا فعلية totalRooms = 0
            يجب أن تكون النسبة 0.0
            لتجنب القسمة على صفر.

            وهذا بالفعل ما يفعله الكود الحقيقي. :contentReference[oaicite:6]{index=6}
         */

        Long hotelId = 7L;

        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("ZeroRooms");

        String month = "2026-03";

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.sumTotalUnitsByHotelId(hotelId)).thenReturn(0);
        when(bookingRepository.sumBookedDaysByHotelAndMonth(eq(hotelId), any(), any())).thenReturn(100L);

        OccupancyReportDto dto = service.getOccupancyReport(hotelId, month);

        assertEquals(0, dto.getTotalRooms());
        assertEquals(0.0, dto.getOccupancyRate());
    }

    @Test
    void getOccupancyReport_shouldReturnZeroOccupancyRate_whenBookedDaysIsNull() {
        /*
            حالة مهمة أخرى:
            لو bookingRepository رجعت null في bookedDays
            يجب أن تكون occupancyRate = 0.0

            أيضًا هذا مطابق للكود الحقيقي. :contentReference[oaicite:7]{index=7}
         */

        Long hotelId = 8L;

        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("NullBookedDays");

        String month = "2026-03";

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.sumTotalUnitsByHotelId(hotelId)).thenReturn(10);
        when(bookingRepository.sumBookedDaysByHotelAndMonth(eq(hotelId), any(), any())).thenReturn(null);

        OccupancyReportDto dto = service.getOccupancyReport(hotelId, month);

        assertEquals(10, dto.getTotalRooms());
        assertEquals(0.0, dto.getOccupancyRate());
    }

    @Test
    void getOccupancyReport_shouldRoundOccupancyRateToOneDecimalPlace() {
        /*
            هذا التست يثبت أن النتيجة النهائية مقربة إلى منزلة عشرية واحدة.

            حتى لو كانت العملية الحسابية تنتج عددًا فيه كسور كثيرة،
            الكود الحقيقي يعمل:
            Math.round(rate * 10.0) / 10.0 :contentReference[oaicite:8]{index=8}

            لذلك نعيد نفس الحساب في التست ونتأكد أن النتيجة النهائية متطابقة.
         */

        Long hotelId = 9L;

        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("RoundHotel");

        String month = "2026-03"; // 31 يوم

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.sumTotalUnitsByHotelId(hotelId)).thenReturn(35);

        int daysInMonth = 31;
        int totalAvailableDays = 35 * daysInMonth;

        Long bookedDays = 500L;
        when(bookingRepository.sumBookedDaysByHotelAndMonth(eq(hotelId), any(), any())).thenReturn(bookedDays);

        OccupancyReportDto dto = service.getOccupancyReport(hotelId, month);

        double rawRate = (bookedDays * 100.0) / totalAvailableDays;
        double expectedRounded = Math.round(rawRate * 10.0) / 10.0;

        assertEquals(expectedRounded, dto.getOccupancyRate());
    }


    // =========================================================
    // Popular Rooms Tests
    // =========================================================

    @Test
    void getPopularRooms_shouldThrowHotelNotFoundException_whenHotelDoesNotExist() {
        /*
            أول خطوة في getPopularRooms هي التأكد من وجود الفندق.
            لذلك إذا الفندق غير موجود، يجب رمي HotelNotFoundException. :contentReference[oaicite:9]{index=9}
         */

        Long hotelId = 10L;
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class, () -> service.getPopularRooms(hotelId));
    }

    @Test
    void getPopularRooms_shouldReturnListOfPopularRoomDto_whenHotelExists() {
        /*
            هنا نختبر الحالة الطبيعية:
            - الفندق موجود
            - bookingRepository ترجع قائمة بأنواع الغرف الأكثر حجزًا
            - السيرفس يجب أن ترجع نفس القائمة
         */

        Long hotelId = 11L;

        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("PopHotel");

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        PopularRoomDto p1 = new PopularRoomDto("Standard", 20L);
        PopularRoomDto p2 = new PopularRoomDto("Deluxe", 10L);

        when(bookingRepository.findPopularRoomTypesByHotel(hotelId)).thenReturn(List.of(p1, p2));

        List<PopularRoomDto> result = service.getPopularRooms(hotelId);

        assertNotNull(result);
        assertEquals(2, result.size());

        // نتحقق من أول عنصر للتأكد أن القائمة وصلت صحيحة
        assertEquals("Standard", result.get(0).getRoomTypeName());
        assertEquals(20L, result.get(0).getBookingsCount());

        verify(bookingRepository).findPopularRoomTypesByHotel(hotelId);
    }

    @Test
    void getPopularRooms_shouldReturnEmptyList_whenRepositoryReturnsEmpty() {
        /*
            Edge Case:
            لو ما في غرف محجوزة أو الاستعلام رجع قائمة فارغة،
            فالمفروض السيرفس ترجع قائمة فارغة، وليس null.
         */

        Long hotelId = 12L;

        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("EmptyPop");

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(bookingRepository.findPopularRoomTypesByHotel(hotelId)).thenReturn(List.of());

        List<PopularRoomDto> result = service.getPopularRooms(hotelId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
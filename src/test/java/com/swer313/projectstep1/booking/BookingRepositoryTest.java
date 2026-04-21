package com.swer313.projectstep1.booking;

import com.swer313.projectstep1.admin.PopularRoomDto;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.room.BedType;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private com.swer313.projectstep1.catalog.hotel.HotelRepository hotelRepository;

    private Hotel buildHotel(String name) {
        Hotel h = new Hotel();
        h.setName(name);
        h.setAddress("Addr");
        h.setCity("City");
        h.setCountry("Country");
        return h;
    }

    private RoomType buildRoomType(Hotel hotel, String name, BigDecimal basePrice) {
        RoomType rt = new RoomType();
        rt.setHotel(hotel);
        rt.setName(name);
        rt.setCapacity(2);
        rt.setBedType(BedType.KING);
        rt.setBedCount(1);
        rt.setMaxAdults(2);
        rt.setMaxChildren(1);
        rt.setBasePrice(basePrice);
        rt.setTotalUnits(3);
        rt.setStatus(RoomTypeStatus.ACTIVE);
        return rt;
    }

    @Test
    @DisplayName("findByGuestEmailIgnoreCase_returns_bookings_ignoring_case")
    void findByGuestEmailIgnoreCase_returns_bookings_ignoring_case() {
        bookingRepository.deleteAll();

        Hotel h = hotelRepository.save(buildHotel("H1"));
        RoomType rt = roomTypeRepository.save(buildRoomType(h, "RT1", new BigDecimal("50.00")));

        Booking b = new Booking();
        b.setRoomType(rt);
        b.setGuestName("Alice");
        b.setGuestEmail("alice@example.com");
        b.setGuestPhone("+123456789");
        b.setAdults(1);
        b.setChildren(0);
        b.setCheckIn(LocalDate.now().plusDays(5));
        b.setCheckOut(LocalDate.now().plusDays(6));
        b.setPricePerNight(rt.getBasePrice());
        b.setTotalPrice(new BigDecimal("50.00"));
        b.setStatus(BookingStatus.PENDING);
        bookingRepository.save(b);

        var page = bookingRepository.findByGuestEmailIgnoreCase("ALICE@EXAMPLE.COM", PageRequest.of(0, 10));

        assertFalse(page.getContent().isEmpty());
        assertEquals("alice@example.com", page.getContent().get(0).getGuestEmail());
    }

    @Test
    @DisplayName("findByGuestEmailIgnoreCaseAndStatus_filters_by_status")
    void findByGuestEmailIgnoreCaseAndStatus_filters_by_status() {
        bookingRepository.deleteAll();

        Hotel h = hotelRepository.save(buildHotel("H2"));
        RoomType rt = roomTypeRepository.save(buildRoomType(h, "RT2", new BigDecimal("60.00")));

        Booking b1 = new Booking();
        b1.setRoomType(rt);
        b1.setGuestName("Bob");
        b1.setGuestEmail("bob@example.com");
        b1.setGuestPhone("+123456789");
        b1.setAdults(1);
        b1.setChildren(0);
        b1.setCheckIn(LocalDate.now().plusDays(3));
        b1.setCheckOut(LocalDate.now().plusDays(4));
        b1.setPricePerNight(rt.getBasePrice());
        b1.setTotalPrice(new BigDecimal("60.00"));
        b1.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setRoomType(rt);
        b2.setGuestName("Bobby");
        b2.setGuestEmail("bob@example.com");
        b2.setGuestPhone("+123456780");
        b2.setAdults(1);
        b2.setChildren(0);
        b2.setCheckIn(LocalDate.now().plusDays(5));
        b2.setCheckOut(LocalDate.now().plusDays(6));
        b2.setPricePerNight(rt.getBasePrice());
        b2.setTotalPrice(new BigDecimal("60.00"));
        b2.setStatus(BookingStatus.PENDING);
        bookingRepository.save(b2);

        var confirmed = bookingRepository.findByGuestEmailIgnoreCaseAndStatus(
                "bob@example.com",
                BookingStatus.CONFIRMED,
                PageRequest.of(0, 10)
        );

        assertEquals(1, confirmed.getTotalElements());
        assertEquals(BookingStatus.CONFIRMED, confirmed.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("findByRoomType_Hotel_Id_returns_bookings_for_hotel")
    void findByRoomType_Hotel_Id_returns_bookings_for_hotel() {
        bookingRepository.deleteAll();

        Hotel h = hotelRepository.save(buildHotel("H3"));
        RoomType rt = roomTypeRepository.save(buildRoomType(h, "RT3", new BigDecimal("80.00")));

        Booking b = new Booking();
        b.setRoomType(rt);
        b.setGuestName("Carl");
        b.setGuestEmail("carl@example.com");
        b.setGuestPhone("+123456781");
        b.setAdults(1);
        b.setChildren(0);
        b.setCheckIn(LocalDate.now().plusDays(7));
        b.setCheckOut(LocalDate.now().plusDays(8));
        b.setPricePerNight(rt.getBasePrice());
        b.setTotalPrice(new BigDecimal("80.00"));
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(b);

        var page = bookingRepository.findByRoomType_Hotel_Id(h.getId(), PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals(h.getId(), page.getContent().get(0).getRoomType().getHotel().getId());
    }

    @Test
    @DisplayName("findByRoomType_Hotel_IdAndStatus_filters_by_status")
    void findByRoomType_Hotel_IdAndStatus_filters_by_status() {
        bookingRepository.deleteAll();

        Hotel h = hotelRepository.save(buildHotel("H4"));
        RoomType rt = roomTypeRepository.save(buildRoomType(h, "RT4", new BigDecimal("90.00")));

        Booking b = new Booking();
        b.setRoomType(rt);
        b.setGuestName("Dana");
        b.setGuestEmail("dana@example.com");
        b.setGuestPhone("+123456782");
        b.setAdults(1);
        b.setChildren(0);
        b.setCheckIn(LocalDate.now().plusDays(2));
        b.setCheckOut(LocalDate.now().plusDays(3));
        b.setPricePerNight(rt.getBasePrice());
        b.setTotalPrice(new BigDecimal("90.00"));
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(b);

        var page = bookingRepository.findByRoomType_Hotel_IdAndStatus(
                h.getId(),
                BookingStatus.CONFIRMED,
                PageRequest.of(0, 10)
        );

        assertEquals(1, page.getTotalElements());
    }

    @Test
    @DisplayName("findConfirmedWithRoomTypeAndHotel_returns_confirmed_for_given_checkin")
    void findConfirmedWithRoomTypeAndHotel_returns_confirmed_for_given_checkin() {
        bookingRepository.deleteAll();

        LocalDate target = LocalDate.now().plusDays(10);
        Hotel h = hotelRepository.save(buildHotel("H5"));
        RoomType rt = roomTypeRepository.save(buildRoomType(h, "RT5", new BigDecimal("110.00")));

        Booking b = new Booking();
        b.setRoomType(rt);
        b.setGuestName("Eman");
        b.setGuestEmail("eman@example.com");
        b.setGuestPhone("+123456783");
        b.setAdults(1);
        b.setChildren(0);
        b.setCheckIn(target);
        b.setCheckOut(target.plusDays(2));
        b.setPricePerNight(rt.getBasePrice());
        b.setTotalPrice(new BigDecimal("220.00"));
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(b);

        List<Booking> found = bookingRepository.findConfirmedWithRoomTypeAndHotel(
                BookingStatus.CONFIRMED,
                target
        );

        assertEquals(1, found.size());
        assertEquals(target, found.get(0).getCheckIn());
    }

    @Test
    @DisplayName("sumRevenueByHotelAndDateRange_returns_value_for_confirmed_bookings")
    void sumRevenueByHotelAndDateRange_returns_value_for_confirmed_bookings() {
        bookingRepository.deleteAll();

        Hotel h = hotelRepository.save(buildHotel("H6"));
        RoomType rt = roomTypeRepository.save(buildRoomType(h, "RT6", new BigDecimal("120.00")));

        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(10);

        Booking b = new Booking();
        b.setRoomType(rt);
        b.setGuestName("Fadi");
        b.setGuestEmail("fadi@example.com");
        b.setGuestPhone("+123456784");
        b.setAdults(1);
        b.setChildren(0);
        b.setCheckIn(LocalDate.now().plusDays(2));
        b.setCheckOut(LocalDate.now().plusDays(4));
        b.setPricePerNight(rt.getBasePrice());
        b.setTotalPrice(new BigDecimal("240.00"));
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(b);

        BigDecimal revenue = bookingRepository.sumRevenueByHotelAndDateRange(h.getId(), from, to);

        assertNotNull(revenue);
        assertTrue(revenue.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("countConfirmedByHotelAndDateRange_counts_only_confirmed")
    void countConfirmedByHotelAndDateRange_counts_only_confirmed() {
        bookingRepository.deleteAll();

        Hotel h = hotelRepository.save(buildHotel("H7"));
        RoomType rt = roomTypeRepository.save(buildRoomType(h, "RT7", new BigDecimal("130.00")));

        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(10);

        Booking b = new Booking();
        b.setRoomType(rt);
        b.setGuestName("Gina");
        b.setGuestEmail("gina@example.com");
        b.setGuestPhone("+123456785");
        b.setAdults(1);
        b.setChildren(0);
        b.setCheckIn(LocalDate.now().plusDays(2));
        b.setCheckOut(LocalDate.now().plusDays(3));
        b.setPricePerNight(rt.getBasePrice());
        b.setTotalPrice(new BigDecimal("130.00"));
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(b);

        Long cnt = bookingRepository.countConfirmedByHotelAndDateRange(h.getId(), from, to);

        assertEquals(1L, cnt);
    }

    @Test
    @DisplayName("sumBookedDaysByHotelAndMonth_handles_spanning_bookings_and_returns_non_negative")
    void sumBookedDaysByHotelAndMonth_handles_spanning_bookings_and_returns_non_negative() {
        bookingRepository.deleteAll();

        Hotel h = hotelRepository.save(buildHotel("H8"));
        RoomType rt = roomTypeRepository.save(buildRoomType(h, "RT8", new BigDecimal("140.00")));

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endExclusive = startOfMonth.plusMonths(1);
        LocalDate checkIn = startOfMonth.minusDays(2);
        LocalDate checkOut = startOfMonth.plusDays(3);

        Booking b = new Booking();
        b.setRoomType(rt);
        b.setGuestName("Hani");
        b.setGuestEmail("hani@example.com");
        b.setGuestPhone("+123456786");
        b.setAdults(1);
        b.setChildren(0);
        b.setCheckIn(checkIn);
        b.setCheckOut(checkOut);
        b.setPricePerNight(rt.getBasePrice());
        b.setTotalPrice(new BigDecimal("420.00"));
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(b);

        Long days = bookingRepository.sumBookedDaysByHotelAndMonth(h.getId(), startOfMonth, endExclusive);

        assertNotNull(days);
        assertTrue(days >= 0);
    }

    @Test
    @DisplayName("findPopularRoomTypesByHotel_returns_roomtypes_ordered_by_count")
    void findPopularRoomTypesByHotel_returns_roomtypes_ordered_by_count() {
        bookingRepository.deleteAll();

        Hotel h = hotelRepository.save(buildHotel("H9"));
        RoomType rt1 = roomTypeRepository.save(buildRoomType(h, "MostBooked", new BigDecimal("150.00")));
        RoomType rt2 = roomTypeRepository.save(buildRoomType(h, "LessBooked", new BigDecimal("80.00")));

        Booking a1 = new Booking();
        a1.setRoomType(rt1);
        a1.setGuestName("Adam");
        a1.setGuestEmail("a1@example.com");
        a1.setGuestPhone("+123456787");
        a1.setAdults(1);
        a1.setChildren(0);
        a1.setCheckIn(LocalDate.now().plusDays(1));
        a1.setCheckOut(LocalDate.now().plusDays(2));
        a1.setPricePerNight(rt1.getBasePrice());
        a1.setTotalPrice(new BigDecimal("150.00"));
        a1.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(a1);

        Booking a2 = new Booking();
        a2.setRoomType(rt1);
        a2.setGuestName("Amin");
        a2.setGuestEmail("a2@example.com");
        a2.setGuestPhone("+123456788");
        a2.setAdults(1);
        a2.setChildren(0);
        a2.setCheckIn(LocalDate.now().plusDays(3));
        a2.setCheckOut(LocalDate.now().plusDays(4));
        a2.setPricePerNight(rt1.getBasePrice());
        a2.setTotalPrice(new BigDecimal("150.00"));
        a2.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(a2);

        Booking b = new Booking();
        b.setRoomType(rt2);
        b.setGuestName("Bashar");
        b.setGuestEmail("b@example.com");
        b.setGuestPhone("+123456789");
        b.setAdults(1);
        b.setChildren(0);
        b.setCheckIn(LocalDate.now().plusDays(5));
        b.setCheckOut(LocalDate.now().plusDays(6));
        b.setPricePerNight(rt2.getBasePrice());
        b.setTotalPrice(new BigDecimal("80.00"));
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(b);

        List<PopularRoomDto> popular = bookingRepository.findPopularRoomTypesByHotel(h.getId());

        assertFalse(popular.isEmpty());
        assertEquals("MostBooked", popular.get(0).getRoomTypeName());
        assertTrue(popular.get(0).getBookingsCount() >= 2L);
    }
}
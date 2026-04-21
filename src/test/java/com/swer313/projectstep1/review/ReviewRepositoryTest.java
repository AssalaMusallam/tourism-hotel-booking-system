package com.swer313.projectstep1.review;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.room.BedType;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository repository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Test
    void getRatingSummaryByHotelId_calculatesCorrectly() {
        Hotel hotel = new Hotel();
        hotel.setName("HT");
        hotel.setCity("Amman");
        hotel.setCountry("Jordan");
        hotel.setAddress("Main Street");
        hotel.setStatus(Hotel.Status.ACTIVE);
        hotel = hotelRepository.saveAndFlush(hotel);

        RoomType rt = new RoomType();
        rt.setName("RT");
        rt.setHotel(hotel);
        rt.setBasePrice(new BigDecimal("100.00"));
        rt.setCapacity(2);
        rt.setMaxAdults(2);
        rt.setMaxChildren(1);
        rt.setTotalUnits(5);
        rt.setBedCount(1);
        rt.setBedType(BedType.KING);
        rt.setStatus(RoomTypeStatus.ACTIVE);
        rt = roomTypeRepository.saveAndFlush(rt);

        Booking b1 = new Booking();
        b1.setRoomType(rt);
        b1.setGuestName("G1");
        b1.setGuestEmail("a@b.com");
        b1.setGuestPhone("0599000001");
        b1.setAdults(2);
        b1.setChildren(0);
        b1.setCheckIn(LocalDate.now().minusDays(5));
        b1.setCheckOut(LocalDate.now().minusDays(3));
        b1.setPricePerNight(new BigDecimal("100.00"));
        b1.setTotalPrice(new BigDecimal("200.00"));
        b1.setTotalGuests(2);
        b1.setStatus(BookingStatus.COMPLETED);
        b1 = bookingRepository.saveAndFlush(b1);

        Review r1 = new Review();
        r1.setBooking(b1);
        r1.setHotelId(hotel.getId());
        r1.setGuestEmail("a@b.com");
        r1.setRating(5);
        r1.setComment("Great");
        repository.saveAndFlush(r1);

        Booking b2 = new Booking();
        b2.setRoomType(rt);
        b2.setGuestName("G2");
        b2.setGuestEmail("c@d.com");
        b2.setGuestPhone("0599000002");
        b2.setAdults(2);
        b2.setChildren(0);
        b2.setCheckIn(LocalDate.now().minusDays(4));
        b2.setCheckOut(LocalDate.now().minusDays(2));
        b2.setPricePerNight(new BigDecimal("100.00"));
        b2.setTotalPrice(new BigDecimal("200.00"));
        b2.setTotalGuests(2);
        b2.setStatus(BookingStatus.COMPLETED);
        b2 = bookingRepository.saveAndFlush(b2);

        Review r2 = new Review();
        r2.setBooking(b2);
        r2.setHotelId(hotel.getId());
        r2.setGuestEmail("c@d.com");
        r2.setRating(3);
        r2.setComment("Okay");
        repository.saveAndFlush(r2);

        Object[] raw = repository.getRatingSummaryByHotelId(hotel.getId());
        assertThat(raw).isNotNull();

        Object[] row = raw;
        if (raw.length == 1 && raw[0] instanceof Object[] nested) {
            row = nested;
        }

        assertThat(row.length).isGreaterThanOrEqualTo(7);
        assertThat(((Number) row[1]).longValue()).isEqualTo(2L);
        assertThat(((Number) row[2]).longValue()).isEqualTo(1L);
        assertThat(((Number) row[4]).longValue()).isEqualTo(1L);
    }
}
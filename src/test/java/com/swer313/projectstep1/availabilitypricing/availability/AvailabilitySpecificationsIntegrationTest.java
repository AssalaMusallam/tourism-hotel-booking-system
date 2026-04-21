package com.swer313.projectstep1.availabilitypricing.availability;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AvailabilitySpecificationsIntegrationTest {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Test
    void hotelId_filter_returnsOnlyRoomTypesForGivenHotel() {
        Hotel h1 = new Hotel(); h1.setName("H1"); h1.setAddress("A"); h1.setCity("C"); h1.setCountry("X");
        Hotel h2 = new Hotel(); h2.setName("H2"); h2.setAddress("A"); h2.setCity("C"); h2.setCountry("X");
        hotelRepository.saveAll(List.of(h1, h2));

        RoomType r1 = new RoomType(); r1.setHotel(h1); r1.setName("R1"); r1.setTotalUnits(2); r1.setCapacity(2); r1.setBasePrice(java.math.BigDecimal.ZERO);
        RoomType r2 = new RoomType(); r2.setHotel(h2); r2.setName("R2"); r2.setTotalUnits(2); r2.setCapacity(2); r2.setBasePrice(java.math.BigDecimal.ZERO);
        roomTypeRepository.saveAll(List.of(r1, r2));

        var spec = AvailabilitySpecifications.hotelAvailabilityFilter(h1.getId(), null, null, null, null, null);
        var page = roomTypeRepository.findAll(spec, PageRequest.of(0,10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getHotelId()).isEqualTo(h1.getId());
    }

    @Test
    void status_and_capacity_and_qLike_filters_work() {
        Hotel h = new Hotel(); h.setName("H"); h.setAddress("A"); h.setCity("C"); h.setCountry("X");
        hotelRepository.save(h);

        RoomType active = new RoomType(); active.setHotel(h); active.setName("Deluxe"); active.setDescription("Nice"); active.setPolicies("p");
        active.setTotalUnits(3); active.setCapacity(4); active.setBasePrice(java.math.BigDecimal.ZERO);
        RoomType inactive = new RoomType(); inactive.setHotel(h); inactive.setName("Old"); inactive.setTotalUnits(1); inactive.setCapacity(1); inactive.setBasePrice(java.math.BigDecimal.ZERO);
        inactive.setStatus(com.swer313.projectstep1.catalog.room.RoomTypeStatus.INACTIVE);
        roomTypeRepository.saveAll(List.of(active, inactive));

        // capacity >= 2 should include 'active' only
        var spec = AvailabilitySpecifications.hotelAvailabilityFilter(h.getId(), 2, "del", null, null, null);
        var page = roomTypeRepository.findAll(spec, PageRequest.of(0,10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName().toLowerCase()).contains("deluxe");
    }

    @Test
    void hasAvailableUnits_subquery_filters_out_fully_booked_roomTypes() {
        Hotel h = new Hotel(); h.setName("H"); h.setAddress("A"); h.setCity("C"); h.setCountry("X");
        hotelRepository.save(h);

        RoomType rt = new RoomType(); rt.setHotel(h); rt.setName("Rooms"); rt.setTotalUnits(2); rt.setCapacity(2); rt.setBasePrice(java.math.BigDecimal.ZERO);
        roomTypeRepository.save(rt);

        // create two bookings that fully book the units for the period
        Booking b1 = new Booking(); b1.setRoomType(rt); b1.setGuestName("G1"); b1.setGuestEmail("a@x.com"); b1.setGuestPhone("p"); b1.setAdults(1); b1.setChildren(0);
        b1.setCheckIn(LocalDate.of(2026,4,10)); b1.setCheckOut(LocalDate.of(2026,4,12)); b1.setPricePerNight(java.math.BigDecimal.ZERO); b1.setTotalPrice(java.math.BigDecimal.ZERO); b1.setStatus(BookingStatus.CONFIRMED);
        Booking b2 = new Booking(); b2.setRoomType(rt); b2.setGuestName("G2"); b2.setGuestEmail("b@x.com"); b2.setGuestPhone("p"); b2.setAdults(1); b2.setChildren(0);
        b2.setCheckIn(LocalDate.of(2026,4,10)); b2.setCheckOut(LocalDate.of(2026,4,12)); b2.setPricePerNight(java.math.BigDecimal.ZERO); b2.setTotalPrice(java.math.BigDecimal.ZERO); b2.setStatus(BookingStatus.CONFIRMED);
        availabilityRepository.saveAll(List.of(b1, b2));

        // Request availableOnly = true should exclude this room type
        var spec = AvailabilitySpecifications.hotelAvailabilityFilter(h.getId(), null, null, true, LocalDate.of(2026,4,10), LocalDate.of(2026,4,12));
        var page = roomTypeRepository.findAll(spec, PageRequest.of(0,10));
        assertThat(page.getContent()).isEmpty();
    }
}


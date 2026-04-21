package com.swer313.projectstep1.catalog.room;

import com.swer313.projectstep1.catalog.hotel.Hotel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoomTypeRepositoryTest {

    @Autowired
    private RoomTypeRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void existsByHotel_IdAndNameIgnoreCase_returnsTrueWhenExists() {
        Hotel hotel = persistHotel("Hotel A");
        persistRoomType(hotel, "Deluxe", RoomTypeStatus.ACTIVE, 5);

        boolean exists = repository.existsByHotel_IdAndNameIgnoreCase(hotel.getId(), "deluxe");

        assertTrue(exists);
    }

    @Test
    void existsByHotel_IdAndNameIgnoreCase_returnsFalseWhenNotExists() {
        Hotel hotel = persistHotel("Hotel A");

        boolean exists = repository.existsByHotel_IdAndNameIgnoreCase(hotel.getId(), "deluxe");

        assertFalse(exists);
    }

    @Test
    void existsByHotel_IdAndNameIgnoreCaseAndIdNot_returnsTrueForDifferentEntitySameName() {
        Hotel hotel = persistHotel("Hotel A");
        RoomType rt1 = persistRoomType(hotel, "Deluxe", RoomTypeStatus.ACTIVE, 5);
        RoomType rt2 = persistRoomType(hotel, "Suite", RoomTypeStatus.ACTIVE, 3);

        boolean exists = repository.existsByHotel_IdAndNameIgnoreCaseAndIdNot(
                hotel.getId(), "Deluxe", rt2.getId()
        );

        assertTrue(exists);
        assertNotEquals(rt1.getId(), rt2.getId());
    }

    @Test
    void existsByHotel_IdAndNameIgnoreCaseAndIdNot_returnsFalseForSameEntity() {
        Hotel hotel = persistHotel("Hotel A");
        RoomType rt1 = persistRoomType(hotel, "Deluxe", RoomTypeStatus.ACTIVE, 5);

        boolean exists = repository.existsByHotel_IdAndNameIgnoreCaseAndIdNot(
                hotel.getId(), "Deluxe", rt1.getId()
        );

        assertFalse(exists);
    }

    @Test
    void findByIdWithLock_returnsEntity() {
        Hotel hotel = persistHotel("Hotel A");
        RoomType roomType = persistRoomType(hotel, "Deluxe", RoomTypeStatus.ACTIVE, 5);

        Optional<RoomType> result = repository.findByIdWithLock(roomType.getId());

        assertTrue(result.isPresent());
        assertEquals(roomType.getId(), result.get().getId());
        assertEquals("Deluxe", result.get().getName());
    }

    @Test
    void sumTotalUnitsByHotelId_sumsOnlyActiveRoomTypes() {
        Hotel hotel = persistHotel("Hotel A");
        persistRoomType(hotel, "Standard", RoomTypeStatus.ACTIVE, 20);
        persistRoomType(hotel, "Deluxe", RoomTypeStatus.ACTIVE, 10);
        persistRoomType(hotel, "Suite", RoomTypeStatus.INACTIVE, 5);

        Integer total = repository.sumTotalUnitsByHotelId(hotel.getId());

        assertEquals(30, total);
    }

    @Test
    void sumTotalUnitsByHotelId_returnsZeroWhenNoActiveRoomTypes() {
        Hotel hotel = persistHotel("Hotel A");
        persistRoomType(hotel, "Suite", RoomTypeStatus.INACTIVE, 5);

        Integer total = repository.sumTotalUnitsByHotelId(hotel.getId());

        assertEquals(0, total);
    }

    private Hotel persistHotel(String name) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setCity("Nablus");
        hotel.setCountry("Palestine");
        hotel.setAddress("Street 1");
        hotel.setDescription("Test hotel");
        hotel.setStatus(Hotel.Status.ACTIVE);
        return entityManager.persistAndFlush(hotel);
    }

    private RoomType persistRoomType(Hotel hotel, String name, RoomTypeStatus status, int totalUnits) {
        RoomType roomType = new RoomType();
        roomType.setHotel(hotel);
        roomType.setName(name);
        roomType.setCapacity(4);
        roomType.setBedType(BedType.KING);
        roomType.setBedCount(1);
        roomType.setMaxAdults(2);
        roomType.setMaxChildren(2);
        roomType.setBasePrice(new BigDecimal("100.00"));
        roomType.setTotalUnits(totalUnits);
        roomType.setDescription("Room description");
        roomType.setPolicies("Room policies");
        roomType.setStatus(status);
        return entityManager.persistAndFlush(roomType);
    }
}
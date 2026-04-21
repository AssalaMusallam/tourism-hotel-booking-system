package com.swer313.projectstep1.availabilitypricing.availability;

import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class AvailabilityCheckerAdapterTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    private AvailabilityCheckerAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AvailabilityCheckerAdapter(availabilityRepository, roomTypeRepository);
    }

    @Test
    void isAvailable_returnsTrue_whenRemainingUnitsGreaterThanZero() {
        // Arrange
        RoomType rt = new RoomType();
        rt.setId(1L);
        rt.setTotalUnits(5);
        when(roomTypeRepository.findByIdWithLock(1L)).thenReturn(Optional.of(rt));
        when(availabilityRepository.countByRoomType_IdAndStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                eq(1L), any(List.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(3L);

        // Act
        boolean available = adapter.isAvailable(1L, LocalDate.now(), LocalDate.now().plusDays(2));

        // Assert
        assertTrue(available);
        verify(roomTypeRepository).findByIdWithLock(1L);
    }

    @Test
    void isAvailable_returnsFalse_whenRemainingUnitsIsZero() {
        RoomType rt = new RoomType();
        rt.setId(2L);
        rt.setTotalUnits(2);
        when(roomTypeRepository.findByIdWithLock(2L)).thenReturn(Optional.of(rt));
        when(availabilityRepository.countByRoomType_IdAndStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                eq(2L), any(List.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(2L);

        assertFalse(adapter.isAvailable(2L, LocalDate.now(), LocalDate.now().plusDays(1)));
    }

    @Test
    void remainingUnits_calculation_andNeverNegative_andRoomTypeMissing() {
        RoomType rt = new RoomType();
        rt.setId(3L);
        rt.setTotalUnits(4);
        when(roomTypeRepository.findByIdWithLock(3L)).thenReturn(Optional.of(rt));
        when(availabilityRepository.countByRoomType_IdAndStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                eq(3L), any(List.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(6L);

        long remaining = adapter.remainingUnits(3L, LocalDate.now(), LocalDate.now().plusDays(3));
        assertEquals(0L, remaining);

        // missing room type -> returns 0
        when(roomTypeRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());
        assertEquals(0L, adapter.remainingUnits(99L, LocalDate.now(), LocalDate.now().plusDays(1)));
    }
}


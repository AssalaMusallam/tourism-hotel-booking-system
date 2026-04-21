package com.swer313.projectstep1.availabilitypricing.availability;

import com.swer313.projectstep1.availabilitypricing.pricing.PriceBreakdownDTO;
import com.swer313.projectstep1.availabilitypricing.pricing.PricingCalculator;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private PricingCalculator pricingCalculator;

    private AvailabilityServiceImpl service;

    @Captor
    private ArgumentCaptor<List<Long>> listCaptor;

    @BeforeEach
    void setUp() {
        service = new AvailabilityServiceImpl(availabilityRepository, roomTypeRepository, pricingCalculator);
    }

    @Test
    void checkAvailability_success_returnsAvailabilityResponse_andUsesPricingCalculator() {
        // Arrange
        RoomType rt = new RoomType();
        rt.setId(1L);
        Hotel h = new Hotel(); h.setId(2L); h.setName("H");
        rt.setHotel(h);
        rt.setName("Std");
        rt.setTotalUnits(5);
        rt.setCapacity(3);
        rt.setBasePrice(new BigDecimal("100.00"));
        rt.setStatus(RoomTypeStatus.ACTIVE);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(rt));
        when(availabilityRepository.countByRoomType_IdAndStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                anyLong(), anyList(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(2L);

        PriceBreakdownDTO pb = new PriceBreakdownDTO(
                rt.getBasePrice(), 2, List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        when(pricingCalculator.calculateBreakdown(eq(rt.getBasePrice()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(pb);

        LocalDate in = LocalDate.of(2026,4,10);
        LocalDate out = LocalDate.of(2026,4,12);

        // Act
        AvailabilityResponseDto resp = service.checkAvailability(1L, in, out, 2);

        // Assert
        assertNotNull(resp);
        assertEquals(2L, resp.getBookedUnits());
        assertEquals(3L, resp.getRemainingUnits());
        assertTrue(resp.isAvailable());
        assertEquals(2, resp.getNights());
        assertEquals(pb, resp.getPriceBreakdown());

        verify(pricingCalculator).calculateBreakdown(eq(rt.getBasePrice()), eq(in), eq(out));
    }

    @Test
    void checkAvailability_roomTypeNotFound_throwsRoomTypeNotFoundException() {
        when(roomTypeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.checkAvailability(99L, LocalDate.now(), LocalDate.now().plusDays(1), 1));
    }

    @Test
    void checkAvailability_inactiveRoomType_throwsAvailabilityRoomTypeInactiveException() {
        RoomType rt = new RoomType(); rt.setId(3L); rt.setStatus(RoomTypeStatus.INACTIVE);
        when(roomTypeRepository.findById(3L)).thenReturn(Optional.of(rt));
        assertThrows(RuntimeException.class, () -> service.checkAvailability(3L, LocalDate.now(), LocalDate.now().plusDays(1), 1));
    }

    @Test
    void checkAvailability_guestsExceedCapacity_throwsAvailabilityGuestsExceedCapacityException() {
        RoomType rt = new RoomType(); rt.setId(4L); rt.setCapacity(2); rt.setStatus(RoomTypeStatus.ACTIVE);
        when(roomTypeRepository.findById(4L)).thenReturn(Optional.of(rt));
        assertThrows(RuntimeException.class, () -> service.checkAvailability(4L, LocalDate.now(), LocalDate.now().plusDays(1), 5));
    }

    @Test
    void checkAvailability_nullCheckInOrCheckOut_throwsAvailabilityDateRangeException() {
        assertThrows(RuntimeException.class, () -> service.checkAvailability(1L, null, LocalDate.now().plusDays(1), 1));
        assertThrows(RuntimeException.class, () -> service.checkAvailability(1L, LocalDate.now(), null, 1));
    }

    @Test
    void checkAvailability_checkOutNotAfterCheckIn_throwsAvailabilityDateRangeException() {
        assertThrows(RuntimeException.class, () -> service.checkAvailability(1L, LocalDate.now(), LocalDate.now(), 1));
    }

    @Test
    void checkHotelAvailability_emptyPage_returnsEmptyPagedResponse() {
        Page<RoomType> empty = new PageImpl<>(List.of(), PageRequest.of(0,10), 0);
        when(roomTypeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(empty);

        var resp = service.checkHotelAvailability(1L, LocalDate.now(), LocalDate.now().plusDays(1), null, null, null, PageRequest.of(0,10));
        assertNotNull(resp);
        assertTrue(resp.getContent().isEmpty());
    }

    @Test
    void checkHotelAvailability_mapsBookedCounts_andCreatesSummaries() {
        RoomType rt1 = new RoomType(); rt1.setId(10L); rt1.setTotalUnits(5); rt1.setCapacity(2); rt1.setName("R1");
        Hotel h = new Hotel(); h.setId(99L); h.setName("H"); rt1.setHotel(h);
        Page<RoomType> page = new PageImpl<>(List.of(rt1), PageRequest.of(0,10), 1);

        when(roomTypeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        BookedCountProjection p = new BookedCountProjection() {
            public Long getRoomTypeId() { return 10L; }
            public Long getBookedCount() { return 2L; }
        };

        when(availabilityRepository.countBookedByRoomTypeIds(anyList(), anyList(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(p));

        var resp = service.checkHotelAvailability(99L, LocalDate.now(), LocalDate.now().plusDays(1), null, null, null, PageRequest.of(0,10));
        assertEquals(1, resp.getContent().size());
        AvailabilitySummaryDto s = resp.getContent().get(0);
        assertEquals(10L, s.getRoomTypeId());
        assertEquals(2L, s.getBookedUnits());
        assertEquals(3L, s.getRemainingUnits());
    }
}



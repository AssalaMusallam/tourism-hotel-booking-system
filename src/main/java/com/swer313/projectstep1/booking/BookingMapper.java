package com.swer313.projectstep1.booking;

import com.swer313.projectstep1.catalog.room.RoomType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BookingMapper {

    public Booking toEntity(BookingRequestDTO dto,
                            RoomType roomType,
                            BigDecimal pricePerNight,
                            BigDecimal totalPrice) {
        if (dto == null) return null;

        int totalGuests = dto.getAdults() + dto.getChildren();

        Booking booking = new Booking();
        booking.setRoomType(roomType);
        booking.setGuestName(dto.getGuestName());
        booking.setGuestEmail(dto.getGuestEmail());
        booking.setGuestPhone(dto.getGuestPhone());
        booking.setAdults(dto.getAdults());
        booking.setChildren(dto.getChildren());
        booking.setTotalGuests(totalGuests);
        booking.setCheckIn(dto.getCheckIn());
        booking.setCheckOut(dto.getCheckOut());
        booking.setPricePerNight(pricePerNight);
        booking.setTotalPrice(totalPrice);          // ← من الـ service مباشرة
        booking.setGuestNotes(dto.getGuestNotes());
        booking.setStatus(BookingStatus.PENDING);
        return booking;
    }

    public BookingResponseDTO toDto(Booking booking, long remainingUnits) {
        if (booking == null) return null;

        return new BookingResponseDTO(
                booking.getId(),
                booking.getRoomType().getId(),
                booking.getRoomType().getName(),
                booking.getRoomType().getHotel().getName(),
                booking.getGuestName(),
                booking.getGuestEmail(),
                booking.getGuestPhone(),
                booking.getAdults(),
                booking.getChildren(),
                booking.getTotalGuests(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getNights(),
                booking.getPricePerNight(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getCancelledAt(),
                booking.getCancellationReason(),
                booking.getRefundAmount(),
                booking.getGuestNotes(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                remainingUnits
        );
    }
}
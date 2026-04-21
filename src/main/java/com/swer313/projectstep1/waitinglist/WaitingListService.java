package com.swer313.projectstep1.waitinglist;

import com.swer313.projectstep1.catalog.room.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface WaitingListService {
    // Admin — عدد المنتظرين
    int countWaiting(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);


    // Guest يسجل نفسه
    WaitingListResponseDTO joinWaitingList(WaitingListRequestDTO dto);

    // Guest يلغي تسجيله
    void cancelEntry(Long entryId, String guestEmail);

    // Guest يشوف تسجيلاته
    PagedResponse<WaitingListResponseDTO> getMyEntries(
            String guestEmail, Pageable pageable
    );

    // Admin — قائمة محدودة
    PagedResponse<WaitingListAdminDTO> getWaitingListForAdmin(
            Long roomTypeId, Pageable pageable
    );

    // يستدعيه BookingServiceImpl بعد cancelBooking
    void notifyNextInQueue(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);
}
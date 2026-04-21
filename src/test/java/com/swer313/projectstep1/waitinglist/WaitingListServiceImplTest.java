
package com.swer313.projectstep1.waitinglist;

import com.swer313.projectstep1.booking.AvailabilityChecker;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import com.swer313.projectstep1.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitingListServiceImplTest {

	@Mock
	private WaitingListRepository waitingListRepository;

	@Mock
	private RoomTypeRepository roomTypeRepository;

	@Mock
	private AvailabilityChecker availabilityChecker;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private WaitingListServiceImpl service;

	private RoomType makeActiveRoomType(Long id) {
		RoomType rt = new RoomType();
		rt.setId(id);
		rt.setName("RT");
		rt.setStatus(RoomTypeStatus.ACTIVE);

		com.swer313.projectstep1.catalog.hotel.Hotel hotel =
				new com.swer313.projectstep1.catalog.hotel.Hotel();
		hotel.setId(99L);
		hotel.setName("H");

		rt.setHotel(hotel);
		return rt;
	}

	@Test
	void joinWaitingList_success_returnsDto() {
		WaitingListRequestDTO req = new WaitingListRequestDTO();
		req.setRoomTypeId(1L);
		req.setGuestEmail("g@example.com");
		req.setGuestName("G");
		req.setCheckIn(LocalDate.now().plusDays(5));
		req.setCheckOut(LocalDate.now().plusDays(7));

		RoomType rt = makeActiveRoomType(1L);

		when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(rt));
		when(availabilityChecker.isAvailable(anyLong(), any(), any())).thenReturn(false);
		when(waitingListRepository.existsActiveEntry(anyLong(), anyString(), any(), any())).thenReturn(false);

		WaitingListEntry saved = new WaitingListEntry();
		ReflectionTestUtils.setField(saved, "id", 123L);
		saved.setRoomTypeId(1L);
		saved.setHotelId(99L);
		saved.setRoomTypeName("RT");
		saved.setHotelName("H");
		saved.setGuestEmail("g@example.com");
		saved.setGuestName("G");
		saved.setCheckIn(req.getCheckIn());
		saved.setCheckOut(req.getCheckOut());
		saved.setStatus(WaitingListStatus.WAITING);

		when(waitingListRepository.save(any(WaitingListEntry.class))).thenReturn(saved);
		when(waitingListRepository.findWaitingByRoomTypeAndPeriod(eq(1L), any(), any()))
				.thenReturn(List.of(saved));

		WaitingListResponseDTO dto = service.joinWaitingList(req);

		assertNotNull(dto);
		assertEquals(123L, dto.getId());
		assertEquals(1L, dto.getRoomTypeId());
		assertEquals("g@example.com", dto.getGuestEmail());
		assertEquals(WaitingListStatus.WAITING, dto.getStatus());

		verify(waitingListRepository).save(any(WaitingListEntry.class));
	}

	@Test
	void joinWaitingList_invalidDates_throws() {
		WaitingListRequestDTO req = new WaitingListRequestDTO();
		req.setRoomTypeId(1L);
		req.setGuestEmail("g@example.com");
		req.setGuestName("G");
		req.setCheckIn(LocalDate.now().plusDays(3));
		req.setCheckOut(LocalDate.now().plusDays(2));

		assertThrows(
				com.swer313.projectstep1.errors.BadRequestException.class,
				() -> service.joinWaitingList(req)
		);
	}

	@Test
	void joinWaitingList_roomTypeNotFound_throws() {
		WaitingListRequestDTO req = new WaitingListRequestDTO();
		req.setRoomTypeId(55L);
		req.setGuestEmail("g@example.com");
		req.setGuestName("G");
		req.setCheckIn(LocalDate.now().plusDays(3));
		req.setCheckOut(LocalDate.now().plusDays(5));

		when(roomTypeRepository.findById(55L)).thenReturn(Optional.empty());

		assertThrows(
				com.swer313.projectstep1.catalog.room.RoomTypeNotFoundException.class,
				() -> service.joinWaitingList(req)
		);
	}

	@Test
	void joinWaitingList_roomTypeNotActive_throws() {
		WaitingListRequestDTO req = new WaitingListRequestDTO();
		req.setRoomTypeId(2L);
		req.setGuestEmail("g@example.com");
		req.setGuestName("G");
		req.setCheckIn(LocalDate.now().plusDays(3));
		req.setCheckOut(LocalDate.now().plusDays(5));

		RoomType rt = makeActiveRoomType(2L);
		rt.setStatus(RoomTypeStatus.INACTIVE);

		when(roomTypeRepository.findById(2L)).thenReturn(Optional.of(rt));

		assertThrows(
				com.swer313.projectstep1.errors.BadRequestException.class,
				() -> service.joinWaitingList(req)
		);
	}

	@Test
	void joinWaitingList_roomAvailable_throws() {
		WaitingListRequestDTO req = new WaitingListRequestDTO();
		req.setRoomTypeId(3L);
		req.setGuestEmail("g@example.com");
		req.setGuestName("G");
		req.setCheckIn(LocalDate.now().plusDays(3));
		req.setCheckOut(LocalDate.now().plusDays(5));

		RoomType rt = makeActiveRoomType(3L);

		when(roomTypeRepository.findById(3L)).thenReturn(Optional.of(rt));
		when(availabilityChecker.isAvailable(3L, req.getCheckIn(), req.getCheckOut())).thenReturn(true);

		assertThrows(RoomTypeNotFullException.class, () -> service.joinWaitingList(req));
	}

	@Test
	void cancelEntry_success_changesStatus() {
		WaitingListEntry entry = new WaitingListEntry();
		ReflectionTestUtils.setField(entry, "id", 77L);
		entry.setGuestEmail("g@x.com");
		entry.setStatus(WaitingListStatus.WAITING);

		when(waitingListRepository.findByIdAndGuestEmailIgnoreCase(77L, "g@x.com"))
				.thenReturn(Optional.of(entry));

		service.cancelEntry(77L, "g@x.com");

		assertEquals(WaitingListStatus.CANCELLED, entry.getStatus());
		verify(waitingListRepository).save(entry);
	}

	@Test
	void cancelEntry_notFound_throws() {
		when(waitingListRepository.findByIdAndGuestEmailIgnoreCase(88L, "a@b.com"))
				.thenReturn(Optional.empty());

		assertThrows(
				WaitingListEntryNotFoundException.class,
				() -> service.cancelEntry(88L, "a@b.com")
		);
	}

	@Test
	void notifyNextInQueue_notifiesAndSaves() {
		WaitingListEntry next = new WaitingListEntry();
		ReflectionTestUtils.setField(next, "id", 200L);
		next.setGuestEmail("n@e.com");
		next.setGuestName("N");
		next.setRoomTypeId(5L);
		next.setRoomTypeName("RT");
		next.setHotelName("H");
		next.setCheckIn(LocalDate.now().plusDays(2));
		next.setCheckOut(LocalDate.now().plusDays(3));
		next.setStatus(WaitingListStatus.WAITING);

		when(waitingListRepository.findWaitingByRoomTypeAndPeriod(eq(5L), any(), any()))
				.thenReturn(List.of(next));
		when(waitingListRepository.save(any(WaitingListEntry.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.notifyNextInQueue(5L, next.getCheckIn(), next.getCheckOut());

		assertEquals(WaitingListStatus.NOTIFIED, next.getStatus());
		assertNotNull(next.getNotifiedAt());
		verify(waitingListRepository).save(next);
		verify(notificationService).send(any());
	}

	@Test
	void processExpiredNotifications_expiresAndNotifiesNext() {
		WaitingListEntry expired = new WaitingListEntry();
		ReflectionTestUtils.setField(expired, "id", 300L);
		expired.setRoomTypeId(7L);
		expired.setCheckIn(LocalDate.now().minusDays(2));
		expired.setCheckOut(LocalDate.now().minusDays(1));
		expired.setStatus(WaitingListStatus.NOTIFIED);

		when(waitingListRepository.findExpiredNotifications(any())).thenReturn(List.of(expired));
		when(waitingListRepository.findWaitingByRoomTypeAndPeriod(eq(7L), any(), any()))
				.thenReturn(List.of());

		service.processExpiredNotifications();

		assertEquals(WaitingListStatus.EXPIRED, expired.getStatus());
		verify(waitingListRepository).save(expired);
	}

	@Test
	void expirePastDateEntries_expiresPast() {
		WaitingListEntry past = new WaitingListEntry();
		ReflectionTestUtils.setField(past, "id", 400L);
		past.setStatus(WaitingListStatus.WAITING);

		when(waitingListRepository.findDateExpiredEntries(any())).thenReturn(List.of(past));

		service.expirePastDateEntries();

		assertEquals(WaitingListStatus.EXPIRED, past.getStatus());
		verify(waitingListRepository).save(past);
	}
}
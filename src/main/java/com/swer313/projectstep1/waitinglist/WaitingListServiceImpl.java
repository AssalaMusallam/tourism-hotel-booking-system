package com.swer313.projectstep1.waitinglist;

import com.swer313.projectstep1.booking.AvailabilityChecker;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeNotFoundException;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import com.swer313.projectstep1.catalog.room.PagedResponse;
import com.swer313.projectstep1.errors.BadRequestException;
import com.swer313.projectstep1.notification.NotificationDTOs;
import com.swer313.projectstep1.notification.NotificationService;
import com.swer313.projectstep1.notification.NotificationType;
import com.swer313.projectstep1.notification.ReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WaitingListServiceImpl implements WaitingListService {

    private static final Logger log =
            LoggerFactory.getLogger(WaitingListServiceImpl.class);

    private static final int NOTIFICATION_EXPIRY_HOURS = 24;

    private final WaitingListRepository waitingListRepository;
    private final RoomTypeRepository    roomTypeRepository;
    private final AvailabilityChecker   availabilityChecker;
    private final NotificationService   notificationService;

    public WaitingListServiceImpl(WaitingListRepository waitingListRepository,
                                  RoomTypeRepository    roomTypeRepository,
                                  AvailabilityChecker   availabilityChecker,
                                  NotificationService   notificationService) {
        this.waitingListRepository = waitingListRepository;
        this.roomTypeRepository    = roomTypeRepository;
        this.availabilityChecker   = availabilityChecker;
        this.notificationService   = notificationService;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // JOIN
    // ══════════════════════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public int countWaiting(Long roomTypeId,
                            LocalDate checkIn,
                            LocalDate checkOut) {
        return (int) waitingListRepository
                .countWaitingByRoomTypeAndPeriod(roomTypeId, checkIn, checkOut);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WaitingListAdminDTO> getWaitingListForAdmin(
            Long roomTypeId, Pageable pageable) {

        // TODO: بعد إضافة Auth — تحقق إن الـ admin مسؤول عن الفندق هاد
        Page<WaitingListEntry> page = waitingListRepository
                .findByRoomTypeIdAndStatusOrderByCreatedAtAsc(
                        roomTypeId, WaitingListStatus.WAITING, pageable
                );

        List<WaitingListAdminDTO> content = page.getContent().stream()
                .map(e -> new WaitingListAdminDTO(
                        e.getId(),
                        e.getGuestEmail(),
                        e.getGuestName(),
                        e.getCheckIn(),
                        e.getCheckOut(),
                        e.getStatus(),
                        e.getCreatedAt(),
                        e.getNotifiedAt()
                ))
                .toList();

        return PagedResponse.from(page, content);
    }

    @Override
    public WaitingListResponseDTO joinWaitingList(WaitingListRequestDTO dto) {

        // 1. تحقق التواريخ
        if (!dto.getCheckOut().isAfter(dto.getCheckIn())) {
            throw new BadRequestException("checkOut must be after checkIn.");
        }

        // 2. تحقق الغرفة موجودة وفاعلة
        RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeId())
                .orElseThrow(() -> new RoomTypeNotFoundException(dto.getRoomTypeId()));

        if (roomType.getStatus() != RoomTypeStatus.ACTIVE) {
            throw new BadRequestException(
                    "Room type '" + roomType.getName() + "' is not active."
            );
        }

        // 3. تحقق إن الغرفة ممتلئة فعلاً — ما نسجّل في waiting list لغرفة متاحة
        if (availabilityChecker.isAvailable(
                dto.getRoomTypeId(), dto.getCheckIn(), dto.getCheckOut())) {
            throw new RoomTypeNotFullException(dto.getRoomTypeId());
        }

        // 4. منع التسجيل المكرر
        if (waitingListRepository.existsActiveEntry(
                dto.getRoomTypeId(),
                dto.getGuestEmail(),
                dto.getCheckIn(),
                dto.getCheckOut())) {
            throw new WaitingListAlreadyExistsException(
                    dto.getGuestEmail(), dto.getRoomTypeId()
            );
        }

        // 5. بناء وحفظ الـ entry
        WaitingListEntry entry = new WaitingListEntry();
        entry.setRoomTypeId(dto.getRoomTypeId());
        entry.setHotelId(roomType.getHotel().getId());
        entry.setGuestEmail(dto.getGuestEmail().toLowerCase().trim());
        entry.setGuestName(dto.getGuestName().trim());
        entry.setCheckIn(dto.getCheckIn());
        entry.setCheckOut(dto.getCheckOut());
        entry.setStatus(WaitingListStatus.WAITING);
        entry.setRoomTypeName(roomType.getName());
        entry.setHotelName(roomType.getHotel().getName());

        WaitingListEntry saved = waitingListRepository.save(entry);

        // 6. احسب مكانه في القائمة
        int position = calculatePosition(saved);

        log.info("Guest {} joined waiting list for roomType={}, checkIn={}, position={}",
                dto.getGuestEmail(), dto.getRoomTypeId(), dto.getCheckIn(), position);

        return toDto(saved, position);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CANCEL ENTRY
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void cancelEntry(Long entryId, String guestEmail) {
        WaitingListEntry entry = waitingListRepository
                .findByIdAndGuestEmailIgnoreCase(entryId, guestEmail)
                .orElseThrow(() -> new WaitingListEntryNotFoundException(entryId));

        if (entry.getStatus() == WaitingListStatus.EXPIRED ||
                entry.getStatus() == WaitingListStatus.CANCELLED) {
            throw new BadRequestException(
                    "Entry is already " + entry.getStatus() + " and cannot be cancelled."
            );
        }

        entry.setStatus(WaitingListStatus.CANCELLED);
        waitingListRepository.save(entry);

        log.info("Waiting list entry {} cancelled by guest {}", entryId, guestEmail);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GET MY ENTRIES
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WaitingListResponseDTO> getMyEntries(
            String guestEmail, Pageable pageable) {

        Page<WaitingListEntry> page = waitingListRepository
                .findByGuestEmailIgnoreCaseOrderByCreatedAtDesc(
                        guestEmail.trim(), pageable
                );

        List<WaitingListResponseDTO> content = page.getContent().stream()
                .map(e -> toDto(e, calculatePosition(e)))
                .toList();

        return PagedResponse.from(page, content);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NOTIFY NEXT — يستدعيه BookingServiceImpl بعد cancelBooking
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void notifyNextInQueue(Long roomTypeId,
                                  LocalDate checkIn,
                                  LocalDate checkOut) {
        List<WaitingListEntry> waiting = waitingListRepository
                .findWaitingByRoomTypeAndPeriod(roomTypeId, checkIn, checkOut);

        if (waiting.isEmpty()) {
            log.info("No waiting entries for roomType={} [{} → {}]",
                    roomTypeId, checkIn, checkOut);
            return;
        }

        // الأول في القائمة (FIFO)
        WaitingListEntry next = waiting.get(0);
        next.setStatus(WaitingListStatus.NOTIFIED);
        next.setNotifiedAt(LocalDateTime.now());
        waitingListRepository.save(next);

        // ابعت notification
        sendAvailabilityNotificationSafely(next);

        log.info("Notified guest {} for roomType={} [{} → {}]",
                next.getGuestEmail(), roomTypeId, checkIn, checkOut);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCHEDULER 1 — كل ساعة: فحص الـ NOTIFIED اللي فات عليها 24 ساعة
    // ══════════════════════════════════════════════════════════════════════════

    @Scheduled(fixedDelay = 3_600_000) // كل ساعة
    @Transactional
    public void processExpiredNotifications() {
        LocalDateTime cutoff = LocalDateTime.now()
                .minusHours(NOTIFICATION_EXPIRY_HOURS);

        List<WaitingListEntry> expired =
                waitingListRepository.findExpiredNotifications(cutoff);

        if (expired.isEmpty()) return;

        log.info("Processing {} expired notifications", expired.size());

        for (WaitingListEntry entry : expired) {
            // 1. غيّر status → EXPIRED
            entry.setStatus(WaitingListStatus.EXPIRED);
            waitingListRepository.save(entry);

            // 2. ابحث عن التالي في القائمة
            notifyNextInQueue(
                    entry.getRoomTypeId(),
                    entry.getCheckIn(),
                    entry.getCheckOut()
            );
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCHEDULER 2 — كل يوم منتصف الليل: expire الـ entries اللي التاريخ عدى
    // ══════════════════════════════════════════════════════════════════════════

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expirePastDateEntries() {
        List<WaitingListEntry> pastEntries =
                waitingListRepository.findDateExpiredEntries(LocalDate.now());

        if (pastEntries.isEmpty()) return;

        log.info("Expiring {} past-date waiting list entries", pastEntries.size());

        pastEntries.forEach(e -> {
            e.setStatus(WaitingListStatus.EXPIRED);
            waitingListRepository.save(e);
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void sendAvailabilityNotificationSafely(WaitingListEntry entry) {
        try {
            notificationService.send(
                    NotificationDTOs.SendRequest.builder()
                            .recipientEmail(entry.getGuestEmail())
                            .recipientName(entry.getGuestName())
                            .type(NotificationType.ROOM_AVAILABLE)
                            .referenceId(entry.getId())
                            .referenceType(ReferenceType.BOOKING)
                            .hotelName(entry.getHotelName())
                            .roomType(entry.getRoomTypeName())
                            .checkInDate(entry.getCheckIn().toString())
                            .checkOutDate(entry.getCheckOut().toString())
                            .build()
            );
        } catch (Exception e) {
            log.warn("Failed to send availability notification for entry {}: {}",
                    entry.getId(), e.getMessage());
        }
    }

    private int calculatePosition(WaitingListEntry entry) {
        if (entry.getStatus() != WaitingListStatus.WAITING) return 0;

        List<WaitingListEntry> queue = waitingListRepository
                .findWaitingByRoomTypeAndPeriod(
                        entry.getRoomTypeId(),
                        entry.getCheckIn(),
                        entry.getCheckOut()
                );

        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId().equals(entry.getId())) {
                return i + 1; // 1-based
            }
        }
        return 0;
    }

    private WaitingListResponseDTO toDto(WaitingListEntry entry, int position) {
        return new WaitingListResponseDTO(
                entry.getId(),
                entry.getRoomTypeId(),
                entry.getRoomTypeName(),
                entry.getHotelName(),
                entry.getGuestEmail(),
                entry.getGuestName(),
                entry.getCheckIn(),
                entry.getCheckOut(),
                entry.getStatus(),
                entry.getCreatedAt(),
                entry.getNotifiedAt(),
                entry.getStatus() == WaitingListStatus.WAITING ? position : null
        );
    }
}
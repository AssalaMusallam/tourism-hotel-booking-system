package com.swer313.projectstep1.waitinglist;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "waiting_list",
        indexes = {
                @Index(name = "idx_wl_room_type_id",  columnList = "room_type_id"),
                @Index(name = "idx_wl_guest_email",   columnList = "guest_email"),
                @Index(name = "idx_wl_status",        columnList = "status"),
                @Index(name = "idx_wl_check_in",      columnList = "check_in")
        }
)
public class WaitingListEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    // نحتفظ بـ hotelId عشان نعرض اسم الفندق في الـ notification
    @NotNull
    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @NotBlank
    @Email
    @Column(name = "guest_email", nullable = false, length = 255)
    private String guestEmail;

    @NotBlank
    @Column(name = "guest_name", nullable = false, length = 150)
    private String guestName;

    @NotNull
    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @NotNull
    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WaitingListStatus status = WaitingListStatus.WAITING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // وقت ما بعتنا الإشعار — بنحسب منه الـ 24 ساعة
    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    // اسم الغرفة والفندق للـ notification — نحفظهم عشان ما نحتاج join
    @Column(name = "room_type_name", length = 200)
    private String roomTypeName;

    @Column(name = "hotel_name", length = 200)
    private String hotelName;

    public WaitingListEntry() {}

    // ── Business helpers ──────────────────────────────────────────────────────

    public boolean isExpiredNotification() {
        if (status != WaitingListStatus.NOTIFIED || notifiedAt == null) return false;
        return LocalDateTime.now().isAfter(notifiedAt.plusHours(24));
    }

    public boolean isDateExpired() {
        return LocalDate.now().isAfter(checkIn);
    }

    // هل الـ entry يتداخل مع فترة معينة؟
    public boolean overlapsWith(LocalDate otherCheckIn, LocalDate otherCheckOut) {
        return !checkOut.isBefore(otherCheckIn) && !checkIn.isAfter(otherCheckOut);
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId()                              { return id; }
    public Long getRoomTypeId()                      { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId)       { this.roomTypeId = roomTypeId; }
    public Long getHotelId()                         { return hotelId; }
    public void setHotelId(Long hotelId)             { this.hotelId = hotelId; }
    public String getGuestEmail()                    { return guestEmail; }
    public void setGuestEmail(String email)          { this.guestEmail = email; }
    public String getGuestName()                     { return guestName; }
    public void setGuestName(String name)            { this.guestName = name; }
    public LocalDate getCheckIn()                    { return checkIn; }
    public void setCheckIn(LocalDate checkIn)        { this.checkIn = checkIn; }
    public LocalDate getCheckOut()                   { return checkOut; }
    public void setCheckOut(LocalDate checkOut)      { this.checkOut = checkOut; }
    public WaitingListStatus getStatus()             { return status; }
    public void setStatus(WaitingListStatus status)  { this.status = status; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public LocalDateTime getNotifiedAt()             { return notifiedAt; }
    public void setNotifiedAt(LocalDateTime t)       { this.notifiedAt = t; }
    public String getRoomTypeName()                  { return roomTypeName; }
    public void setRoomTypeName(String name)         { this.roomTypeName = name; }
    public String getHotelName()                     { return hotelName; }
    public void setHotelName(String name)            { this.hotelName = name; }
}
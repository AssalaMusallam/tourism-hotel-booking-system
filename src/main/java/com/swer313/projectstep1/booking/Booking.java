package com.swer313.projectstep1.booking;

import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.notification.Notification;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(
        name = "bookings",
        indexes = {
                @Index(name = "idx_booking_room_type_id", columnList = "room_type_id"),
                @Index(name = "idx_booking_status",        columnList = "status"),
                @Index(name = "idx_booking_guest_email",   columnList = "guest_email"),
                @Index(name = "idx_booking_check_in",      columnList = "check_in"),
                @Index(name = "idx_booking_check_out",     columnList = "check_out")
        }
)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @NotBlank
    @Size(min = 2, max = 150)
    @Column(name = "guest_name", nullable = false, length = 150)
    private String guestName;

    @NotBlank
    @Email
    @Column(name = "guest_email", nullable = false, length = 255)
    private String guestEmail;

    @NotBlank
    @Column(name = "guest_phone", nullable = false, length = 30)
    private String guestPhone;

    @Min(1)
    @Column(nullable = false)
    private int adults;

    @Min(0)
    @Column(nullable = false)
    private int children;

    @Column(name = "total_guests", nullable = false)
    private int totalGuests;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Size(max = 1000)
    @Column(name = "guest_notes", columnDefinition = "TEXT")
    private String guestNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Booking() {}

    // ── Business helpers ──────────────────────────────────────────────────────

    public long getNights() {
        if (checkIn == null || checkOut == null) return 0;
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public boolean isCancellable() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    public long hoursUntilCheckIn() {
        return ChronoUnit.HOURS.between(LocalDateTime.now(), checkIn.atStartOfDay());
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId()                                              { return id; }
    public RoomType getRoomType()                                    { return roomType; }
    public void setRoomType(RoomType roomType)                       { this.roomType = roomType; }
    public String getGuestName()                                     { return guestName; }
    public void setGuestName(String guestName)                       { this.guestName = guestName; }
    public String getGuestEmail()                                    { return guestEmail; }
    public void setGuestEmail(String guestEmail)                     { this.guestEmail = guestEmail; }
    public String getGuestPhone()                                    { return guestPhone; }
    public void setGuestPhone(String guestPhone)                     { this.guestPhone = guestPhone; }
    public int getAdults()                                           { return adults; }
    public void setAdults(int adults)                                { this.adults = adults; }
    public int getChildren()                                         { return children; }
    public void setChildren(int children)                            { this.children = children; }
    public int getTotalGuests()                                      { return totalGuests; }
    public void setTotalGuests(int totalGuests)                      { this.totalGuests = totalGuests; }
    public LocalDate getCheckIn()                                    { return checkIn; }
    public void setCheckIn(LocalDate checkIn)                        { this.checkIn = checkIn; }
    public LocalDate getCheckOut()                                   { return checkOut; }
    public void setCheckOut(LocalDate checkOut)                      { this.checkOut = checkOut; }
    public BigDecimal getPricePerNight()                             { return pricePerNight; }
    public void setPricePerNight(BigDecimal p)                       { this.pricePerNight = p; }
    public BigDecimal getTotalPrice()                                { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice)                 { this.totalPrice = totalPrice; }
    public BookingStatus getStatus()                                 { return status; }
    public void setStatus(BookingStatus status)                      { this.status = status; }
    public LocalDateTime getCancelledAt()                            { return cancelledAt; }
    public void setCancelledAt(LocalDateTime t)                      { this.cancelledAt = t; }
    public String getCancellationReason()                            { return cancellationReason; }
    public void setCancellationReason(String r)                      { this.cancellationReason = r; }
    public BigDecimal getRefundAmount()                              { return refundAmount; }
    public void setRefundAmount(BigDecimal r)                        { this.refundAmount = r; }
    public String getGuestNotes()                                    { return guestNotes; }
    public void setGuestNotes(String guestNotes)                     { this.guestNotes = guestNotes; }
    public LocalDateTime getCreatedAt()                              { return createdAt; }
    public LocalDateTime getUpdatedAt()                              { return updatedAt; }

    @Override
    public String toString() {
        return "Booking{id=" + id + ", guestEmail='" + guestEmail
                + "', checkIn=" + checkIn + ", checkOut=" + checkOut
                + ", status=" + status + '}';
    }
}
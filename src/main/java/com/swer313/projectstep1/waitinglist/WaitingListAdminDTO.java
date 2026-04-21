package com.swer313.projectstep1.waitinglist;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Admin view — بيكشف بيانات محدودة فقط.
 * الإيميل masked عشان نحمي خصوصية الـ guest.
 * TODO: بعد إضافة Auth، تحقق إن الـ admin مسؤول عن هذا الفندق فقط.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WaitingListAdminDTO {

    private final Long              id;
    private final String            maskedEmail;  // ah***@test.com
    private final String            guestName;
    private final LocalDate         checkIn;
    private final LocalDate         checkOut;
    private final WaitingListStatus status;
    private final LocalDateTime     createdAt;
    private final LocalDateTime     notifiedAt;

    public WaitingListAdminDTO(Long id, String guestEmail,
                               String guestName, LocalDate checkIn,
                               LocalDate checkOut, WaitingListStatus status,
                               LocalDateTime createdAt, LocalDateTime notifiedAt) {
        this.id          = id;
        this.maskedEmail = maskEmail(guestEmail);
        this.guestName   = guestName;
        this.checkIn     = checkIn;
        this.checkOut    = checkOut;
        this.status      = status;
        this.createdAt   = createdAt;
        this.notifiedAt  = notifiedAt;
    }

    // ah***@test.com
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) return "**@" + domain;
        return local.substring(0, 2) + "***@" + domain;
    }

    public Long              getId()          { return id; }
    public String            getMaskedEmail() { return maskedEmail; }
    public String            getGuestName()   { return guestName; }
    public LocalDate         getCheckIn()     { return checkIn; }
    public LocalDate         getCheckOut()    { return checkOut; }
    public WaitingListStatus getStatus()      { return status; }
    public LocalDateTime     getCreatedAt()   { return createdAt; }
    public LocalDateTime     getNotifiedAt()  { return notifiedAt; }
}
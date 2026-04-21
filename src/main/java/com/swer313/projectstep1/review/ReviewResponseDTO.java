package com.swer313.projectstep1.review;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponseDTO {

    private final Long          id;
    private final Long          bookingId;
    private final Long          hotelId;
    private final String        hotelName;
    private final String        guestName;
    private final String        guestEmail;
    private final int           rating;
    private final String        comment;
    private final LocalDateTime createdAt;

    // verifiedStay = true دايماً — يعني الضيف أكمل الإقامة ودفع
    private final boolean       verifiedStay;

    // آخر يوم يقدر فيه يكتب review (checkOut + 30 يوم)
    private final LocalDate     reviewDeadline;

    public ReviewResponseDTO(Long id, Long bookingId, Long hotelId,
                             String hotelName, String guestName,
                             String guestEmail, int rating,
                             String comment, LocalDateTime createdAt,
                             boolean verifiedStay, LocalDate reviewDeadline) {
        this.id             = id;
        this.bookingId      = bookingId;
        this.hotelId        = hotelId;
        this.hotelName      = hotelName;
        this.guestName      = guestName;
        this.guestEmail     = guestEmail;
        this.rating         = rating;
        this.comment        = comment;
        this.createdAt      = createdAt;
        this.verifiedStay   = verifiedStay;
        this.reviewDeadline = reviewDeadline;
    }

    public Long getId()                    { return id; }
    public Long getBookingId()             { return bookingId; }
    public Long getHotelId()               { return hotelId; }
    public String getHotelName()           { return hotelName; }
    public String getGuestName()           { return guestName; }
    public String getGuestEmail()          { return guestEmail; }
    public int getRating()                 { return rating; }
    public String getComment()             { return comment; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public boolean isVerifiedStay()        { return verifiedStay; }
    public LocalDate getReviewDeadline()   { return reviewDeadline; }
}
package com.swer313.projectstep1.review;

import com.swer313.projectstep1.booking.Booking;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_review_hotel_id",    columnList = "hotel_id"),
                @Index(name = "idx_review_guest_email", columnList = "guest_email"),
                @Index(name = "idx_review_booking_id",  columnList = "booking_id"),
                @Index(name = "idx_review_rating",      columnList = "rating")
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique = true يضمن review واحد لكل booking على مستوى الداتابيس
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    // نحتفظ بـ hotelId مباشرة عشان queries الـ aggregate تكون سريعة
    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @NotBlank
    @Column(name = "guest_email", nullable = false, length = 255)
    private String guestEmail;

    @Min(1) @Max(5)
    @Column(nullable = false)
    private int rating;

    @Size(max = 1000)
    @Column(length = 1000)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Review() {}

    public Long getId()                        { return id; }
    public Booking getBooking()                { return booking; }
    public void setBooking(Booking booking)    { this.booking = booking; }
    public Long getHotelId()                   { return hotelId; }
    public void setHotelId(Long hotelId)       { this.hotelId = hotelId; }
    public String getGuestEmail()              { return guestEmail; }
    public void setGuestEmail(String email)    { this.guestEmail = email; }
    public int getRating()                     { return rating; }
    public void setRating(int rating)          { this.rating = rating; }
    public String getComment()                 { return comment; }
    public void setComment(String comment)     { this.comment = comment; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
}
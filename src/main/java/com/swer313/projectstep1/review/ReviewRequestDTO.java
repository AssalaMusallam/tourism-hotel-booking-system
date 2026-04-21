package com.swer313.projectstep1.review;

import jakarta.validation.constraints.*;

public class ReviewRequestDTO {

    @NotNull(message = "bookingId is required")
    private Long bookingId;

    @NotBlank(message = "guestEmail is required")
    @Email(message = "guestEmail must be a valid email")
    private String guestEmail;

    @Min(value = 1, message = "rating must be at least 1")
    @Max(value = 5, message = "rating must be at most 5")
    private int rating;

    @Size(max = 1000, message = "comment cannot exceed 1000 characters")
    private String comment;

    public Long getBookingId()              { return bookingId; }
    public void setBookingId(Long id)       { this.bookingId = id; }
    public String getGuestEmail()           { return guestEmail; }
    public void setGuestEmail(String email) { this.guestEmail = email; }
    public int getRating()                  { return rating; }
    public void setRating(int rating)       { this.rating = rating; }
    public String getComment()              { return comment; }
    public void setComment(String comment)  { this.comment = comment; }
}
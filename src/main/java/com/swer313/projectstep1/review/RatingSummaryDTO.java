package com.swer313.projectstep1.review;

public class RatingSummaryDTO {

    private final Long   hotelId;
    private final String hotelName;
    private final double averageRating;
    private final long   totalReviews;
    private final long   fiveStars;
    private final long   fourStars;
    private final long   threeStars;
    private final long   twoStars;
    private final long   oneStar;
    private final double fiveStarPercent;
    private final double fourStarPercent;
    private final double threeStarPercent;
    private final double twoStarPercent;
    private final double oneStarPercent;

    public RatingSummaryDTO(Long hotelId, String hotelName,
                            double averageRating, long totalReviews,
                            long fiveStars, long fourStars, long threeStars,
                            long twoStars, long oneStar,
                            double fiveStarPercent, double fourStarPercent,
                            double threeStarPercent, double twoStarPercent,
                            double oneStarPercent) {
        this.hotelId          = hotelId;
        this.hotelName        = hotelName;
        this.averageRating    = averageRating;
        this.totalReviews     = totalReviews;
        this.fiveStars        = fiveStars;
        this.fourStars        = fourStars;
        this.threeStars       = threeStars;
        this.twoStars         = twoStars;
        this.oneStar          = oneStar;
        this.fiveStarPercent  = fiveStarPercent;
        this.fourStarPercent  = fourStarPercent;
        this.threeStarPercent = threeStarPercent;
        this.twoStarPercent   = twoStarPercent;
        this.oneStarPercent   = oneStarPercent;
    }

    public Long   getHotelId()           { return hotelId; }
    public String getHotelName()         { return hotelName; }
    public double getAverageRating()     { return averageRating; }
    public long   getTotalReviews()      { return totalReviews; }
    public long   getFiveStars()         { return fiveStars; }
    public long   getFourStars()         { return fourStars; }
    public long   getThreeStars()        { return threeStars; }
    public long   getTwoStars()          { return twoStars; }
    public long   getOneStar()           { return oneStar; }
    public double getFiveStarPercent()   { return fiveStarPercent; }
    public double getFourStarPercent()   { return fourStarPercent; }
    public double getThreeStarPercent()  { return threeStarPercent; }
    public double getTwoStarPercent()    { return twoStarPercent; }
    public double getOneStarPercent()    { return oneStarPercent; }
}
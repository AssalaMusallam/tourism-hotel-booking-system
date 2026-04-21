package com.swer313.projectstep1.review;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingNotFoundException;
import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelNotFoundException;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.room.PagedResponse;
import com.swer313.projectstep1.notification.NotificationDTOs;
import com.swer313.projectstep1.notification.NotificationService;
import com.swer313.projectstep1.notification.NotificationType;
import com.swer313.projectstep1.notification.ReferenceType;
import com.swer313.projectstep1.payment.PaymentRepository;
import com.swer313.projectstep1.payment.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private static final int REVIEW_WINDOW_DAYS          = 30;
    private static final int REMINDER_DAYS_AFTER_CHECKOUT = 2;

    private final ReviewRepository    reviewRepository;
    private final BookingRepository   bookingRepository;
    private final HotelRepository     hotelRepository;
    private final PaymentRepository   paymentRepository;
    private final NotificationService notificationService;

    public ReviewServiceImpl(ReviewRepository    reviewRepository,
                             BookingRepository   bookingRepository,
                             HotelRepository     hotelRepository,
                             PaymentRepository   paymentRepository,
                             NotificationService notificationService) {
        this.reviewRepository    = reviewRepository;
        this.bookingRepository   = bookingRepository;
        this.hotelRepository     = hotelRepository;
        this.paymentRepository   = paymentRepository;
        this.notificationService = notificationService;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO dto) {

        // 1. جلب الـ booking
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(dto.getBookingId()));

        // 2. الـ booking لازم يكون COMPLETED
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ReviewNotAllowedException(
                    "Reviews can only be submitted for COMPLETED bookings. " +
                            "Current status: " + booking.getStatus()
            );
        }

        // 3. تحقق إن الـ guestEmail مطابق للـ booking — منع الانتحال
        if (!booking.getGuestEmail().equalsIgnoreCase(dto.getGuestEmail())) {
            throw new ReviewNotAllowedException(
                    "The provided email does not match the booking guest email."
            );
        }

        // 4. تحقق إن الحجز اتدفع فعلاً (PaymentStatus.SUCCESS)
        boolean hasPaid = paymentRepository.existsByBookingIdAndStatusIn(
                booking.getId(),
                List.of(PaymentStatus.SUCCESS)
        );
        if (!hasPaid) {
            throw new ReviewNotAllowedException(
                    "Cannot submit a review for a booking without a successful payment."
            );
        }

        // 5. تحقق إن ما فات وقت الـ review window
        LocalDate deadline = booking.getCheckOut().plusDays(REVIEW_WINDOW_DAYS);
        if (LocalDate.now().isAfter(deadline)) {
            throw new ReviewNotAllowedException(
                    "Review period has expired. Reviews must be submitted within "
                            + REVIEW_WINDOW_DAYS + " days of checkout. Deadline was: " + deadline
            );
        }

        // 6. منع التكرار — فحص قبل الـ save
        //    الـ unique constraint على booking_id في الداتابيس هو الضمان النهائي
        if (reviewRepository.existsByBooking_Id(dto.getBookingId())) {
            throw new ReviewAlreadyExistsException(dto.getBookingId());
        }

        // 7. جلب الـ hotel
        Long hotelId = booking.getRoomType().getHotel().getId();
        Hotel hotel  = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        // 8. بناء وحفظ الـ Review
        Review review = new Review();
        review.setBooking(booking);
        review.setHotelId(hotelId);
        review.setGuestEmail(booking.getGuestEmail()); // من الـ booking — أمان
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        Review saved = reviewRepository.save(review);

        log.info("Review created: id={}, bookingId={}, hotelId={}, rating={}",
                saved.getId(), booking.getId(), hotelId, saved.getRating());

        return toDto(saved, hotel.getName(), booking.getGuestName(), deadline);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // READ
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public PagedResponse<ReviewResponseDTO> getHotelReviews(Long hotelId,
                                                            Pageable pageable) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        Page<Review> page = reviewRepository
                .findByHotelIdOrderByCreatedAtDesc(hotelId, pageable);

        List<ReviewResponseDTO> content = page.getContent().stream()
                .map(r -> {
                    LocalDate deadline = r.getBooking().getCheckOut()
                            .plusDays(REVIEW_WINDOW_DAYS);
                    return toDto(r, hotel.getName(),
                            r.getBooking().getGuestName(), deadline);
                })
                .toList();

        return PagedResponse.from(page, content);
    }

    @Override
    public RatingSummaryDTO getRatingSummary(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        Object[] row = reviewRepository.getRatingSummaryByHotelId(hotelId);

        // الـ JPA بيرجع Object[][] أحياناً — خذ الـ row الأول
        if (row != null && row.length > 0 && row[0] instanceof Object[]) {
            row = (Object[]) row[0];
        }

        if (row == null || row[0] == null) {
            return new RatingSummaryDTO(
                    hotelId, hotel.getName(),
                    0.0, 0L, 0L, 0L, 0L, 0L, 0L,
                    0.0, 0.0, 0.0, 0.0, 0.0
            );
        }

        double avg        = ((Number) row[0]).doubleValue();
        double rounded    = Math.round(avg * 10.0) / 10.0;
        long   total      = ((Number) row[1]).longValue();
        long   fiveStars  = ((Number) row[2]).longValue();
        long   fourStars  = ((Number) row[3]).longValue();
        long   threeStars = ((Number) row[4]).longValue();
        long   twoStars   = ((Number) row[5]).longValue();
        long   oneStar    = ((Number) row[6]).longValue();

        return new RatingSummaryDTO(
                hotelId, hotel.getName(), rounded, total,
                fiveStars, fourStars, threeStars, twoStars, oneStar,
                percent(fiveStars,  total),
                percent(fourStars,  total),
                percent(threeStars, total),
                percent(twoStars,   total),
                percent(oneStar,    total)
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCHEDULER — تذكير بكتابة الـ review بعد يومين من الـ checkout
    // ══════════════════════════════════════════════════════════════════════════

    @Scheduled(cron = "0 0 10 * * *") // كل يوم الساعة 10 صباحاً
    @Transactional(readOnly = true)
    public void sendReviewReminders() {
        LocalDate targetDate = LocalDate.now().minusDays(REMINDER_DAYS_AFTER_CHECKOUT);

        List<Booking> eligible =
                reviewRepository.findCompletedBookingsWithoutReview(targetDate);

        log.info("Review reminder scheduler: {} booking(s) eligible (checkout: {})",
                eligible.size(), targetDate);

        for (Booking b : eligible) {
            try {
                notificationService.send(
                        NotificationDTOs.SendRequest.builder()
                                .recipientEmail(b.getGuestEmail())
                                .recipientName(b.getGuestName())
                                .type(NotificationType.REVIEW_REMINDER)
                                .referenceId(b.getId())
                                .referenceType(ReferenceType.BOOKING)
                                .hotelName(b.getRoomType().getHotel().getName())
                                .bookingReference("BK-" + b.getId())
                                .build()
                );
                log.info("Review reminder sent for booking id={}", b.getId());
            } catch (Exception e) {
                // فشل الـ notification ما يفشّل الـ scheduler
                log.warn("Failed to send review reminder for booking {}: {}",
                        b.getId(), e.getMessage());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════════════════

    private ReviewResponseDTO toDto(Review review, String hotelName,
                                    String guestName, LocalDate reviewDeadline) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getBooking().getId(),
                review.getHotelId(),
                hotelName,
                guestName,
                review.getGuestEmail(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                true,           // verifiedStay — دايماً true لأن الـ booking COMPLETED ومدفوع
                reviewDeadline
        );
    }


    private double percent(long part, long total) {
        if (total == 0) return 0.0;
        return Math.round((part * 100.0 / total) * 10.0) / 10.0;
    }
}
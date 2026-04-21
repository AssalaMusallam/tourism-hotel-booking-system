package com.swer313.projectstep1.availabilitypricing.availability;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.List;
public final class AvailabilitySpecifications {

    private AvailabilitySpecifications() {}

    // ── Atomic specs ─────────────────────────────────────────────────────────

    public static Specification<RoomType> hotelIdEq(Long hotelId) {
        return (root, query, cb) ->
                hotelId == null ? null
                        : cb.equal(root.get("hotel").get("id"), hotelId);
    }

    public static Specification<RoomType> statusEq(RoomTypeStatus status) {
        return (root, query, cb) ->
                status == null ? null
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<RoomType> capacityGte(Integer guests) {
        return (root, query, cb) ->
                guests == null ? null
                        : cb.greaterThanOrEqualTo(root.get("capacity"), guests);
    }

    public static Specification<RoomType> qLike(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;
            String pattern = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")),        pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("policies")),    pattern)
            );
        };
    }

    /**
     * Subquery spec — يفلتر في الـ DB مباشرة.
     * يحل مشكلة availableOnly كان بيصير in-memory بعد الـ pagination.
     *
     * المنطق: totalUnits - count(active bookings in range) > 0
     */
    public static Specification<RoomType> hasAvailableUnits(
            LocalDate checkIn, LocalDate checkOut) {

        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<Booking> b    = sub.from(Booking.class);

            sub.select(cb.count(b))
                    .where(
                            cb.equal(b.get("roomType").get("id"), root.get("id")),
                            b.get("status").in(
                                    List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
                            ),
                            cb.lessThan(b.get("checkIn"),    checkOut),
                            cb.greaterThan(b.get("checkOut"), checkIn)
                    );

            // availableUnits = totalUnits - bookedCount > 0
            return cb.gt(
                    cb.diff(root.<Integer>get("totalUnits").as(Long.class), sub),
                    0L
            );
        };
    }

    // ── Composite ─────────────────────────────────────────────────────────────

    /**
     * الـ spec الموحّد للبحث في فندق — يجمع كل الفلاتر.
     *
     * @param availableOnly إذا true، يفلتر في الـ DB عبر subquery
     */
    public static Specification<RoomType> hotelAvailabilityFilter(
            Long hotelId, Integer guests, String q,
            Boolean availableOnly, LocalDate checkIn, LocalDate checkOut) {

        Specification<RoomType> spec = Specification
                .where(hotelIdEq(hotelId))
                .and(statusEq(RoomTypeStatus.ACTIVE))
                .and(capacityGte(guests))
                .and(qLike(q));

        if (Boolean.TRUE.equals(availableOnly)
                && checkIn  != null
                && checkOut != null) {
            spec = spec.and(hasAvailableUnits(checkIn, checkOut));
        }

        return spec;
    }
}
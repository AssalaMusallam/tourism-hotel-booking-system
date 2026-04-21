package com.swer313.projectstep1.booking;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class BookingSpecification {

    private BookingSpecification() {}

    // ── Overlap detection ─────────────────────────────────────────────────────

    public static Specification<Booking> overlapping(
            Long roomTypeId, LocalDate checkIn,
            LocalDate checkOut, Long excludeBookingId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("roomType").get("id"), roomTypeId));
            predicates.add(root.get("status").in(
                    List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)));
            predicates.add(cb.lessThan(root.get("checkIn"), checkOut));
            predicates.add(cb.greaterThan(root.get("checkOut"), checkIn));
            if (excludeBookingId != null)
                predicates.add(cb.notEqual(root.get("id"), excludeBookingId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ── Individual filters ────────────────────────────────────────────────────

    public static Specification<Booking> byRoomTypeId(Long roomTypeId) {
        return (root, query, cb) -> roomTypeId == null ? null
                : cb.equal(root.get("roomType").get("id"), roomTypeId);
    }

    public static Specification<Booking> byHotelId(Long hotelId) {
        return (root, query, cb) -> {
            if (hotelId == null) return null;
            var roomType = root.join("roomType", JoinType.LEFT);
            return cb.equal(roomType.get("hotel").get("id"), hotelId);
        };
    }

    public static Specification<Booking> byGuestEmail(String email) {
        return (root, query, cb) -> email == null ? null
                : cb.equal(cb.lower(root.get("guestEmail")),
                email.trim().toLowerCase());
    }

    public static Specification<Booking> byStatus(BookingStatus status) {
        return (root, query, cb) -> status == null ? null
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Booking> checkInOnOrAfter(LocalDate from) {
        return (root, query, cb) -> from == null ? null
                : cb.greaterThanOrEqualTo(root.get("checkIn"), from);
    }

    public static Specification<Booking> checkInOnOrBefore(LocalDate to) {
        return (root, query, cb) -> to == null ? null
                : cb.lessThanOrEqualTo(root.get("checkIn"), to);
    }

    public static Specification<Booking> checkOutOnOrAfter(LocalDate from) {
        return (root, query, cb) -> from == null ? null
                : cb.greaterThanOrEqualTo(root.get("checkOut"), from);
    }

    public static Specification<Booking> checkOutOnOrBefore(LocalDate to) {
        return (root, query, cb) -> to == null ? null
                : cb.lessThanOrEqualTo(root.get("checkOut"), to);
    }

    // ── Upcoming ──────────────────────────────────────────────────────────────

    /** CONFIRMED bookings بـ checkIn >= today — لكل الفنادق. */
    public static Specification<Booking> upcoming() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), BookingStatus.CONFIRMED),
                cb.greaterThanOrEqualTo(root.get("checkIn"), LocalDate.now())
        );
    }

    /**
     * FIX: overload مخصص بدل chain في الـ service.
     * CONFIRMED + checkIn >= today + hotel محدد.
     */
    public static Specification<Booking> upcomingByHotel(Long hotelId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), BookingStatus.CONFIRMED));
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get("checkIn"), LocalDate.now()));
            if (hotelId != null) {
                var roomType = root.join("roomType", JoinType.INNER);
                predicates.add(cb.equal(roomType.get("hotel").get("id"), hotelId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ── Combined filter ───────────────────────────────────────────────────────

    public static Specification<Booking> withFilters(
            Long roomTypeId, Long hotelId, String guestEmail,
            BookingStatus status,
            LocalDate checkInFrom,  LocalDate checkInTo,
            LocalDate checkOutFrom, LocalDate checkOutTo) {

        return Specification
                .where(byRoomTypeId(roomTypeId))
                .and(byHotelId(hotelId))
                .and(byGuestEmail(guestEmail))
                .and(byStatus(status))
                .and(checkInOnOrAfter(checkInFrom))
                .and(checkInOnOrBefore(checkInTo))
                .and(checkOutOnOrAfter(checkOutFrom))
                .and(checkOutOnOrBefore(checkOutTo));
    }
}
package com.swer313.projectstep1.catalog.room;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;

public class RoomTypeSpecifications {

    private RoomTypeSpecifications() {}

    // ✅ فلترة عبر العلاقة hotel.id (مش عمود مباشر)
    public static Specification<RoomType> hotelIdEq(Long hotelId) {
        return (root, query, cb) ->
                hotelId == null ? null : cb.equal(root.get("hotel").get("id"), hotelId);
    }

    public static Specification<RoomType> nameEq(String name) {
        return (root, query, cb) ->
                (name == null || name.isBlank()) ? null :
                        cb.equal(cb.lower(root.get("name")), name.trim().toLowerCase());
    }

    public static Specification<RoomType> bedTypeEq(BedType bedType) {
        return (root, query, cb) ->
                bedType == null ? null : cb.equal(root.get("bedType"), bedType);
    }

    public static Specification<RoomType> bedCountBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("bedCount"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("bedCount"), min);
            return cb.lessThanOrEqualTo(root.get("bedCount"), max);
        };
    }

    public static Specification<RoomType> capacityBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("capacity"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("capacity"), min);
            return cb.lessThanOrEqualTo(root.get("capacity"), max);
        };
    }

    public static Specification<RoomType> maxAdultsBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("maxAdults"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("maxAdults"), min);
            return cb.lessThanOrEqualTo(root.get("maxAdults"), max);
        };
    }

    public static Specification<RoomType> maxChildrenBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("maxChildren"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("maxChildren"), min);
            return cb.lessThanOrEqualTo(root.get("maxChildren"), max);
        };
    }

    public static Specification<RoomType> basePriceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("basePrice"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("basePrice"), min);
            return cb.lessThanOrEqualTo(root.get("basePrice"), max);
        };
    }

    public static Specification<RoomType> statusEq(RoomTypeStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<RoomType> qLike(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;
            String pattern = "%" + q.trim().toLowerCase() + "%";
            Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
            Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
            return cb.or(nameLike, descLike);
        };
    }
}

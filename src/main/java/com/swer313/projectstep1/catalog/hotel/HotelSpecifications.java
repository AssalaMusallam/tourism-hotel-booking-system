package com.swer313.projectstep1.catalog.hotel;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class HotelSpecifications {

    private HotelSpecifications() {}

    public static Specification<Hotel> hasStatus(Hotel.Status status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }
    public static Specification<Hotel> hasImage(Boolean hasImage) {
        return (root, query, cb) -> {
            if (hasImage == null) return cb.conjunction();

            if (hasImage) {
                query.distinct(true);
                return cb.isNotEmpty(root.get("images"));
            } else {
                return cb.isEmpty(root.get("images"));
            }
        };
    }

    public static Specification<Hotel> hasPhone(Boolean hasPhone) {
        return (root, query, cb) -> {
            if (hasPhone == null) return cb.conjunction();

            return hasPhone
                    ? cb.and(
                    cb.isNotNull(root.get("phoneNumber")),
                    cb.notEqual(root.get("phoneNumber"), "")
            )
                    : cb.or(
                    cb.isNull(root.get("phoneNumber")),
                    cb.equal(root.get("phoneNumber"), "")
            );
        };
    }

    public static Specification<Hotel> hasWebsite(Boolean hasWebsite) {
        return (root, query, cb) -> {
            if (hasWebsite == null) return cb.conjunction();

            return hasWebsite
                    ? cb.and(
                    cb.isNotNull(root.get("websiteUrl")),
                    cb.notEqual(root.get("websiteUrl"), "")
            )
                    : cb.or(
                    cb.isNull(root.get("websiteUrl")),
                    cb.equal(root.get("websiteUrl"), "")
            );
        };
    }

    public static Specification<Hotel> hasEmail(Boolean hasEmail) {
        return (root, query, cb) -> {
            if (hasEmail == null) return cb.conjunction();

            return hasEmail
                    ? cb.and(
                    cb.isNotNull(root.get("email")),
                    cb.notEqual(root.get("email"), "")
            )
                    : cb.or(
                    cb.isNull(root.get("email")),
                    cb.equal(root.get("email"), "")
            );
        };
    }

    public static Specification<Hotel> cityContains(String city) {
        return (root, query, cb) -> (city == null || city.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("city")), "%" + city.trim().toLowerCase() + "%");
    }

    public static Specification<Hotel> countryContains(String country) {
        return (root, query, cb) -> (country == null || country.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("country")), "%" + country.trim().toLowerCase() + "%");
    }

    public static Specification<Hotel> nameOrAddressOrDescriptionContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("address")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }

    public static Specification<Hotel> ratingBetween(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            if (min != null && max != null) return cb.between(root.get("rating"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("rating"), min);
            return cb.lessThanOrEqualTo(root.get("rating"), max);
        };
    }

    //  ManyToMany: Hotel.amenities -> Amenity.name
    public static Specification<Hotel> hasAmenity(String amenityName) {
        return (root, query, cb) -> {
            if (amenityName == null || amenityName.isBlank()) return cb.conjunction();

            query.distinct(true);
            var join = root.joinSet("amenities", JoinType.INNER);
            return cb.equal(cb.lower(join.get("name")), amenityName.trim().toLowerCase());
        };
    }
}
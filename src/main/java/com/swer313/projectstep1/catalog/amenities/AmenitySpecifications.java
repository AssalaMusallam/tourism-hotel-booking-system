package com.swer313.projectstep1.catalog.amenities;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
public class AmenitySpecifications {

    private AmenitySpecifications() {}

    public static Specification<Amenity> nameEq(String name) {
        return (root, query, cb) ->
                (name == null || name.isBlank())
                        ? null
                        : cb.equal(cb.lower(root.get("name")), name.trim().toLowerCase());
    }

    public static Specification<Amenity> categoryEq(Amenity.AmenityCategory category) {
        return (root, query, cb) ->
                category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Amenity> premiumEq(Boolean premium) {
        return (root, query, cb) ->
                premium == null ? null : cb.equal(root.get("isPremium"), premium);
    }

    public static Specification<Amenity> activeEq(Boolean active) {
        return (root, query, cb) ->
                active == null ? null : cb.equal(root.get("isActive"), active);
    }

    public static Specification<Amenity> qLike(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;
            String pattern = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Amenity> createdAtBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("createdAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    public static Specification<Amenity> roomTypeIdEq(Long roomTypeId) {
        return (root, query, cb) -> {
            if (roomTypeId == null) return null;
            /*
 ليش؟
لأنه في join ممكن يرجع duplicates 😅
مثال:
لو amenity مرتبطة بأكثر من room
الـ join ممكن يرجع نفس amenity أكثر من مرة
👉 distinct(true) يحل المشكلة
             */
            query.distinct(true);
            return cb.equal(root.join("roomTypes").get("id"), roomTypeId);
        };
    }
    /*
هذا method بعمل filter:
"هاتلي كل الـ amenities اللي مرتبطة بـ RoomType معيّن"
لو المستخدم أعطى:
roomTypeId = 5
بدنا:
كل amenities اللي موجودة في room رقم 5
🧠 الفكرة الكبيرة
هذا الكود يسمح لك تعملي:
GET /api/amenities?roomTypeId=5
👉 وترجعي amenities حسب الغرفة 🔥
⚠️ ليش هذا مهم؟
لأن بدون هذا:
ما بتقدري تربطي بين rooms و amenities في البحث


     */


}
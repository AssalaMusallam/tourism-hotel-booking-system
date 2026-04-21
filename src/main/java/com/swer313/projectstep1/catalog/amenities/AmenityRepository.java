package com.swer313.projectstep1.catalog.amenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long>, JpaSpecificationExecutor<Amenity> {
/*
📌 معناها:
هل في record موجود بنفس الاسم؟
IgnoreCase يعني بدون ما يفرق بين:
"Pool"
"pool"
 */
    boolean existsByNameIgnoreCase(String name);
/*
📌 هاي بتستخدم غالباً مع update
📌 معناها:
هل في اسم مكرر بس مش لنفس الـ ID
📌 مثال:
عندك:
ID=1 → "wifi"
ID=2 → "parking"
بدك تعدل ID=2 وتحط:
name = "wifi"
هون:
existsByNameIgnoreCaseAndIdNot("wifi", 2)
👉 رح ترجع true
👉 لأنه في واحد ثاني بنفس الاسم (ID=1)
ليش مهمين هدول؟
💡 عشان تمنع تكرار البيانات (Duplicate)
 */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}

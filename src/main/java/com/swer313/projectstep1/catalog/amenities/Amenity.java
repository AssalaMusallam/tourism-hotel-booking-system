package com.swer313.projectstep1.catalog.amenities;
import com.swer313.projectstep1.catalog.room.RoomType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(
        //الجدول في الداتابيس اسمه amenities
        name = "amenities",
        /*
⚡ طيب شو يعني indexes؟
الـ index زي "فهرس" في كتاب 📖
بدل ما الداتابيس تقرأ كل الجدول (slow 😴)،
بتستخدم index عشان توصل للبيانات بسرعة 🚀
 */
        indexes = {
                /*
 👉 عملتي index على name
→ عشان البحث بالاسم يكون سريع
                  */
                @Index(name = "idx_amenity_name", columnList = "name"),
                @Index(name = "idx_amenity_category", columnList = "category"),
                @Index(name = "idx_amenity_active", columnList = "is_active"),
                @Index(name = "idx_amenity_premium", columnList = "is_premium")
        }
   /*
   💡 ربط مع الكود تبعك
إنتِ فعليًا بتستخدميهم هون 👇
في AmenitySpecifications:
premiumEq(...)
activeEq(...)
categoryEq(...)
nameEq(...)
👉 كل هدول queries رح تستفيد من الـ indexes 🔥

    */
)
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Amenity name cannot be blank")
    @Size(min = 3, max = 100, message = "Amenity name must be between 3 and 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Amenity description cannot be blank")
    @Size(min = 10, max = 500, message = "Amenity description must be between 10 and 500 characters")
    @Column(nullable = false, length = 500)
    private String description;

    @NotNull(message = "Amenity category cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AmenityCategory category;

    @Column(name = "is_premium", nullable = false)
    //هل الخدمة مدفوعة ولا لا
    private boolean isPremium = false;

    @Column(name = "is_active", nullable = false)
    /*
    // الـ amenity "موجودة/مستخدمة" أو "محذوفة منطقيًا"
    عندك تطبيق حجز
في amenity اسمها "Gym"
بدل ما تمسحيها:
بتخليها inactive
→ المستخدمين ما يشوفوها
→ بس إنتي تقدري ترجعيها
🟢 active = true
تظهر للمستخدم
ممكن تنحجز
موجودة في النظام بشكل طبيعي
🔴 active = false
موقوفة أو محذوفة منطقيًا
ما تظهر للمستخدم
بس لسه موجودة في الداتابيس

     */
    private boolean isActive = true;
    @ManyToMany(mappedBy = "amenities")
    private Set<RoomType> roomTypes = new HashSet<>();
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    //تاريخ إنشاء الـ amenity,👉 ينحط تلقائي عند الإنشاء
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    //تاريخ آخر تحديث للـ amenity,👉 ينحط تلقائي عند كل تحديث
    private LocalDateTime updatedAt;

    public Amenity() {}

    public Amenity(String name, String description, AmenityCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AmenityCategory getCategory() { return category; }
    public void setCategory(AmenityCategory category) { this.category = category; }

    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Set<RoomType> getRoomTypes() { return roomTypes; }
    public void setRoomTypes(Set<RoomType> roomTypes) { this.roomTypes = roomTypes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Amenity)) return false;
        Amenity other = (Amenity) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    public enum AmenityCategory {
        CONNECTIVITY,
        WELLNESS,
        ENTERTAINMENT,
        COMFORT,
        DINING,
        PARKING,
        SECURITY,
        CLEANING,
        ACCESSIBILITY,
        OUTDOOR
    }
    /*
    👉 الـ enum يعني:
مجموعة قيم محددة وثابتة
يعني بدل ما أي حد يكتب أي نص عشوائي ❌
إنتِ بتحددي خيارات جاهزة فقط ✅
    :هاي تصنيفات للـ amenities
    AmenityCategory يعني:
"نوع الخدمة داخل الفندق"
2. 🧼 ينظم البيانات
بدل ما يكون عندك chaos:
❌ category = "random text"
✔️ يصير:
كل amenity إلها نوع واضح ومنظم
بدل ما يصير:
"WIFI"
"wifi"
"Wfi"
👉 كلها تخرب 😅
لكن مع enum:
CONNECTIVITY
✔️ ثابت
✔️ بدون أخطاء spelling
| Category        | Description                     | Examples                                              | Meaning                          |
|----------------|----------------------------------|--------------------------------------------------------|----------------------------------|
| CONNECTIVITY   | كل إشي له علاقة بالاتصال        | WiFi, Internet cable, Smart TV apps                   | يربطك بالعالم 🌍                |
| WELLNESS       | الصحة والاسترخاء                | Spa, Sauna, Gym, Massage                              | راحة الجسم والنفس 😌            |
| ENTERTAINMENT  | الترفيه                         | TV, Netflix, Game room, Cinema                        | أشياء للتسلية 🎬                |
| COMFORT        | الراحة داخل الغرفة              | AC, Heating, Comfortable bed, Extra pillows           | إقامة مريحة 🛌                  |
| DINING         | الأكل والشرب                    | Restaurant, Breakfast, Room service, Mini bar         | كل ما له علاقة بالأكل 🍕        |
| PARKING        | مواقف السيارات                  | Free parking, Valet parking, Private parking          | مكان للسيارة 🚙                 |
| SECURITY       | الأمان                          | CCTV, Security guards, Safe box, Key card access      | حماية النزيل 🔒                |
| CLEANING       | النظافة                         | Daily cleaning, Laundry, Housekeeping                 | نظافة الغرفة 🧼                |
| ACCESSIBILITY  | تسهيلات لذوي الاحتياجات الخاصة | Wheelchair access, Elevator, Special bathrooms        | سهولة الوصول ♿                |
| OUTDOOR        | مرافق خارجية                   | Garden, Swimming pool, Balcony, Outdoor seating       | أشياء خارجية 🌴                |

     */


}
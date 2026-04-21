package com.swer313.projectstep1.catalog.amenities;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
public interface AmenityService {
//جلب قائمة amenities مع فلترة + pagination
    PagedResponse<AmenityResponseDTO> getAll(
            Pageable pageable,
            String name,
            Amenity.AmenityCategory category,
            Boolean premium,
            Boolean active,
            String q,
            Long roomTypeId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo
    );
//جلب amenity وحدة حسب id
    AmenityResponseDTO getById(Long id);
//انشاء amenity جديد
    AmenityResponseDTO create(AmenityRequestDTO dto);
//تحديث amenity موجود
    AmenityResponseDTO update(Long id, AmenityRequestDTO dto);
//تفعيل amenity
    AmenityResponseDTO activate(Long id);
//تعطيل amenity
    AmenityResponseDTO deactivate(Long id);
//استعادة amenity تم حذفها (soft delete)
    AmenityResponseDTO restore(Long id);
/*
soft delete
يعني:
active = false
💡 ما تنحذف من الداتابيس
 */
    void delete(Long id);
//حذف نهائي من الداتابيس
    void hardDelete(Long id);
/*
اقتراحات بحث (autocomplete)
💡 مثل:
المستخدم يكتب: "wi"
يرجع: WiFi
 */
    List<AmenityMinimalDTO> suggest(String q, Boolean active);
/*
جلب قائمة خفيفة (id + name فقط)
💡 تستخدم في:
dropdown
select menu
 */
    PagedResponse<AmenityMinimalDTO> minimal(Boolean active, Pageable pageable);
//التحقق من وجود اسم معين (لمنع التكرار)
    Map<String, Object> exists(String name);
//تحديث حالة مجموعة من amenities دفعة واحدة (تفعيل/تعطيل)
    Map<String, Object> bulkStatus(AmenityBulkStatusRequest body);
}
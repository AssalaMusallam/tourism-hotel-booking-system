package com.swer313.projectstep1.admin;

/**
 * DTO لأشهر أنواع الغرف.
 *
 * الـ Admin يسأل: "أي نوع غرف أكثر طلباً؟"
 *
 * مهم جداً: الـ constructor لازم يكون بهاي الأسماء بالضبط
 * عشان الـ JPQL في BookingRepository يقدر يبنيه تلقائياً:
 * SELECT new PopularRoomDto(b.roomType.name, COUNT(b)) FROM Booking b ...
 */
public class PopularRoomDto {

    private final String roomTypeName;  // اسم نوع الغرفة
    private final Long   bookingsCount; // عدد الحجوزات المؤكدة

    // الـ JPQL بيستخدم هاد الـ constructor مباشرة — ما تغير الأسماء
    public PopularRoomDto(String roomTypeName, Long bookingsCount) {
        this.roomTypeName  = roomTypeName;
        this.bookingsCount = bookingsCount;
    }

    public String getRoomTypeName()  { return roomTypeName; }
    public Long   getBookingsCount() { return bookingsCount; }
}
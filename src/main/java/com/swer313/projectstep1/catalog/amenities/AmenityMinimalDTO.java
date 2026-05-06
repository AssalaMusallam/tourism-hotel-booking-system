package com.swer313.projectstep1.catalog.amenities;

public class AmenityMinimalDTO {
    private final Long id;
    private final String name;
    private final String nameEn;

    public AmenityMinimalDTO(Long id, String name) {
        this(id, name, null);
    }

    public AmenityMinimalDTO(Long id, String name, String nameEn) {
        this.id = id;
        this.name = name;
        this.nameEn = nameEn;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getNameEn() { return nameEn; }
}
/*
شو معناها؟
هذا DTO خفيف جدًا، بيرجع:
id
name
فقط، بدون:
description
category
premium
active
createdAt
updatedAt
//////////////
ليش نحتاجه؟
لأن أحيانًا الواجهة أو الـ frontend ما بدها كل التفاصيل.
بدها فقط شيء خفيف مثل:
dropdown list
autocomplete
suggestion list
select menu
مثلاً المستخدم يكتب:
wi
فيرجعله:
WiFi
Wireless Charger
هون ما في داعي ترجعي الوصف والتواريخ وكل شيء.
 */




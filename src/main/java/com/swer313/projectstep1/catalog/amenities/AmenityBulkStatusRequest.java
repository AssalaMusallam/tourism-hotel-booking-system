package com.swer313.projectstep1.catalog.amenities;

import java.util.List;
//هذا request خاص لعملية معينة:
//تعديل status لعدة amenities دفعة واحدة

public class AmenityBulkStatusRequest {
    private List<Long> ids;
    private Boolean active;

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
/*
مش نفس دور AmenityRequestDTO و AmenityResponseDTO.
شو معناها؟
هذا كلاس معمول عشان يستقبل request خاص لعملية:
تغيير حالة أكثر من amenity مرة واحدة
يعني بدل ما تبعتي 10 requests:
deactivate id=1
deactivate id=2
deactivate id=3
بتبعتي request واحد فيه:
قائمة ids
والحالة الجديدة active = true/false
/////////////////////////////////////////////////////////////
مثال عليه:

request مثل هيك:
{
  "ids": [1, 2, 3],
  "active": false
}
يعني:
عطّل amenities اللي أرقامهم 1 و2 و3
////////////////////////////////////////////////////////////
ليش ما نستخدم AmenityRequestDTO بدل منه؟
لأن AmenityRequestDTO معمول لشيء ثاني، فيه:
name
description
category
premium
active
بينما هون في bulk status، إحنا ما بدنا اسم ولا وصف ولا category.
بدنا فقط:
مين العناصر؟
شو الحالة الجديدة؟
فلو استخدمتي AmenityRequestDTO هون، بصير الكلاس مش مناسب للغرض.




 */
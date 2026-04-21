package com.swer313.projectstep1.waitinglist;

public enum WaitingListStatus {
    WAITING,    // مسجل وينتظر
    NOTIFIED,   // اتبعتله إشعار، عنده 24 ساعة يحجز
    EXPIRED,    // فات الـ 24 ساعة أو التاريخ عدى
    CANCELLED   // ألغى تسجيله بنفسه
}
package com.swer313.projectstep1.availabilitypricing.availability;

/**
 * Projection interface لاستقبال نتيجة الـ batch count query.
 * بيتجنب N+1 — query واحدة تجيب counts لكل roomTypes.
 */
public interface BookedCountProjection {
    Long getRoomTypeId();
    Long getBookedCount();
}
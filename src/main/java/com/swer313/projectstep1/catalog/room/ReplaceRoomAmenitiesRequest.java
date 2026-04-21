package com.swer313.projectstep1.catalog.room;

import java.util.Set;

public class ReplaceRoomAmenitiesRequest {
    private Set<Long> amenityIds;

    public Set<Long> getAmenityIds() {
        return amenityIds;
    }

    public void setAmenityIds(Set<Long> amenityIds) {
        this.amenityIds = amenityIds;
    }
}
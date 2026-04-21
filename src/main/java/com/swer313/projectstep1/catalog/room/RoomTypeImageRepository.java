package com.swer313.projectstep1.catalog.room;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomTypeImageRepository extends JpaRepository<RoomTypeImage, Long> {
    List<RoomTypeImage> findByRoomTypeId(Long roomTypeId);
}
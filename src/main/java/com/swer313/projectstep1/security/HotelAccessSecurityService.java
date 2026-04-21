package com.swer313.projectstep1.security;

import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRole;
import org.springframework.stereotype.Service;

@Service("hotelAccessSecurityService")
public class HotelAccessSecurityService {

    private final CurrentUserService currentUserService;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;

    public HotelAccessSecurityService(CurrentUserService currentUserService,
                                      HotelRepository hotelRepository,
                                      RoomTypeRepository roomTypeRepository) {
        this.currentUserService = currentUserService;
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
    }

    public boolean canAccessHotel(Long hotelId) {
        User currentUser = currentUserService.getCurrentUser();

        if (currentUser.getRole() == UserRole.ADMIN) return true;
        if (currentUser.getRole() != UserRole.MANAGER) return false;

        return currentUser.managesHotel(hotelId);
    }

    public boolean canAccessRoomType(Long roomTypeId) {
        User currentUser = currentUserService.getCurrentUser();

        if (currentUser.getRole() == UserRole.ADMIN) return true;
        if (currentUser.getRole() != UserRole.MANAGER) return false;

        RoomType roomType = roomTypeRepository.findById(roomTypeId).orElse(null);
        if (roomType == null) return false;

        Long hotelId = roomType.getHotel().getId();
        return currentUser.managesHotel(hotelId);
    }
}
package com.swer313.projectstep1.security;

import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.room.RoomType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelAccessSecurityServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private com.swer313.projectstep1.catalog.room.RoomTypeRepository roomTypeRepository;

    @InjectMocks
    private HotelAccessSecurityService service;

    @Test
    void canAccessHotel_admin_true() {
        var user = new com.swer313.projectstep1.user.User();
        user.setRole(com.swer313.projectstep1.user.UserRole.ADMIN);
        when(currentUserService.getCurrentUser()).thenReturn(user);

        assertTrue(service.canAccessHotel(1L));
    }

    @Test
    void canAccessRoomType_manager_checksHotel() {
        var user = new com.swer313.projectstep1.user.User();
        user.setRole(com.swer313.projectstep1.user.UserRole.MANAGER);
        when(currentUserService.getCurrentUser()).thenReturn(user);

        RoomType rt = new RoomType();
        var hotel = new Hotel(); hotel.setId(50L);
        rt.setHotel(hotel);
        when(roomTypeRepository.findById(10L)).thenReturn(Optional.of(rt));

        // manager has no managed hotels by default → false
        assertFalse(service.canAccessRoomType(10L));
    }
}



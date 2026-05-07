package com.swer313.projectstep1.manager;

import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelMapper;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.hotel.HotelResponseDto;
import com.swer313.projectstep1.catalog.room.RoomTypeMapper;
import com.swer313.projectstep1.catalog.room.RoomTypeResponseDto;
import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class ManagerHotelController {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;

    public ManagerHotelController(UserRepository userRepository, HotelRepository hotelRepository) {
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
    }

    @GetMapping("/api/manager/my-hotel")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMyHotel(@AuthenticationPrincipal UserDetails userDetails) {
        Hotel hotel = firstManagedHotelOrNull(userDetails);
        if (hotel == null) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(Map.of("error", "No hotel assigned to this manager"));
        }
        return ResponseEntity.ok(HotelMapper.toDto(hotel));
    }

    @GetMapping("/api/manager/hotels")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<HotelResponseDto>> getMyHotels(@AuthenticationPrincipal UserDetails userDetails) {
        Set<Hotel> managedHotels = currentUser(userDetails).getManagedHotels();
        if (managedHotels == null || managedHotels.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<HotelResponseDto> hotels = managedHotels.stream()
                .sorted(Comparator.comparing(Hotel::getId))
                .map(HotelMapper::toDto)
                .toList();
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/api/manager/my-hotel/room-types")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMyRoomTypes(@AuthenticationPrincipal UserDetails userDetails) {
        Hotel hotel = firstManagedHotelOrNull(userDetails);
        if (hotel == null) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(Map.of("error", "No hotel assigned to this manager"));
        }
        List<RoomTypeResponseDto> roomTypes = hotel.getRoomTypes().stream()
                .map(RoomTypeMapper::toDto)
                .toList();
        return ResponseEntity.ok(roomTypes);
    }

    @PutMapping("/api/manager/my-hotel")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @Transactional
    public ResponseEntity<?> updateMyHotel(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        Hotel hotel = firstManagedHotelOrNull(userDetails);
        if (hotel == null) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(Map.of("error", "No hotel assigned to this manager"));
        }
        applyIfPresent(request, "name", hotel::setName);
        applyIfPresent(request, "address", hotel::setAddress);
        applyIfPresent(request, "description", hotel::setDescription);
        applyIfPresent(request, "city", hotel::setCity);
        applyIfPresent(request, "country", hotel::setCountry);
        applyIfPresent(request, "phoneNumber", hotel::setPhoneNumber);
        applyIfPresent(request, "email", hotel::setEmail);
        applyIfPresent(request, "websiteUrl", hotel::setWebsiteUrl);
        if (request.containsKey("rating")) hotel.setRating(toDouble(request.get("rating")));
        if (request.containsKey("latitude")) hotel.setLatitude(toDouble(request.get("latitude")));
        if (request.containsKey("longitude")) hotel.setLongitude(toDouble(request.get("longitude")));
        if (request.containsKey("checkInTime")) hotel.setCheckInTime(toLocalTime(request.get("checkInTime")));
        if (request.containsKey("checkOutTime")) hotel.setCheckOutTime(toLocalTime(request.get("checkOutTime")));
        applyIfPresent(request, "policies", hotel::setPolicies);
        applyIfPresent(request, "cancellationPolicySummary", hotel::setCancellationPolicySummary);
        if (request.containsKey("status") && request.get("status") != null) {
            hotel.setStatus(Hotel.Status.valueOf(request.get("status").toString()));
        }

        return ResponseEntity.ok(HotelMapper.toDto(hotelRepository.save(hotel)));
    }

    private User currentUser(UserDetails userDetails) {
        if (userDetails == null) throw new ResponseStatusException(NOT_FOUND, "Manager user not found");
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Manager user not found"));
    }

    private Hotel firstManagedHotelOrNull(UserDetails userDetails) {
        Set<Hotel> hotels = currentUser(userDetails).getManagedHotels();
        if (hotels == null || hotels.isEmpty()) {
            return null;
        }
        return hotels.stream()
                .min(Comparator.comparing(Hotel::getId))
                .orElse(null);
    }

    private void applyIfPresent(Map<String, Object> request, String key, java.util.function.Consumer<String> setter) {
        if (request.containsKey(key)) {
            Object value = request.get(key);
            setter.accept(value == null ? null : value.toString());
        }
    }

    private Double toDouble(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        return Double.valueOf(value.toString());
    }

    private LocalTime toLocalTime(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        return LocalTime.parse(value.toString());
    }
}

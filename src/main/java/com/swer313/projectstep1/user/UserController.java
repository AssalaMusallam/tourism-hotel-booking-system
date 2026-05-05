package com.swer313.projectstep1.user;

import com.swer313.projectstep1.catalog.hotel.HotelMapper;
import com.swer313.projectstep1.catalog.hotel.HotelResponseDto;
import com.swer313.projectstep1.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public UserController(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponseDTO> me() {
        return ResponseEntity.ok(new UserResponseDTO(currentUserService.getCurrentUser()));
    }

    @PutMapping("/api/users/me")
    public ResponseEntity<UserResponseDTO> updateMe(@Valid @RequestBody UserUpdateRequest request) {
        User user = currentUserService.getCurrentUser();
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().isBlank() ? null : request.getPhone());
        }
        return ResponseEntity.ok(new UserResponseDTO(userRepository.save(user)));
    }

    @PatchMapping("/api/users/me")
    public ResponseEntity<UserResponseDTO> patchMe(@Valid @RequestBody UserUpdateRequest request) {
        return updateMe(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users")
    public ResponseEntity<List<UserResponseDTO>> getUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean active
    ) {
        List<UserResponseDTO> users = userRepository.findAll().stream()
                .filter(user -> role == null || user.getRole() == role)
                .filter(user -> active == null || user.isActive() == active)
                .sorted(Comparator.comparing(User::getId))
                .map(UserResponseDTO::new)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/admin/users/{id}/status")
    public ResponseEntity<UserResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(request.getActive());
        return ResponseEntity.ok(new UserResponseDTO(userRepository.save(user)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/admin/users/{id}/role")
    public ResponseEntity<UserResponseDTO> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setRole(request.getRole());
        return ResponseEntity.ok(new UserResponseDTO(userRepository.save(user)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users/{id}/hotels")
    public ResponseEntity<List<HotelResponseDto>> getManagedHotels(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return ResponseEntity.ok(
                user.getManagedHotels().stream()
                        .sorted(Comparator.comparing(hotel -> hotel.getName().toLowerCase()))
                        .map(HotelMapper::toDto)
                        .toList()
        );
    }
}

package com.swer313.projectstep1.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ManagerNotificationController {

    private final NotificationService notificationService;

    @GetMapping("/api/manager/notifications")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<NotificationDTOs.PagedNotificationResponse> getManagerNotifications(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getByEmail(user.getUsername(), page, size));
    }
}

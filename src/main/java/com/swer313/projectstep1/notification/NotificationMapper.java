package com.swer313.projectstep1.notification;

import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDTOs.NotificationResponse toResponse(Notification n) {
        if (n == null) return null;
        return NotificationDTOs.NotificationResponse.builder()
                .id(n.getId())
                .recipientEmail(n.getRecipientEmail())
                .recipientName(n.getRecipientName())
                .type(n.getType())
                .status(n.getStatus())
                .subject(n.getSubject())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .retryCount(n.getRetryCount())
                .errorMessage(n.getErrorMessage())
                .createdAt(n.getCreatedAt())
                .sentAt(n.getSentAt())
                .build();
    }
}

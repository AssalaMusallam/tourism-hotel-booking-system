package com.swer313.projectstep1.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateBuilderTest {

    private final EmailTemplateBuilder builder = new EmailTemplateBuilder();

    @Test
    void buildSubject_and_body_forBookingConfirmed() {
        NotificationDTOs.SendRequest req = NotificationDTOs.SendRequest.builder()
                .recipientName("John")
                .bookingReference("BK-1")
                .hotelName("Hotel X")
                .build();

        String subject = builder.buildSubject(NotificationType.BOOKING_CONFIRMED, req);
        String body = builder.buildBody(NotificationType.BOOKING_CONFIRMED, req);

        assertThat(subject).contains("Booking Confirmed");
        assertThat(body).contains("Hotel X");
    }

    @Test
    void buildBody_forCustom_returnsSimple() {
        NotificationDTOs.SendRequest req = NotificationDTOs.SendRequest.builder()
                .recipientName("A")
                .build();

        String body = builder.buildBody(NotificationType.CUSTOM, req);
        assertThat(body).contains("Please see the message");
    }
}


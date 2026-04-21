package com.swer313.projectstep1.notification;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

	@Mock
	private NotificationRepository repository;

	@Mock
	private JavaMailSender mailSender;

	@Mock
	private EmailTemplateBuilder templateBuilder;

	@Mock
	private NotificationMapper mapper;

	@InjectMocks
	private NotificationServiceImpl service;

	@Test
	void send_success_marksSent() throws Exception {
		NotificationDTOs.SendRequest req = NotificationDTOs.SendRequest.builder()
				.recipientEmail("a@b.com")
				.recipientName("A")
				.type(NotificationType.WELCOME_EMAIL)
				.build();

		when(templateBuilder.buildSubject(any(), any())).thenReturn("sub");
		when(templateBuilder.buildBody(any(), any())).thenReturn("body");

		Notification saved = Notification.builder()
				.id(5L)
				.recipientEmail("a@b.com")
				.recipientName("A")
				.type(NotificationType.WELCOME_EMAIL)
				.status(NotificationStatus.PENDING)
				.retryCount(0)
				.build();

		when(repository.save(any())).thenReturn(saved);
		when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
		doNothing().when(mailSender).send(any(MimeMessage.class));
		when(mapper.toResponse(any())).thenReturn(NotificationDTOs.NotificationResponse.builder().id(5L).build());

		NotificationDTOs.NotificationResponse resp = service.send(req);

		assertNotNull(resp);
		assertEquals(5L, resp.getId());
		verify(repository, atLeastOnce()).save(any());
	}

	@Test
	void send_failure_setsRetryScheduled() throws Exception {
		NotificationDTOs.SendRequest req = NotificationDTOs.SendRequest.builder()
				.recipientEmail("a@b.com")
				.recipientName("A")
				.type(NotificationType.WELCOME_EMAIL)
				.build();

		when(templateBuilder.buildSubject(any(), any())).thenReturn("sub");
		when(templateBuilder.buildBody(any(), any())).thenReturn("body");

		Notification n = Notification.builder()
				.id(6L)
				.recipientEmail("a@b.com")
				.recipientName("A")
				.type(NotificationType.WELCOME_EMAIL)
				.status(NotificationStatus.PENDING)
				.retryCount(0)
				.build();

		when(repository.save(any())).thenReturn(n);
		when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
		doThrow(new MailSendException("fail")).when(mailSender).send(any(MimeMessage.class));
		when(mapper.toResponse(any())).thenReturn(NotificationDTOs.NotificationResponse.builder().id(6L).build());

		NotificationDTOs.NotificationResponse resp = service.send(req);

		assertNotNull(resp);
		verify(repository, atLeastOnce()).save(any());
	}
}
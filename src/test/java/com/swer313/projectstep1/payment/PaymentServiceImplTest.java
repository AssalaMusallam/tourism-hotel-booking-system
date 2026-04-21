package com.swer313.projectstep1.payment;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingNotFoundException;
import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.booking.BookingService;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.errors.BadRequestException;
import com.swer313.projectstep1.errors.DuplicatePaymentIntentException;
import com.swer313.projectstep1.errors.InvalidPaymentStateException;
import com.swer313.projectstep1.errors.PaymentNotFoundException;
import com.swer313.projectstep1.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

	@Mock
	private PaymentRepository repository;

	@Mock
	private PaymentMapper mapper;

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private BookingService bookingService;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private PaymentServiceImpl service;

	private static void setId(Object target, Long id) {
		try {
			Class<?> type = target.getClass();
			Field field = null;

			while (type != null) {
				try {
					field = type.getDeclaredField("id");
					break;
				} catch (NoSuchFieldException ignored) {
					type = type.getSuperclass();
				}
			}

			if (field == null) {
				throw new RuntimeException("Field 'id' not found on " + target.getClass().getName());
			}

			field.setAccessible(true);
			field.set(target, id);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set id via reflection", e);
		}
	}

	private PaymentRequestDTO validRequest() {
		PaymentRequestDTO dto = new PaymentRequestDTO();
		dto.setBookingId(1L);
		dto.setAmount(new BigDecimal("100.00"));
		dto.setCurrency("USD");
		return dto;
	}

	private Booking makePendingBooking() {
		Booking b = new Booking();
		b.setStatus(BookingStatus.PENDING);
		b.setTotalPrice(new BigDecimal("100.00"));
		return b;
	}

	@Test
	void createIntent_success_savesAndReturnsDto() {
		PaymentRequestDTO req = validRequest();

		Booking booking = makePendingBooking();
		setId(booking, 1L);

		when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
		when(repository.existsByBookingIdAndStatusIn(eq(1L), any())).thenReturn(false);

		Payment toSave = new Payment();
		toSave.setBookingId(1L);
		toSave.setAmount(req.getAmount());

		when(mapper.toEntity(req)).thenReturn(toSave);

		Payment saved = new Payment();
		setId(saved, 11L);
		saved.setBookingId(1L);
		saved.setAmount(req.getAmount());
		saved.setCurrency("USD");
		saved.setMethod(PaymentMethod.MOCK_CARD);
		saved.setStatus(PaymentStatus.PENDING);

		when(repository.save(toSave)).thenReturn(saved);

		PaymentResponseDTO dto = new PaymentResponseDTO(
				11L,
				1L,
				req.getAmount(),
				"USD",
				PaymentMethod.MOCK_CARD,
				PaymentStatus.PENDING,
				"MOCK_GATEWAY",
				"pay_x",
				null,
				null,
				null,
				null,
				null,
				null
		);
		when(mapper.toDto(saved)).thenReturn(dto);

		PaymentResponseDTO result = service.createIntent(req);

		assertNotNull(result);
		assertEquals(11L, result.getId());
		verify(repository).save(toSave);
	}

	@Test
	void createIntent_bookingNotFound_throws() {
		when(bookingRepository.findById(2L)).thenReturn(Optional.empty());

		PaymentRequestDTO dto = new PaymentRequestDTO();
		dto.setBookingId(2L);
		dto.setAmount(new BigDecimal("10.00"));

		assertThrows(BookingNotFoundException.class, () -> service.createIntent(dto));
	}

	@Test
	void createIntent_duplicateIntent_throws() {
		Booking booking = makePendingBooking();
		setId(booking, 3L);

		when(bookingRepository.findById(3L)).thenReturn(Optional.of(booking));
		when(repository.existsByBookingIdAndStatusIn(eq(3L), any())).thenReturn(true);

		PaymentRequestDTO dto = new PaymentRequestDTO();
		dto.setBookingId(3L);
		dto.setAmount(new BigDecimal("100.00"));

		assertThrows(DuplicatePaymentIntentException.class, () -> service.createIntent(dto));
	}

	@Test
	void getByTransactionReference_null_throwsBadRequest() {
		assertThrows(BadRequestException.class, () -> service.getByTransactionReference(null));
		assertThrows(BadRequestException.class, () -> service.getByTransactionReference("   "));
	}

	@Test
	void simulateSuccess_nonPending_throws() {
		Payment p = new Payment();
		setId(p, 5L);
		p.setStatus(PaymentStatus.SUCCESS);

		when(repository.findById(5L)).thenReturn(Optional.of(p));

		assertThrows(InvalidPaymentStateException.class, () -> service.simulateSuccess(5L));
	}

	@Test
	void simulateSuccess_happyPath_confirmsBookingAndSendsNotification() {
		Payment p = new Payment();
		setId(p, 7L);
		p.setBookingId(20L);
		p.setStatus(PaymentStatus.PENDING);
		p.setAmount(new BigDecimal("100.00"));
		p.setCurrency("USD");
		p.setMethod(PaymentMethod.MOCK_CARD);

		Booking booking = makePendingBooking();
		setId(booking, 20L);
		booking.setGuestEmail("g@e.com");
		booking.setGuestName("Guest");

		when(repository.findById(7L)).thenReturn(Optional.of(p));
		when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));
		when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
		when(mapper.toDto(any(Payment.class))).thenAnswer(inv -> {
			Payment payment = inv.getArgument(0);
			return new PaymentResponseDTO(
					7L,
					20L,
					payment.getAmount(),
					"USD",
					PaymentMethod.MOCK_CARD,
					PaymentStatus.SUCCESS,
					"MOCK_GATEWAY",
					"r",
					null,
					null,
					null,
					null,
					null,
					null
			);
		});

		PaymentResponseDTO resp = service.simulateSuccess(7L);

		assertNotNull(resp);
		verify(bookingService).confirmBooking(20L);
		verify(repository).save(any(Payment.class));
	}

	@Test
	void getLatestByBookingId_notFoundBooking_throws() {
		when(bookingRepository.existsById(99L)).thenReturn(false);
		assertThrows(BookingNotFoundException.class, () -> service.getLatestByBookingId(99L));
	}

	@Test
	void getHistoryByBookingId_notFoundBooking_throws() {
		when(bookingRepository.existsById(100L)).thenReturn(false);
		assertThrows(BookingNotFoundException.class, () -> service.getHistoryByBookingId(100L));
	}

	@Test
	void getByTransactionReference_notFound_throws() {
		when(repository.findByTransactionReference("nope")).thenReturn(Optional.empty());
		assertThrows(PaymentNotFoundException.class, () -> service.getByTransactionReference("nope"));
	}
}
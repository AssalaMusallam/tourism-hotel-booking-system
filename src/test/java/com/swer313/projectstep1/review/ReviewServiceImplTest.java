package com.swer313.projectstep1.review;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.notification.NotificationService;
import com.swer313.projectstep1.payment.PaymentRepository;
import com.swer313.projectstep1.payment.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private HotelRepository hotelRepository;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private ReviewServiceImpl service;

	@Test
	void createReview_success_savesAndReturnsDto() {
		Booking booking = new Booking();
		ReflectionTestUtils.setField(booking, "id", 5L);
		booking.setStatus(BookingStatus.COMPLETED);
		booking.setGuestEmail("g@e.com");
		booking.setGuestName("G");
		booking.setCheckOut(LocalDate.now());

		RoomType rt = new RoomType();
		Hotel hotel = new Hotel();
		ReflectionTestUtils.setField(hotel, "id", 10L);
		hotel.setName("H");
		rt.setHotel(hotel);
		booking.setRoomType(rt);

		when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
		when(paymentRepository.existsByBookingIdAndStatusIn(5L, List.of(PaymentStatus.SUCCESS))).thenReturn(true);
		when(reviewRepository.existsByBooking_Id(5L)).thenReturn(false);
		when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));

		Review saved = new Review();
		ReflectionTestUtils.setField(saved, "id", 99L);
		saved.setBooking(booking);
		saved.setHotelId(10L);
		saved.setRating(5);
		saved.setGuestEmail("g@e.com");
		saved.setComment("Excellent");

		when(reviewRepository.save(any(Review.class))).thenReturn(saved);

		ReviewRequestDTO dto = new ReviewRequestDTO();
		dto.setBookingId(5L);
		dto.setGuestEmail("g@e.com");
		dto.setRating(5);
		dto.setComment("Excellent");

		ReviewResponseDTO resp = service.createReview(dto);

		assertNotNull(resp);
		assertEquals(99L, resp.getId());
		assertEquals(5L, resp.getBookingId());
		assertEquals(10L, resp.getHotelId());
		assertEquals("H", resp.getHotelName());
		assertEquals("G", resp.getGuestName());
		assertEquals("g@e.com", resp.getGuestEmail());
		assertEquals(5, resp.getRating());
	}

	@Test
	void createReview_bookingNotCompleted_throws() {
		Booking booking = new Booking();
		ReflectionTestUtils.setField(booking, "id", 6L);
		booking.setStatus(BookingStatus.PENDING);

		when(bookingRepository.findById(6L)).thenReturn(Optional.of(booking));

		ReviewRequestDTO dto = new ReviewRequestDTO();
		dto.setBookingId(6L);
		dto.setGuestEmail("x@x.com");
		dto.setRating(4);

		assertThrows(ReviewNotAllowedException.class, () -> service.createReview(dto));
	}

	@Test
	void getRatingSummary_handlesNullRow() {
		Hotel hotel = new Hotel();
		ReflectionTestUtils.setField(hotel, "id", 2L);
		hotel.setName("Z");

		when(hotelRepository.findById(2L)).thenReturn(Optional.of(hotel));
		when(reviewRepository.getRatingSummaryByHotelId(2L)).thenReturn(null);

		RatingSummaryDTO dto = service.getRatingSummary(2L);

		assertNotNull(dto);
		assertEquals(2L, dto.getHotelId());
		assertEquals("Z", dto.getHotelName());
		assertEquals(0L, dto.getTotalReviews());
		assertEquals(0.0, dto.getAverageRating());
	}

	@Test
	void getHotelReviews_delegatesAndMaps() {
		Hotel hotel = new Hotel();
		ReflectionTestUtils.setField(hotel, "id", 3L);
		hotel.setName("H3");

		when(hotelRepository.findById(3L)).thenReturn(Optional.of(hotel));

		Booking booking = new Booking();
		ReflectionTestUtils.setField(booking, "id", 7L);
		booking.setGuestName("G");
		booking.setCheckOut(LocalDate.now());

		Review review = new Review();
		ReflectionTestUtils.setField(review, "id", 8L);
		review.setBooking(booking);
		review.setHotelId(3L);
		review.setRating(4);
		review.setGuestEmail("g@e.com");
		review.setComment("Good");

		PageRequest pageable = PageRequest.of(0, 10);

		when(reviewRepository.findByHotelIdOrderByCreatedAtDesc(3L, pageable))
				.thenReturn(new PageImpl<>(List.of(review), pageable, 1));

		var page = service.getHotelReviews(3L, pageable);

		assertNotNull(page);
		assertEquals(1, page.getContent().size());
		assertEquals(8L, page.getContent().get(0).getId());
		assertEquals(7L, page.getContent().get(0).getBookingId());
		assertEquals("H3", page.getContent().get(0).getHotelName());
		assertEquals("G", page.getContent().get(0).getGuestName());
	}
}
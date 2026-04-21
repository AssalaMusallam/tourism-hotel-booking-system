package com.swer313.projectstep1.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swer313.projectstep1.availabilitypricing.pricing.PriceBreakdownDTO;
import com.swer313.projectstep1.availabilitypricing.pricing.PricingCalculator;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.room.BedType;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import com.swer313.projectstep1.notification.NotificationService;
import com.swer313.projectstep1.waitinglist.WaitingListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private WaitingListService waitingListService;

    @MockBean
    private PricingCalculator pricingCalculator;

    @MockBean
    private AvailabilityChecker availabilityChecker;

    @Test
    @WithMockUser(username = "jwtguest@test.com", roles = "GUEST")
    void createBooking_success() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setName("Integration Hotel");
        hotel.setCity("Nablus");
        hotel.setCountry("Palestine");
        hotel.setAddress("Main Street");
        hotel = hotelRepository.saveAndFlush(hotel);

        RoomType roomType = new RoomType();
        roomType.setHotel(hotel);
        roomType.setName("Standard Room");
        roomType.setCapacity(3);
        roomType.setBedType(BedType.QUEEN);
        roomType.setBedCount(1);
        roomType.setMaxAdults(2);
        roomType.setMaxChildren(1);
        roomType.setBasePrice(new BigDecimal("100.00"));
        roomType.setTotalUnits(10);
        roomType.setStatus(RoomTypeStatus.ACTIVE);
        roomType = roomTypeRepository.saveAndFlush(roomType);

        PriceBreakdownDTO breakdown = new PriceBreakdownDTO(
                new BigDecimal("100.00"),
                2,
                List.of(),
                new BigDecimal("200.00"),
                new BigDecimal("0.16"),
                new BigDecimal("32.00"),
                new BigDecimal("232.00")
        );

        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(7);

        when(availabilityChecker.isAvailable(
                eq(roomType.getId()),
                eq(checkIn),
                eq(checkOut)
        )).thenReturn(true);

        when(availabilityChecker.remainingUnits(
                eq(roomType.getId()),
                eq(checkIn),
                eq(checkOut)
        )).thenReturn(9L);

        when(pricingCalculator.calculateBreakdown(
                eq(new BigDecimal("100.00")),
                eq(checkIn),
                eq(checkOut)
        )).thenReturn(breakdown);

        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomTypeId(roomType.getId());
        request.setGuestName("Test Guest");
        request.setGuestEmail("bodyemail@test.com");
        request.setGuestPhone("+970599999999");
        request.setAdults(2);
        request.setChildren(0);
        request.setCheckIn(checkIn);
        request.setCheckOut(checkOut);
        request.setGuestNotes("Near elevator");

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guestEmail").value("jwtguest@test.com"))
                .andExpect(jsonPath("$.guestName").value("Test Guest"))
                .andExpect(jsonPath("$.roomTypeId").value(roomType.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(232.00));

        assertThat(bookingRepository.findAll()).hasSize(1);

        Booking saved = bookingRepository.findAll().get(0);
        assertThat(saved.getGuestEmail()).isEqualTo("jwtguest@test.com");
        assertThat(saved.getGuestName()).isEqualTo("Test Guest");
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(saved.getTotalPrice()).isEqualByComparingTo("232.00");
    }

    @Test
    @WithMockUser(username = "jwtguest@test.com", roles = "GUEST")
    void createBooking_invalidDateRange_returnsBadRequest() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setName("Integration Hotel");
        hotel.setCity("Nablus");
        hotel.setCountry("Palestine");
        hotel.setAddress("Main Street");
        hotel = hotelRepository.saveAndFlush(hotel);

        RoomType roomType = new RoomType();
        roomType.setHotel(hotel);
        roomType.setName("Standard Room");
        roomType.setCapacity(3);
        roomType.setBedType(BedType.QUEEN);
        roomType.setBedCount(1);
        roomType.setMaxAdults(2);
        roomType.setMaxChildren(1);
        roomType.setBasePrice(new BigDecimal("100.00"));
        roomType.setTotalUnits(10);
        roomType.setStatus(RoomTypeStatus.ACTIVE);
        roomType = roomTypeRepository.saveAndFlush(roomType);

        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomTypeId(roomType.getId());
        request.setGuestName("Test Guest");
        request.setGuestEmail("bodyemail@test.com");
        request.setGuestPhone("+970599999999");
        request.setAdults(2);
        request.setChildren(0);
        request.setCheckIn(LocalDate.now().plusDays(7));
        request.setCheckOut(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(bookingRepository.findAll()).isEmpty();
    }
}
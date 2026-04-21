package com.swer313.projectstep1.admin;

import com.swer313.projectstep1.errors.GlobalExceptionHandler;
import com.swer313.projectstep1.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminReportController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)

@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminReportService adminReportService;

    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private JwtService jwtService;

    private final RevenueReportDto sampleRevenue = new RevenueReportDto(
            1L,
            "Grand Hotel",
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31),
            new BigDecimal("1500.00"),
            "USD",
            12L
    );

    private final OccupancyReportDto sampleOccupancy = new OccupancyReportDto(
            1L,
            "2026-03",
            35,
            47.6
    );

    private final List<PopularRoomDto> samplePopularRooms = List.of(
            new PopularRoomDto("Standard", 20L),
            new PopularRoomDto("Deluxe", 10L)
    );

    @Test
    @DisplayName("GET /api/admin/reports/revenue - valid request returns 200 and correct JSON")
    void revenue_validRequest_returns200AndJson() throws Exception {
        given(adminReportService.getRevenueReport(
                eq(1L),
                eq(LocalDate.of(2026, 1, 1)),
                eq(LocalDate.of(2026, 1, 31))
        )).willReturn(sampleRevenue);

        mockMvc.perform(get("/api/admin/reports/revenue")
                        .param("hotelId", "1")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotelId", is(1)))
                .andExpect(jsonPath("$.hotelName", is("Grand Hotel")))
                .andExpect(jsonPath("$.from", is("2026-01-01")))
                .andExpect(jsonPath("$.to", is("2026-01-31")))
                .andExpect(jsonPath("$.totalRevenue", is(1500.00)))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.totalBookings", is(12)));

        verify(adminReportService).getRevenueReport(
                eq(1L),
                eq(LocalDate.of(2026, 1, 1)),
                eq(LocalDate.of(2026, 1, 31))
        );
    }

    @Test
    @DisplayName("GET /api/admin/reports/revenue - to not after from returns 400")
    void revenue_toNotAfterFrom_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/reports/revenue")
                        .param("hotelId", "1")
                        .param("from", "2026-01-31")
                        .param("to", "2026-01-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("'to' date must be after 'from' date.")));
    }

    @Test
    @DisplayName("GET /api/admin/reports/revenue - missing hotelId returns 400")
    void revenue_missingHotelId_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/reports/revenue")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/admin/reports/revenue - missing from returns 400")
    void revenue_missingFrom_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/reports/revenue")
                        .param("hotelId", "1")
                        .param("to", "2026-01-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/admin/reports/revenue - missing to returns 400")
    void revenue_missingTo_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/reports/revenue")
                        .param("hotelId", "1")
                        .param("from", "2026-01-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/admin/reports/occupancy - valid request returns 200 and correct JSON")
    void occupancy_validRequest_returns200AndJson() throws Exception {
        given(adminReportService.getOccupancyReport(eq(1L), eq("2026-03")))
                .willReturn(sampleOccupancy);

        mockMvc.perform(get("/api/admin/reports/occupancy")
                        .param("hotelId", "1")
                        .param("month", "2026-03")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotelId", is(1)))
                .andExpect(jsonPath("$.month", is("2026-03")))
                .andExpect(jsonPath("$.totalRooms", is(35)))
                .andExpect(jsonPath("$.occupancyRate", is(47.6)));

        verify(adminReportService).getOccupancyReport(eq(1L), eq("2026-03"));
    }

    @Test
    @DisplayName("GET /api/admin/reports/occupancy - invalid month format returns 400")
    void occupancy_invalidMonthFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/reports/occupancy")
                        .param("hotelId", "1")
                        .param("month", "03-2026")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid month format. Use YYYY-MM (e.g. 2026-03).")));
    }

    @Test
    @DisplayName("GET /api/admin/reports/occupancy - missing hotelId returns 400")
    void occupancy_missingHotelId_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/reports/occupancy")
                        .param("month", "2026-03")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/admin/reports/occupancy - missing month returns 400")
    void occupancy_missingMonth_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/reports/occupancy")
                        .param("hotelId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/admin/reports/popular-rooms - valid request returns 200 and JSON array")
    void popularRooms_validRequest_returns200AndJsonArray() throws Exception {
        given(adminReportService.getPopularRooms(eq(1L))).willReturn(samplePopularRooms);

        mockMvc.perform(get("/api/admin/reports/popular-rooms")
                        .param("hotelId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].roomTypeName", is("Standard")))
                .andExpect(jsonPath("$[0].bookingsCount", is(20)))
                .andExpect(jsonPath("$[1].roomTypeName", is("Deluxe")))
                .andExpect(jsonPath("$[1].bookingsCount", is(10)));

        verify(adminReportService).getPopularRooms(eq(1L));
    }

    @Test
    @DisplayName("GET /api/admin/reports/popular-rooms - missing hotelId returns 400")
    void popularRooms_missingHotelId_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/reports/popular-rooms")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
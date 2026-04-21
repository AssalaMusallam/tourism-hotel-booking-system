package com.swer313.projectstep1.availabilitypricing.availability;

import com.swer313.projectstep1.errors.GlobalExceptionHandler;
import com.swer313.projectstep1.security.JwtAuthFilter;
import com.swer313.projectstep1.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvailabilityController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AvailabilityControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvailabilityService availabilityService;

    // Security-related beans that may be required by the test context
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("checkAvailability_validRequest_returns200_andJson")
    void checkAvailability_validRequest_returns200_andJson() throws Exception {
        AvailabilityResponseDto dto = new AvailabilityResponseDto(
                1L, "Hotel", 2L, "Room", LocalDate.of(2026,4,10), LocalDate.of(2026,4,12),
                2, 2, 3, 5, 1, 4, true, new BigDecimal("100.00"), null
        );

        when(availabilityService.checkAvailability(eq(2L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(dto);

        mockMvc.perform(get("/api/v1/availability")
                        .param("roomTypeId", "2")
                        .param("checkIn", "2026-04-10")
                        .param("checkOut", "2026-04-12")
                        .param("guests", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotelId", is(1)))
                .andExpect(jsonPath("$.roomTypeId", is(2)))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    @DisplayName("checkAvailability_missingParams_returns400")
    void checkAvailability_missingParams_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/availability")
                        .param("roomTypeId", "2")
                        // missing checkIn and checkOut
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(availabilityService);
    }

    @Test
    @DisplayName("checkHotelAvailability_invalidPageSizeSort_throwsIllegalArgument")
    void checkHotelAvailability_invalidPageSizeSort_throwsIllegalArgument() throws Exception {
        // invalid page
        mockMvc.perform(get("/api/v1/hotels/1/availability")
                        .param("checkIn", "2026-04-10")
                        .param("checkOut", "2026-04-12")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());

        // invalid size
        mockMvc.perform(get("/api/v1/hotels/1/availability")
                        .param("checkIn", "2026-04-10")
                        .param("checkOut", "2026-04-12")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        // invalid sort field
        mockMvc.perform(get("/api/v1/hotels/1/availability")
                        .param("checkIn", "2026-04-10")
                        .param("checkOut", "2026-04-12")
                        .param("sort", "unknown,asc"))
                .andExpect(status().isBadRequest());

        // invalid sort direction
        mockMvc.perform(get("/api/v1/hotels/1/availability")
                        .param("checkIn", "2026-04-10")
                        .param("checkOut", "2026-04-12")
                        .param("sort", "id,invalid"))
                .andExpect(status().isBadRequest());
    }
}



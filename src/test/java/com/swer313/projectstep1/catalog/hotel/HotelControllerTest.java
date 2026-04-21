package com.swer313.projectstep1.catalog.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swer313.projectstep1.security.HotelAccessSecurityService;
import com.swer313.projectstep1.security.JwtAuthFilter;
import com.swer313.projectstep1.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HotelService hotelService;

    @MockBean(name = "hotelAccessSecurityService")
    private HotelAccessSecurityService hotelAccessSecurityService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void search_returnsPagedHotels() throws Exception {
        HotelResponseDto dto = new HotelResponseDto();
        dto.setId(1L);
        dto.setName("Test Hotel");

        when(hotelService.search(
                anyString(),
                any(),
                any(),
                eq(Hotel.Status.ACTIVE),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/hotels").param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Hotel"));
    }

    @Test
    void getById_returnsHotel() throws Exception {
        HotelResponseDto dto = new HotelResponseDto();
        dto.setId(2L);
        dto.setName("H2");

        when(hotelService.getActiveById(2L)).thenReturn(dto);

        mockMvc.perform(get("/api/hotels/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("H2"));
    }

    @Test
    void autocomplete_invalidLimit_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/hotels/autocomplete")
                        .param("q", "x")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void adminGetById_forbidden_whenSecurityDenies() throws Exception {
        when(hotelAccessSecurityService.canAccessHotel(10L)).thenReturn(false);

        mockMvc.perform(get("/api/admin/hotels/10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_validationError_returnsBadRequest() throws Exception {
        String body = "{ \"name\": \"\" }";

        mockMvc.perform(post("/api/admin/hotels")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
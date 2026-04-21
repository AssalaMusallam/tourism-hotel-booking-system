package com.swer313.projectstep1.catalog.amenities;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AmenityController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AmenityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AmenityService amenityService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/amenities returns 200")
    void get_all_returns_200() throws Exception {
        AmenityResponseDTO dto = new AmenityResponseDTO(
                1L, "A", "d", Amenity.AmenityCategory.COMFORT,
                false, true, null, null
        );
        PagedResponse<AmenityResponseDTO> page =
                new PagedResponse<>(List.of(dto), 0, 10, 1, 1, true, true, false, false);

        when(amenityService.getAll(any(Pageable.class), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/amenities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("GET /api/amenities?page=-1 returns error")
    void get_all_invalid_page_returns_error() throws Exception {
        mockMvc.perform(get("/api/amenities").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("page must be >= 0")));

        verify(amenityService, never()).getAll(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/amenities?size=0 returns error")
    void get_all_invalid_size_zero_returns_error() throws Exception {
        mockMvc.perform(get("/api/amenities").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("size must be > 0")));

        verify(amenityService, never()).getAll(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/amenities?size=51 returns error")
    void get_all_size_too_large_returns_error() throws Exception {
        mockMvc.perform(get("/api/amenities").param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("size must be <=")));

        verify(amenityService, never()).getAll(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/amenities?sort=invalid,asc returns error")
    void get_all_invalid_sort_returns_error() throws Exception {
        mockMvc.perform(get("/api/amenities").param("sort", "invalid,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid sort field")));

        verify(amenityService, never()).getAll(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/amenities/{id} returns 200")
    void get_by_id_returns_200() throws Exception {
        AmenityResponseDTO dto = new AmenityResponseDTO(
                2L, "N", "d", Amenity.AmenityCategory.COMFORT,
                false, true, null, null
        );
        when(amenityService.getById(2L)).thenReturn(dto);

        mockMvc.perform(get("/api/amenities/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)));
    }

    @Test
    @DisplayName("POST /api/amenities valid body returns 201")
    void post_valid_returns_201() throws Exception {
        AmenityRequestDTO req = new AmenityRequestDTO(
                "Name", "A valid description text",
                Amenity.AmenityCategory.CLEANING, null, null
        );
        AmenityResponseDTO dto = new AmenityResponseDTO(
                5L, "Name", "A valid description text",
                Amenity.AmenityCategory.CLEANING, false, true, null, null
        );
        when(amenityService.create(any(AmenityRequestDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(5)));

        verify(amenityService).create(any(AmenityRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/amenities invalid body returns 400")
    void post_invalid_returns_400() throws Exception {
        mockMvc.perform(post("/api/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(amenityService, never()).create(any());
    }

    @Test
    @DisplayName("PUT /api/amenities/{id} valid body returns 200")
    void put_valid_returns_200() throws Exception {
        AmenityRequestDTO req = new AmenityRequestDTO(
                "Upd", "A valid description text",
                Amenity.AmenityCategory.CLEANING, null, null
        );
        AmenityResponseDTO dto = new AmenityResponseDTO(
                7L, "Upd", "A valid description text",
                Amenity.AmenityCategory.CLEANING, false, true, null, null
        );
        when(amenityService.update(eq(7L), any(AmenityRequestDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/amenities/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(7)));

        verify(amenityService).update(eq(7L), any(AmenityRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/amenities/{id} returns 204")
    void delete_returns_204() throws Exception {
        doNothing().when(amenityService).delete(9L);

        mockMvc.perform(delete("/api/amenities/9"))
                .andExpect(status().isNoContent());

        verify(amenityService).delete(9L);
    }

    @Test
    @DisplayName("DELETE /api/amenities/{id}/hard returns 204")
    void delete_hard_returns_204() throws Exception {
        doNothing().when(amenityService).hardDelete(10L);

        mockMvc.perform(delete("/api/amenities/10/hard"))
                .andExpect(status().isNoContent());

        verify(amenityService).hardDelete(10L);
    }

    @Test
    @DisplayName("PATCH /api/amenities/{id}/activate returns 200")
    void activate_returns_200() throws Exception {
        AmenityResponseDTO dto = new AmenityResponseDTO(
                3L, "A", "d", Amenity.AmenityCategory.COMFORT,
                false, true, null, null
        );
        when(amenityService.activate(3L)).thenReturn(dto);

        mockMvc.perform(patch("/api/amenities/3/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("PATCH /api/amenities/{id}/deactivate returns 200")
    void deactivate_returns_200() throws Exception {
        AmenityResponseDTO dto = new AmenityResponseDTO(
                4L, "A", "d", Amenity.AmenityCategory.COMFORT,
                false, false, null, null
        );
        when(amenityService.deactivate(4L)).thenReturn(dto);

        mockMvc.perform(patch("/api/amenities/4/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @DisplayName("PATCH /api/amenities/{id}/restore returns 200")
    void restore_returns_200() throws Exception {
        AmenityResponseDTO dto = new AmenityResponseDTO(
                6L, "A", "d", Amenity.AmenityCategory.COMFORT,
                false, true, null, null
        );
        when(amenityService.restore(6L)).thenReturn(dto);

        mockMvc.perform(patch("/api/amenities/6/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("GET /api/amenities/exists returns 200")
    void exists_returns_200() throws Exception {
        when(amenityService.exists("Wifi")).thenReturn(Map.of("name", "Wifi", "exists", true));

        mockMvc.perform(get("/api/amenities/exists").param("name", "Wifi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Wifi")))
                .andExpect(jsonPath("$.exists", is(true)));
    }

    @Test
    @DisplayName("GET /api/amenities/minimal returns 200")
    void minimal_returns_200() throws Exception {
        PagedResponse<AmenityMinimalDTO> page =
                new PagedResponse<>(List.of(new AmenityMinimalDTO(1L, "Wifi")), 0, 20, 1, 1, true, true, false, false);

        when(amenityService.minimal(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/amenities/minimal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("GET /api/amenities/suggest returns 200")
    void suggest_returns_200() throws Exception {
        when(amenityService.suggest("wi", null)).thenReturn(List.of(new AmenityMinimalDTO(1L, "Wifi")));

        mockMvc.perform(get("/api/amenities/suggest").param("q", "wi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Wifi")));
    }

    @Test
    @DisplayName("PATCH /api/amenities/status returns 200")
    void bulk_status_returns_200() throws Exception {
        AmenityBulkStatusRequest req = new AmenityBulkStatusRequest();
        req.setIds(List.of(1L, 2L));
        req.setActive(false);

        when(amenityService.bulkStatus(any(AmenityBulkStatusRequest.class)))
                .thenReturn(Map.of("active", false, "updatedIds", List.of(1L, 2L), "count", 2));

        mockMvc.perform(patch("/api/amenities/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(2)));
    }
}
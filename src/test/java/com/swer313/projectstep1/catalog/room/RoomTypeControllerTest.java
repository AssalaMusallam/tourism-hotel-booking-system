package com.swer313.projectstep1.catalog.room;

import com.swer313.projectstep1.security.HotelAccessSecurityService;
import com.swer313.projectstep1.security.JwtAuthFilter;
import com.swer313.projectstep1.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RoomTypeControllerTest.MethodSecurityConfig.class)
class RoomTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomTypeService service;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean(name = "hotelAccessSecurityService")
    private HotelAccessSecurityService hotelAccessSecurityService;

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    @Test
    @WithMockUser
    void getById_returnsOk() throws Exception {
        RoomTypeResponseDto dto = mock(RoomTypeResponseDto.class);
        when(dto.getId()).thenReturn(1L);
        when(service.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/room-types/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getStatus_returnsStatusMap() throws Exception {
        RoomTypeResponseDto dto = mock(RoomTypeResponseDto.class);
        when(dto.getStatus()).thenReturn(RoomTypeStatus.ACTIVE);
        when(service.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/room-types/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    void listAll_returnsOk() throws Exception {
        @SuppressWarnings("unchecked")
        PagedResponse<RoomTypeResponseDto> response = mock(PagedResponse.class);

        when(service.listAll(
                any(Pageable.class),
                eq(10L),
                eq("Deluxe"),
                eq(BedType.KING),
                eq(1),
                eq(3),
                eq(2),
                eq(5),
                eq(1),
                eq(4),
                eq(0),
                eq(2),
                eq(new BigDecimal("100")),
                eq(new BigDecimal("500")),
                eq("sea")
        )).thenReturn(response);

        mockMvc.perform(get("/api/room-types")
                        .param("hotelId", "10")
                        .param("name", "Deluxe")
                        .param("bedType", "KING")
                        .param("bedCountMin", "1")
                        .param("bedCountMax", "3")
                        .param("capacityMin", "2")
                        .param("capacityMax", "5")
                        .param("maxAdultsMin", "1")
                        .param("maxAdultsMax", "4")
                        .param("maxChildrenMin", "0")
                        .param("maxChildrenMax", "2")
                        .param("priceMin", "100")
                        .param("priceMax", "500")
                        .param("q", "sea")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "id")
                        .param("dir", "asc"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void listByHotel_returnsOk() throws Exception {
        @SuppressWarnings("unchecked")
        PagedResponse<RoomTypeResponseDto> response = mock(PagedResponse.class);

        when(service.listByHotel(
                eq(10L),
                any(Pageable.class),
                eq("Deluxe"),
                eq(BedType.KING),
                eq(1),
                eq(3),
                eq(2),
                eq(5),
                eq(1),
                eq(4),
                eq(0),
                eq(2),
                eq(new BigDecimal("100")),
                eq(new BigDecimal("500")),
                eq(RoomTypeStatus.ACTIVE),
                eq("sea")
        )).thenReturn(response);

        mockMvc.perform(get("/api/hotels/10/room-types")
                        .param("name", "Deluxe")
                        .param("bedType", "KING")
                        .param("bedCountMin", "1")
                        .param("bedCountMax", "3")
                        .param("capacityMin", "2")
                        .param("capacityMax", "5")
                        .param("maxAdultsMin", "1")
                        .param("maxAdultsMax", "4")
                        .param("maxChildrenMin", "0")
                        .param("maxChildrenMax", "2")
                        .param("priceMin", "100")
                        .param("priceMax", "500")
                        .param("status", "ACTIVE")
                        .param("q", "sea")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "id")
                        .param("dir", "asc"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void minimal_returnsOk() throws Exception {
        @SuppressWarnings("unchecked")
        PagedResponse<RoomTypeService.RoomTypeMinimalDto> response = mock(PagedResponse.class);

        when(service.minimal(10L, RoomTypeStatus.ACTIVE, 0, 50)).thenReturn(response);

        mockMvc.perform(get("/api/hotels/10/room-types/meta/minimal")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void suggest_returnsOk() throws Exception {
        when(service.suggest(10L, "del"))
                .thenReturn(List.of(new RoomTypeService.RoomTypeMinimalDto(1L, "Deluxe")));

        mockMvc.perform(get("/api/hotels/10/room-types/suggest")
                        .param("q", "del"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Deluxe"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void getByIdAdmin_returnsOk_forManager() throws Exception {
        RoomTypeResponseDto dto = mock(RoomTypeResponseDto.class);
        when(service.getByIdAdmin(10L, 1L)).thenReturn(dto);
        when(hotelAccessSecurityService.canAccessHotel(10L)).thenReturn(true);

        mockMvc.perform(get("/api/hotels/10/room-types/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void create_returnsCreated_forManager() throws Exception {
        RoomTypeResponseDto created = mock(RoomTypeResponseDto.class);
        when(created.getId()).thenReturn(5L);

        when(hotelAccessSecurityService.canAccessHotel(10L)).thenReturn(true);
        when(service.create(eq(10L), any(RoomTypeRequestDto.class))).thenReturn(created);

        String body = """
                {
                  "hotelId": 10,
                  "name": "Deluxe",
                  "capacity": 3,
                  "bedType": "KING",
                  "bedCount": 1,
                  "maxAdults": 2,
                  "maxChildren": 1,
                  "basePrice": 120.00,
                  "totalUnits": 5,
                  "description": "Nice room",
                  "policies": "No smoking",
                  "status": "ACTIVE",
                  "amenityIds": [1,2]
                }
                """;

        mockMvc.perform(post("/api/hotels/10/room-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void update_returnsOk_forManager() throws Exception {
        RoomTypeResponseDto updated = mock(RoomTypeResponseDto.class);

        when(hotelAccessSecurityService.canAccessHotel(10L)).thenReturn(true);
        when(service.update(eq(10L), eq(1L), any(RoomTypeRequestDto.class))).thenReturn(updated);

        String body = """
                {
                  "hotelId": 10,
                  "name": "Updated Deluxe",
                  "capacity": 4,
                  "bedType": "KING",
                  "bedCount": 2,
                  "maxAdults": 3,
                  "maxChildren": 1,
                  "basePrice": 150.00,
                  "totalUnits": 6,
                  "description": "Updated room",
                  "policies": "No smoking",
                  "status": "ACTIVE",
                  "amenityIds": [1,2]
                }
                """;

        mockMvc.perform(put("/api/hotels/10/room-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void delete_returnsNoContent_forManager() throws Exception {
        when(hotelAccessSecurityService.canAccessHotel(10L)).thenReturn(true);
        doNothing().when(service).delete(10L, 1L);

        mockMvc.perform(delete("/api/hotels/10/room-types/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void bulkStatus_returnsOk_forManager() throws Exception {
        RoomTypeService.BulkStatusResult result =
                new RoomTypeService.BulkStatusResult(List.of(1L, 2L), List.of(), RoomTypeStatus.INACTIVE);

        when(hotelAccessSecurityService.canAccessHotel(10L)).thenReturn(true);
        when(service.bulkStatus(eq(10L), eq(List.of(1L, 2L)), eq(RoomTypeStatus.INACTIVE))).thenReturn(result);

        String body = """
                {
                  "ids": [1,2],
                  "status": "INACTIVE"
                }
                """;

        mockMvc.perform(patch("/api/hotels/10/room-types/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedIds", hasSize(2)))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void changeStatus_returnsOk_forManager() throws Exception {
        RoomTypeResponseDto dto = mock(RoomTypeResponseDto.class);

        when(hotelAccessSecurityService.canAccessHotel(10L)).thenReturn(true);
        when(service.changeStatus(10L, 1L, RoomTypeStatus.INACTIVE)).thenReturn(dto);

        String body = """
                {
                  "status": "INACTIVE"
                }
                """;

        mockMvc.perform(patch("/api/hotels/10/room-types/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void replacePolicies_returnsOk_forManager() throws Exception {
        RoomTypeResponseDto dto = mock(RoomTypeResponseDto.class);

        when(hotelAccessSecurityService.canAccessHotel(10L)).thenReturn(true);
        when(service.replacePolicies(10L, 1L, "No pets")).thenReturn(dto);

        String body = """
                {
                  "policies": "No pets"
                }
                """;

        mockMvc.perform(put("/api/hotels/10/room-types/1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void replaceAmenities_returnsOk_forManager() throws Exception {
        RoomTypeResponseDto dto = mock(RoomTypeResponseDto.class);

        when(hotelAccessSecurityService.canAccessRoomType(1L)).thenReturn(true);
        when(service.replaceAmenities(eq(1L), anySet())).thenReturn(dto);

        String body = """
                {
                  "amenityIds": [1,2,3]
                }
                """;

        mockMvc.perform(put("/api/admin/room-types/1/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void addAmenity_returnsOk_forManager() throws Exception {
        RoomTypeResponseDto dto = mock(RoomTypeResponseDto.class);

        when(hotelAccessSecurityService.canAccessRoomType(1L)).thenReturn(true);
        when(service.addAmenity(1L, 2L)).thenReturn(dto);

        mockMvc.perform(patch("/api/admin/room-types/1/amenities/2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void removeAmenity_returnsOk_forManager() throws Exception {
        RoomTypeResponseDto dto = mock(RoomTypeResponseDto.class);

        when(hotelAccessSecurityService.canAccessRoomType(1L)).thenReturn(true);
        when(service.removeAmenity(1L, 2L)).thenReturn(dto);

        mockMvc.perform(delete("/api/admin/room-types/1/amenities/2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void uploadImages_returnsCreated_forManager() throws Exception {
        RoomTypeImageResponseDto image = new RoomTypeImageResponseDto();
        image.setId(1L);
        image.setImageUrl("http://localhost/img.jpg");
        image.setFileName("img.jpg");

        when(hotelAccessSecurityService.canAccessRoomType(1L)).thenReturn(true);
        when(service.uploadImages(eq(1L), any())).thenReturn(List.of(image));

        mockMvc.perform(multipart("/api/admin/room-types/1/images")
                        .file("files", "abc".getBytes()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fileName").value("img.jpg"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void getImages_returnsOk_forManager() throws Exception {
        RoomTypeImageResponseDto image = new RoomTypeImageResponseDto();
        image.setId(1L);
        image.setImageUrl("http://localhost/img.jpg");
        image.setFileName("img.jpg");

        when(hotelAccessSecurityService.canAccessRoomType(1L)).thenReturn(true);
        when(service.getRoomTypeImages(1L)).thenReturn(List.of(image));

        mockMvc.perform(get("/api/admin/room-types/1/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fileName").value("img.jpg"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void deleteImage_returnsNoContent_forManager() throws Exception {
        when(hotelAccessSecurityService.canAccessRoomType(1L)).thenReturn(true);
        doNothing().when(service).deleteRoomTypeImage(1L, 7L);

        mockMvc.perform(delete("/api/admin/room-types/1/images/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void listAll_buildsPageableCorrectly() throws Exception {
        @SuppressWarnings("unchecked")
        PagedResponse<RoomTypeResponseDto> response = mock(PagedResponse.class);

        when(service.listAll(
                argThat(pageable -> {
                    Sort.Order order = pageable.getSort().getOrderFor("id");
                    return pageable.getPageNumber() == 0
                            && pageable.getPageSize() == 20
                            && order != null
                            && order.getDirection() == Sort.Direction.ASC;
                }),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null)
        )).thenReturn(response);

        mockMvc.perform(get("/api/room-types")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "id")
                        .param("dir", "asc"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void listByHotel_buildsPageableCorrectly() throws Exception {
        @SuppressWarnings("unchecked")
        PagedResponse<RoomTypeResponseDto> response = mock(PagedResponse.class);

        when(service.listByHotel(
                eq(10L),
                argThat(pageable -> {
                    Sort.Order order = pageable.getSort().getOrderFor("id");
                    return pageable.getPageNumber() == 0
                            && pageable.getPageSize() == 20
                            && order != null
                            && order.getDirection() == Sort.Direction.ASC;
                }),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null)
        )).thenReturn(response);

        mockMvc.perform(get("/api/hotels/10/room-types")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "id")
                        .param("dir", "asc"))
                .andExpect(status().isOk());
    }
}
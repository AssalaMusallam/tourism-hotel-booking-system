package com.swer313.projectstep1.catalog.amenities;

import com.swer313.projectstep1.errors.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmenityServiceImplTest {

    @Mock
    private AmenityRepository repository;

    @Mock
    private AmenityMapper mapper;

    @InjectMocks
    private AmenityServiceImpl service;

    @Captor
    private ArgumentCaptor<Amenity> amenityCaptor;

    @Test
    @DisplayName("getAll_success_returns_mapped_paged_response")
    void getAll_success_returns_mapped_paged_response() {
        Amenity a = new Amenity();
        a.setId(1L);
        a.setName("N");

        List<Amenity> content = List.of(a);
        Page<Amenity> page = new PageImpl<>(content, PageRequest.of(0, 10), 1);

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        AmenityResponseDTO dto = new AmenityResponseDTO(
                1L, "N", "desc", Amenity.AmenityCategory.COMFORT,
                false, true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(mapper.toDto(a)).thenReturn(dto);

        PagedResponse<AmenityResponseDTO> res =
                service.getAll(PageRequest.of(0, 10), null, null, null, null, null, null, null, null);

        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertEquals("N", res.getContent().get(0).getName());
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
        verify(mapper).toDto(a);
    }

    @Test
    @DisplayName("getAll_invalid_created_range_throws_BadRequestException")
    void getAll_invalid_created_range_throws_BadRequestException() {
        LocalDateTime from = LocalDateTime.now().plusDays(1);
        LocalDateTime to = LocalDateTime.now();

        assertThrows(BadRequestException.class, () ->
                service.getAll(PageRequest.of(0, 10), null, null, null, null, null, null, from, to));

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("getById_found_returns_dto")
    void getById_found_returns_dto() {
        Amenity entity = new Amenity();
        entity.setId(5L);

        when(repository.findById(5L)).thenReturn(Optional.of(entity));

        AmenityResponseDTO dto = new AmenityResponseDTO(
                5L, "X", "d", Amenity.AmenityCategory.COMFORT,
                false, true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(mapper.toDto(entity)).thenReturn(dto);

        AmenityResponseDTO got = service.getById(5L);

        assertEquals(5L, got.getId());
        assertEquals("X", got.getName());
        verify(repository).findById(5L);
        verify(mapper).toDto(entity);
    }

    @Test
    @DisplayName("getById_not_found_throws_AmenityNotFoundException")
    void getById_not_found_throws_AmenityNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AmenityNotFoundException.class, () -> service.getById(99L));

        verify(repository).findById(99L);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("create_success_trims_name_saves_and_returns_dto")
    void create_success_trims_name_saves_and_returns_dto() {
        AmenityRequestDTO req = new AmenityRequestDTO();
        req.setName("  MyAmenity  ");
        req.setDescription("A long enough description");
        req.setCategory(Amenity.AmenityCategory.CLEANING);

        when(repository.existsByNameIgnoreCase("MyAmenity")).thenReturn(false);

        Amenity toSave = new Amenity();
        when(mapper.toEntity(req)).thenReturn(toSave);

        Amenity saved = new Amenity();
        saved.setId(10L);
        saved.setName("MyAmenity");
        when(repository.save(any(Amenity.class))).thenReturn(saved);

        AmenityResponseDTO dto = new AmenityResponseDTO(
                10L, "MyAmenity", "A long enough description",
                Amenity.AmenityCategory.CLEANING, false, true,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(mapper.toDto(saved)).thenReturn(dto);

        AmenityResponseDTO res = service.create(req);

        assertEquals(10L, res.getId());
        assertEquals("MyAmenity", res.getName());

        verify(repository).existsByNameIgnoreCase("MyAmenity");
        verify(mapper).toEntity(req);
        verify(repository).save(amenityCaptor.capture());

        Amenity captured = amenityCaptor.getValue();
        assertEquals("MyAmenity", captured.getName());

        verify(mapper).toDto(saved);
    }

    @Test
    @DisplayName("create_duplicate_name_throws_DuplicateAmenityException")
    void create_duplicate_name_throws_DuplicateAmenityException() {
        AmenityRequestDTO req = new AmenityRequestDTO();
        req.setName(" Dup ");
        req.setDescription("descdescdesc");
        req.setCategory(Amenity.AmenityCategory.OUTDOOR);

        when(repository.existsByNameIgnoreCase("Dup")).thenReturn(true);

        assertThrows(DuplicateAmenityException.class, () -> service.create(req));

        verify(repository).existsByNameIgnoreCase("Dup");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create_null_body_throws_BadRequestException")
    void create_null_body_throws_BadRequestException() {
        assertThrows(BadRequestException.class, () -> service.create(null));
        verifyNoInteractions(repository, mapper);
    }

    @Test
    @DisplayName("update_success_trims_name_updates_and_returns_dto")
    void update_success_trims_name_updates_and_returns_dto() {
        Long id = 2L;

        AmenityRequestDTO req = new AmenityRequestDTO();
        req.setName("  NewName  ");
        req.setDescription("new description for update");
        req.setCategory(Amenity.AmenityCategory.PARKING);

        Amenity existing = new Amenity();
        existing.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.existsByNameIgnoreCaseAndIdNot("NewName", id)).thenReturn(false);

        Amenity updated = new Amenity();
        updated.setId(id);
        updated.setName("NewName");

        when(repository.save(existing)).thenReturn(updated);

        AmenityResponseDTO dto = new AmenityResponseDTO(
                id, "NewName", "new description for update",
                Amenity.AmenityCategory.PARKING, false, true,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(mapper.toDto(updated)).thenReturn(dto);

        AmenityResponseDTO res = service.update(id, req);

        assertEquals("NewName", res.getName());
        verify(repository).findById(id);
        verify(repository).existsByNameIgnoreCaseAndIdNot("NewName", id);
        verify(mapper).updateEntity(existing, req);
        verify(repository).save(existing);
        verify(mapper).toDto(updated);
    }

    @Test
    @DisplayName("update_not_found_throws_AmenityNotFoundException")
    void update_not_found_throws_AmenityNotFoundException() {
        when(repository.findById(55L)).thenReturn(Optional.empty());

        AmenityRequestDTO req = new AmenityRequestDTO();
        req.setName("Name");
        req.setDescription("descdescdesc");
        req.setCategory(Amenity.AmenityCategory.DINING);

        assertThrows(AmenityNotFoundException.class, () -> service.update(55L, req));
        verify(repository).findById(55L);
    }

    @Test
    @DisplayName("update_duplicate_name_throws_DuplicateAmenityException")
    void update_duplicate_name_throws_DuplicateAmenityException() {
        Long id = 7L;

        AmenityRequestDTO req = new AmenityRequestDTO();
        req.setName("Dup");
        req.setDescription("descdescdesc");
        req.setCategory(Amenity.AmenityCategory.SECURITY);

        Amenity existing = new Amenity();
        existing.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.existsByNameIgnoreCaseAndIdNot("Dup", id)).thenReturn(true);

        assertThrows(DuplicateAmenityException.class, () -> service.update(id, req));

        verify(repository).findById(id);
        verify(repository).existsByNameIgnoreCaseAndIdNot("Dup", id);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("activate_success_sets_active_true")
    void activate_success_sets_active_true() {
        Amenity existing = new Amenity();
        existing.setId(3L);
        existing.setActive(false);

        when(repository.findById(3L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(new AmenityResponseDTO(
                3L, "n", "d", Amenity.AmenityCategory.COMFORT,
                false, true, LocalDateTime.now(), LocalDateTime.now()
        ));

        service.activate(3L);

        assertTrue(existing.isActive());
        verify(repository).save(existing);
        verify(mapper).toDto(existing);
    }

    @Test
    @DisplayName("deactivate_success_sets_active_false")
    void deactivate_success_sets_active_false() {
        Amenity existing = new Amenity();
        existing.setId(4L);
        existing.setActive(true);

        when(repository.findById(4L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(new AmenityResponseDTO(
                4L, "n", "d", Amenity.AmenityCategory.COMFORT,
                false, false, LocalDateTime.now(), LocalDateTime.now()
        ));

        service.deactivate(4L);

        assertFalse(existing.isActive());
        verify(repository).save(existing);
        verify(mapper).toDto(existing);
    }

    @Test
    @DisplayName("restore_success_sets_active_true")
    void restore_success_sets_active_true() {
        Amenity existing = new Amenity();
        existing.setId(6L);
        existing.setActive(false);

        when(repository.findById(6L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(new AmenityResponseDTO(
                6L, "n", "d", Amenity.AmenityCategory.COMFORT,
                false, true, LocalDateTime.now(), LocalDateTime.now()
        ));

        service.restore(6L);

        assertTrue(existing.isActive());
        verify(repository).save(existing);
        verify(mapper).toDto(existing);
    }

    @Test
    @DisplayName("delete_success_soft_deletes_by_setting_active_false")
    void delete_success_soft_deletes_by_setting_active_false() {
        Amenity existing = new Amenity();
        existing.setId(8L);
        existing.setActive(true);

        when(repository.findById(8L)).thenReturn(Optional.of(existing));

        service.delete(8L);

        assertFalse(existing.isActive());
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("hardDelete_success_removes_relationships_and_deletes_entity")
    void hardDelete_success_removes_relationships_and_deletes_entity() {
        Amenity existing = new Amenity();
        existing.setId(20L);

        com.swer313.projectstep1.catalog.room.RoomType rt =
                new com.swer313.projectstep1.catalog.room.RoomType();
        rt.setId(100L);
        rt.getAmenities().add(existing);
        existing.getRoomTypes().add(rt);

        when(repository.findById(20L)).thenReturn(Optional.of(existing));
        doNothing().when(repository).delete(existing);

        service.hardDelete(20L);

        assertTrue(existing.getRoomTypes().isEmpty());
        assertFalse(rt.getAmenities().contains(existing));
        verify(repository).delete(existing);
    }

    @Test
    @DisplayName("suggest_blank_q_returns_empty_list_and_does_not_hit_repository")
    void suggest_blank_q_returns_empty_list_and_does_not_hit_repository() {
        List<AmenityMinimalDTO> result = service.suggest("   ", true);

        assertTrue(result.isEmpty());
        verify(repository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("suggest_success_returns_minimal_dtos")
    void suggest_success_returns_minimal_dtos() {
        Amenity a1 = new Amenity();
        a1.setId(1L);
        a1.setName("A1");

        Page<Amenity> page = new PageImpl<>(List.of(a1));
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        List<AmenityMinimalDTO> res = service.suggest("a", true);

        assertEquals(1, res.size());
        assertEquals("A1", res.get(0).getName());
    }

    @Test
    @DisplayName("minimal_success_returns_paged_minimal_dtos")
    void minimal_success_returns_paged_minimal_dtos() {
        Amenity a1 = new Amenity();
        a1.setId(2L);
        a1.setName("Min");

        Page<Amenity> page = new PageImpl<>(List.of(a1), PageRequest.of(0, 20), 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PagedResponse<AmenityMinimalDTO> res = service.minimal(true, PageRequest.of(0, 20));

        assertEquals(1, res.getTotalElements());
        assertEquals("Min", res.getContent().get(0).getName());
    }

    @Test
    @DisplayName("exists_blank_name_throws_BadRequestException")
    void exists_blank_name_throws_BadRequestException() {
        assertThrows(BadRequestException.class, () -> service.exists(null));
        assertThrows(BadRequestException.class, () -> service.exists("   "));
    }

    @Test
    @DisplayName("exists_success_trims_name_and_returns_map")
    void exists_success_trims_name_and_returns_map() {
        when(repository.existsByNameIgnoreCase("Trimmed")).thenReturn(true);

        Map<String, Object> res = service.exists("  Trimmed  ");

        assertEquals("Trimmed", res.get("name"));
        assertTrue((Boolean) res.get("exists"));
        verify(repository).existsByNameIgnoreCase("Trimmed");
    }

    @Test
    @DisplayName("bulkStatus_null_body_throws_BadRequestException")
    void bulkStatus_null_body_throws_BadRequestException() {
        assertThrows(BadRequestException.class, () -> service.bulkStatus(null));
    }

    @Test
    @DisplayName("bulkStatus_empty_ids_throws_BadRequestException")
    void bulkStatus_empty_ids_throws_BadRequestException() {
        AmenityBulkStatusRequest req = new AmenityBulkStatusRequest();
        req.setIds(Collections.emptyList());
        req.setActive(true);

        assertThrows(BadRequestException.class, () -> service.bulkStatus(req));
    }

    @Test
    @DisplayName("bulkStatus_null_active_throws_BadRequestException")
    void bulkStatus_null_active_throws_BadRequestException() {
        AmenityBulkStatusRequest req = new AmenityBulkStatusRequest();
        req.setIds(List.of(1L));
        req.setActive(null);

        assertThrows(BadRequestException.class, () -> service.bulkStatus(req));
    }

    @Test
    @DisplayName("bulkStatus_success_updates_all_ids_and_returns_summary")
    @SuppressWarnings("unchecked")
    void bulkStatus_success_updates_all_ids_and_returns_summary() {
        AmenityBulkStatusRequest req = new AmenityBulkStatusRequest();
        req.setIds(List.of(11L, 12L));
        req.setActive(false);

        Amenity a1 = new Amenity();
        a1.setId(11L);
        a1.setActive(true);

        Amenity a2 = new Amenity();
        a2.setId(12L);
        a2.setActive(true);

        when(repository.findById(11L)).thenReturn(Optional.of(a1));
        when(repository.findById(12L)).thenReturn(Optional.of(a2));
        when(repository.save(any(Amenity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> out = service.bulkStatus(req);

        assertEquals(false, out.get("active"));
        assertTrue(((List<Long>) out.get("updatedIds")).containsAll(List.of(11L, 12L)));
        assertEquals(2, out.get("count"));

        verify(repository).findById(11L);
        verify(repository).findById(12L);
        verify(repository, times(2)).save(any(Amenity.class));
    }
}
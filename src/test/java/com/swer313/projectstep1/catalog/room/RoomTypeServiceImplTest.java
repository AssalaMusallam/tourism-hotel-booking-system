package com.swer313.projectstep1.catalog.room;

import com.swer313.projectstep1.catalog.amenities.Amenity;
import com.swer313.projectstep1.catalog.amenities.AmenityRepository;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.files.FileStorageService;
import com.swer313.projectstep1.files.StoredFileResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomTypeServiceImplTest {

    @Mock
    private RoomTypeRepository repository;

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private RoomTypeImageRepository roomTypeImageRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private RoomTypeServiceImpl service;

    private RoomType roomType;
    private RoomTypeRequestDto dto;

    @BeforeEach
    void setUp() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);

        roomType = new RoomType();
        roomType.setId(10L);
        roomType.setHotel(hotel);
        roomType.setName("Deluxe");
        roomType.setCapacity(4);
        roomType.setBedType(BedType.KING);
        roomType.setBedCount(1);
        roomType.setMaxAdults(2);
        roomType.setMaxChildren(2);
        roomType.setBasePrice(new BigDecimal("120.00"));
        roomType.setTotalUnits(5);
        roomType.setDescription("Nice room");
        roomType.setPolicies("No smoking");
        roomType.setStatus(RoomTypeStatus.ACTIVE);
        roomType.setAmenities(new HashSet<>());

        dto = new RoomTypeRequestDto();
        dto.setHotelId(1L);
        dto.setName(" Deluxe ");
        dto.setCapacity(4);
        dto.setBedType(BedType.KING);
        dto.setBedCount(1);
        dto.setMaxAdults(2);
        dto.setMaxChildren(2);
        dto.setBasePrice(new BigDecimal("120.00"));
        dto.setTotalUnits(5);
        dto.setDescription("Nice room");
        dto.setPolicies("No smoking");
        dto.setStatus(RoomTypeStatus.ACTIVE);
        dto.setAmenityIds(new HashSet<>());
    }

    @Test
    void getById_returnsDto_whenActive() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));

        RoomTypeResponseDto result = service.getById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Deluxe", result.getName());
        assertEquals(RoomTypeStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getById_throws_whenNotFound() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RoomTypeNotFoundException.class, () -> service.getById(10L));
    }

    @Test
    void getById_throws_whenInactive() {
        roomType.setStatus(RoomTypeStatus.INACTIVE);
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));

        assertThrows(RoomTypeNotFoundException.class, () -> service.getById(10L));
    }

    @Test
    void getByIdAdmin_returnsDto_whenHotelMatches() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));

        RoomTypeResponseDto result = service.getByIdAdmin(1L, 10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(1L, result.getHotelId());
    }

    @Test
    void getByIdAdmin_throws_whenHotelMismatch() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));

        assertThrows(RoomTypeNotFoundException.class, () -> service.getByIdAdmin(999L, 10L));
    }

    @Test
    void create_savesEntity_andReturnsDto() {
        Amenity amenity = new Amenity();
        amenity.setId(100L);
        dto.setAmenityIds(Set.of(100L));

        when(repository.existsByHotel_IdAndNameIgnoreCase(1L, "Deluxe")).thenReturn(false);
        when(amenityRepository.findAllById(Set.of(100L))).thenReturn(List.of(amenity));
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> {
            RoomType saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        RoomTypeResponseDto result = service.create(1L, dto);

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals(1L, result.getHotelId());
        assertEquals("Deluxe", result.getName());
        assertEquals(RoomTypeStatus.ACTIVE, result.getStatus());

        ArgumentCaptor<RoomType> captor = ArgumentCaptor.forClass(RoomType.class);
        verify(repository).save(captor.capture());
        assertEquals("Deluxe", captor.getValue().getName());
        assertEquals(1, captor.getValue().getAmenities().size());
    }

    @Test
    void create_setsActiveStatus_whenDtoStatusIsNull() {
        dto.setStatus(null);

        when(repository.existsByHotel_IdAndNameIgnoreCase(1L, "Deluxe")).thenReturn(false);
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> {
            RoomType saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        RoomTypeResponseDto result = service.create(1L, dto);

        assertEquals(RoomTypeStatus.ACTIVE, result.getStatus());
    }

    @Test
    void create_throws_whenDuplicateNameExists() {
        when(repository.existsByHotel_IdAndNameIgnoreCase(1L, "Deluxe")).thenReturn(true);

        assertThrows(DuplicateRoomTypeException.class, () -> service.create(1L, dto));

        verify(repository, never()).save(any());
    }

    @Test
    void create_throws_whenAmenityReferenceInvalid() {
        dto.setAmenityIds(Set.of(100L, 200L));
        Amenity amenity = new Amenity();
        amenity.setId(100L);

        when(repository.existsByHotel_IdAndNameIgnoreCase(1L, "Deluxe")).thenReturn(false);
        when(amenityRepository.findAllById(Set.of(100L, 200L))).thenReturn(List.of(amenity));

        assertThrows(InvalidAmenityReferenceException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_throws_whenRequestBodyNull() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.create(1L, null));

        assertEquals("Request body is required.", ex.getMessage());
    }

    @Test
    void create_throws_whenNameMissing() {
        dto.setName(" ");

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.create(1L, dto));

        assertEquals("name is required.", ex.getMessage());
    }

    @Test
    void create_throws_whenCapacityInvalid() {
        dto.setCapacity(0);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.create(1L, dto));

        assertEquals("capacity must be >= 1.", ex.getMessage());
    }

    @Test
    void create_throws_whenCapacityLessThanGuests() {
        dto.setCapacity(2);
        dto.setMaxAdults(2);
        dto.setMaxChildren(2);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.create(1L, dto));

        assertEquals("capacity must be >= (maxAdults + maxChildren).", ex.getMessage());
    }

    @Test
    void update_updatesExistingEntity() {
        Amenity amenity = new Amenity();
        amenity.setId(7L);
        dto.setAmenityIds(Set.of(7L));

        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(repository.existsByHotel_IdAndNameIgnoreCaseAndIdNot(1L, "Deluxe", 10L)).thenReturn(false);
        when(amenityRepository.findAllById(Set.of(7L))).thenReturn(List.of(amenity));
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomTypeResponseDto result = service.update(1L, 10L, dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Deluxe", result.getName());
        verify(repository).save(roomType);
        assertEquals(1, roomType.getAmenities().size());
    }

    @Test
    void update_throws_whenRoomTypeNotFound() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RoomTypeNotFoundException.class, () -> service.update(1L, 10L, dto));
    }

    @Test
    void update_throws_whenHotelOwnershipMismatch() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));

        assertThrows(RoomTypeNotFoundException.class, () -> service.update(999L, 10L, dto));
    }

    @Test
    void update_throws_whenDuplicateNameExists() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(repository.existsByHotel_IdAndNameIgnoreCaseAndIdNot(1L, "Deluxe", 10L)).thenReturn(true);

        assertThrows(DuplicateRoomTypeException.class, () -> service.update(1L, 10L, dto));
    }

    @Test
    void delete_setsStatusInactive() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));

        service.delete(1L, 10L);

        assertEquals(RoomTypeStatus.INACTIVE, roomType.getStatus());
        verify(repository).save(roomType);
    }

    @Test
    void delete_throws_whenHotelOwnershipMismatch() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));

        assertThrows(RoomTypeNotFoundException.class, () -> service.delete(999L, 10L));
    }

    @Test
    void changeStatus_updatesStatus() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomTypeResponseDto result = service.changeStatus(1L, 10L, RoomTypeStatus.INACTIVE);

        assertEquals(RoomTypeStatus.INACTIVE, result.getStatus());
        assertEquals(RoomTypeStatus.INACTIVE, roomType.getStatus());
    }

    @Test
    void changeStatus_throws_whenStatusNull() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.changeStatus(1L, 10L, null));

        assertEquals("status is required.", ex.getMessage());
    }

    @Test
    void clone_createsCopyWithCopySuffix() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(repository.existsByHotel_IdAndNameIgnoreCase(1L, "Deluxe Copy")).thenReturn(false);
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> {
            RoomType saved = invocation.getArgument(0);
            saved.setId(55L);
            return saved;
        });

        RoomTypeResponseDto result = service.clone(1L, 10L);

        assertNotNull(result);
        assertEquals(55L, result.getId());
        assertEquals("Deluxe Copy", result.getName());
    }

    @Test
    void clone_appendsTimestamp_whenCopyNameAlreadyExists() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(repository.existsByHotel_IdAndNameIgnoreCase(1L, "Deluxe Copy")).thenReturn(true);
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> {
            RoomType saved = invocation.getArgument(0);
            saved.setId(56L);
            return saved;
        });

        RoomTypeResponseDto result = service.clone(1L, 10L);

        assertNotNull(result);
        assertEquals(56L, result.getId());
        assertTrue(result.getName().startsWith("Deluxe Copy"));
        assertNotEquals("Deluxe Copy", result.getName());
    }

    @Test
    void replacePolicies_updatesPolicies() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomTypeResponseDto result = service.replacePolicies(1L, 10L, "Updated policies");

        assertEquals("Updated policies", result.getPolicies());
        assertEquals("Updated policies", roomType.getPolicies());
    }

    @Test
    void replacePolicies_throws_whenHotelMismatch() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));

        assertThrows(RoomTypeNotFoundException.class, () -> service.replacePolicies(999L, 10L, "x"));
    }

    @Test
    void bulkStatus_updatesExistingAndCollectsMissingIds() {
        RoomType second = new RoomType();
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        second.setHotel(hotel);
        second.setId(11L);
        second.setName("Second");
        second.setStatus(RoomTypeStatus.ACTIVE);
        second.setAmenities(new HashSet<>());

        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(repository.findById(11L)).thenReturn(Optional.of(second));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RoomTypeService.BulkStatusResult result =
                service.bulkStatus(1L, List.of(10L, 11L, 99L), RoomTypeStatus.INACTIVE);

        assertEquals(List.of(10L, 11L), result.updatedIds());
        assertEquals(List.of(99L), result.notFoundIds());
        assertEquals(RoomTypeStatus.INACTIVE, result.status());
        verify(repository, times(2)).save(any(RoomType.class));
    }

    @Test
    void bulkStatus_throws_whenIdsMissing() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> service.bulkStatus(1L, Collections.emptyList(), RoomTypeStatus.ACTIVE));

        assertEquals("ids are required.", ex.getMessage());
    }

    @Test
    void bulkStatus_throws_whenStatusMissing() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> service.bulkStatus(1L, List.of(10L), null));

        assertEquals("status is required.", ex.getMessage());
    }

    @Test
    void listAll_returnsPagedResponse() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<RoomType> page = new PageImpl<>(List.of(roomType), pageable, 1);

        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<RoomType>>any(), eq(pageable)))
                .thenReturn(page);

        PagedResponse<RoomTypeResponseDto> result = service.listAll(
                pageable, 1L, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Deluxe", result.getContent().get(0).getName());
    }

    @Test
    void listByHotel_returnsPagedResponse() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<RoomType> page = new PageImpl<>(List.of(roomType), pageable, 1);

        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<RoomType>>any(), eq(pageable)))
                .thenReturn(page);

        PagedResponse<RoomTypeResponseDto> result = service.listByHotel(
                1L, pageable, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, RoomTypeStatus.ACTIVE, null
        );

        assertEquals(1, result.getContent().size());
        assertEquals("Deluxe", result.getContent().get(0).getName());
    }

    @Test
    void listByHotel_throws_whenInvalidRange() {
        Pageable pageable = PageRequest.of(0, 10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.listByHotel(
                        1L, pageable, null, null,
                        10, 5, null, null,
                        null, null, null, null,
                        null, null, RoomTypeStatus.ACTIVE, null
                ));

        assertEquals("bedCountMin must be <= bedCountMax", ex.getMessage());
    }

    @Test
    void listByHotel_throws_whenInvalidPriceRange() {
        Pageable pageable = PageRequest.of(0, 10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.listByHotel(
                        1L, pageable, null, null,
                        null, null, null, null,
                        null, null, null, null,
                        new BigDecimal("300.00"), new BigDecimal("100.00"),
                        RoomTypeStatus.ACTIVE, null
                ));

        assertEquals("priceMin must be <= priceMax", ex.getMessage());
    }

    @Test
    void minimal_mapsToMinimalResponse() {
        PageRequest pageable = PageRequest.of(0, 50);
        Page<RoomType> page = new PageImpl<>(List.of(roomType), pageable, 1);

        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<RoomType>>any(), any(Pageable.class)))
                .thenReturn(page);

        PagedResponse<RoomTypeService.RoomTypeMinimalDto> result =
                service.minimal(1L, RoomTypeStatus.ACTIVE, 0, 50);

        assertEquals(1, result.getContent().size());
        assertEquals(10L, result.getContent().get(0).id());
        assertEquals("Deluxe", result.getContent().get(0).name());
    }

    @Test
    void suggest_returnsMinimalList() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<RoomType> page = new PageImpl<>(List.of(roomType), pageable, 1);

        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<RoomType>>any(), any(Pageable.class)))
                .thenReturn(page);

        List<RoomTypeService.RoomTypeMinimalDto> result = service.suggest(1L, "del");

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals("Deluxe", result.get(0).name());
    }

    @Test
    void suggest_throws_whenQueryBlank() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.suggest(1L, " "));

        assertEquals("q is required.", ex.getMessage());
    }

    @Test
    void exists_returnsRepositoryValue() {
        when(repository.existsById(10L)).thenReturn(true);

        assertTrue(service.exists(10L));
        verify(repository).existsById(10L);
    }

    @Test
    void replaceAmenities_replacesAmenitiesAndSaves() {
        Amenity a1 = new Amenity();
        a1.setId(1L);
        Amenity a2 = new Amenity();
        a2.setId(2L);

        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(amenityRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(a1, a2));
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomTypeResponseDto result = service.replaceAmenities(10L, Set.of(1L, 2L));

        assertEquals(2, result.getAmenityIds().size());
        assertTrue(result.getAmenityIds().containsAll(Set.of(1L, 2L)));
    }

    @Test
    void addAmenity_addsAmenityAndSaves() {
        Amenity amenity = new Amenity();
        amenity.setId(99L);

        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(amenityRepository.findById(99L)).thenReturn(Optional.of(amenity));
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomTypeResponseDto result = service.addAmenity(10L, 99L);

        assertTrue(result.getAmenityIds().contains(99L));
    }

    @Test
    void addAmenity_throws_whenAmenityInvalid() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(amenityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InvalidAmenityReferenceException.class, () -> service.addAmenity(10L, 99L));
    }

    @Test
    void removeAmenity_removesAmenityAndSaves() {
        Amenity amenity = new Amenity();
        amenity.setId(99L);
        roomType.setAmenities(new HashSet<>(Set.of(amenity)));

        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomTypeResponseDto result = service.removeAmenity(10L, 99L);

        assertFalse(result.getAmenityIds().contains(99L));
    }

    @Test
    void uploadImages_storesFilesAndReturnsImageResponses() {
        MockMultipartFile f1 = new MockMultipartFile("files", "a.jpg", "image/jpeg", "x".getBytes());
        MockMultipartFile f2 = new MockMultipartFile("files", "b.jpg", "image/jpeg", "y".getBytes());

        when(repository.findById(10L)).thenReturn(Optional.of(roomType));

        StoredFileResult r1 = mock(StoredFileResult.class);
        when(r1.getFileUrl()).thenReturn("http://cdn/a.jpg");
        when(r1.getFileName()).thenReturn("a.jpg");

        StoredFileResult r2 = mock(StoredFileResult.class);
        when(r2.getFileUrl()).thenReturn("http://cdn/b.jpg");
        when(r2.getFileName()).thenReturn("b.jpg");

        when(fileStorageService.storeRoomTypeImage(f1)).thenReturn(r1);
        when(fileStorageService.storeRoomTypeImage(f2)).thenReturn(r2);

        when(roomTypeImageRepository.save(any(RoomTypeImage.class))).thenAnswer(invocation -> {
            RoomTypeImage image = invocation.getArgument(0);
            if ("a.jpg".equals(image.getFileName())) {
                setRoomTypeImageId(image, 1L);
            } else {
                setRoomTypeImageId(image, 2L);
            }
            return image;
        });

        List<RoomTypeImageResponseDto> result = service.uploadImages(10L, List.of(f1, f2));

        assertEquals(2, result.size());
        assertEquals("http://cdn/a.jpg", result.get(0).getImageUrl());
        assertEquals("b.jpg", result.get(1).getFileName());
        verify(roomTypeImageRepository, times(2)).save(any(RoomTypeImage.class));
    }

    @Test
    void getRoomTypeImages_returnsMappedResponses() {
        RoomTypeImage image = new RoomTypeImage();
        image.setImageUrl("http://cdn/a.jpg");
        image.setFileName("a.jpg");
        image.setRoomType(roomType);
        setRoomTypeImageId(image, 7L);

        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(roomTypeImageRepository.findByRoomTypeId(10L)).thenReturn(List.of(image));

        List<RoomTypeImageResponseDto> result = service.getRoomTypeImages(10L);

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).getId());
        assertEquals("a.jpg", result.get(0).getFileName());
    }

    @Test
    void deleteRoomTypeImage_deletesStoredFileAndRecord() {
        RoomTypeImage image = new RoomTypeImage();
        image.setImageUrl("http://cdn/a.jpg");
        image.setFileName("a.jpg");
        image.setRoomType(roomType);
        setRoomTypeImageId(image, 9L);

        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(roomTypeImageRepository.findById(9L)).thenReturn(Optional.of(image));

        service.deleteRoomTypeImage(10L, 9L);

        verify(fileStorageService).deleteFileByUrl("http://cdn/a.jpg");
        verify(roomTypeImageRepository).delete(image);
    }

    @Test
    void deleteRoomTypeImage_throws_whenImageNotFound() {
        when(repository.findById(10L)).thenReturn(Optional.of(roomType));
        when(roomTypeImageRepository.findById(9L)).thenReturn(Optional.empty());

        RuntimeException ex =
                assertThrows(RuntimeException.class, () -> service.deleteRoomTypeImage(10L, 9L));

        assertEquals("Image not found: 9", ex.getMessage());
    }

    private void setRoomTypeImageId(RoomTypeImage image, Long id) {
        try {
            var field = RoomTypeImage.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(image, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set RoomTypeImage id in test", e);
        }
    }
}
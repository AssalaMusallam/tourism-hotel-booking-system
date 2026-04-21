package com.swer313.projectstep1.catalog.hotel;

import com.swer313.projectstep1.catalog.amenities.Amenity;
import com.swer313.projectstep1.catalog.amenities.AmenityRepository;
import com.swer313.projectstep1.files.FileStorageService;
import com.swer313.projectstep1.files.StoredFileResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

	@Mock private HotelRepository hotelRepository;
	@Mock private AmenityRepository amenityRepository;
	@Mock private HotelImageRepository hotelImageRepository;
	@Mock private FileStorageService fileStorageService;

	@InjectMocks private HotelServiceImpl service;

	@Captor private ArgumentCaptor<Hotel> hotelCaptor;
	@Captor private ArgumentCaptor<HotelImage> hotelImageCaptor;

	@Test
	void autocompleteNames_qBlank_returnsEmptyList() {
		List<String> result = service.autocompleteNames("   ", 10);
		assertTrue(result.isEmpty());
		verifyNoInteractions(hotelRepository);
	}

	@Test
	void autocompleteNames_limitBounds_usesSafeLimitAndTrim() {
		when(hotelRepository.findActiveNameContaining(eq("hotel"), eq(Hotel.Status.ACTIVE), any()))
				.thenReturn(List.of("hotel A"));

		List<String> result = service.autocompleteNames("  hotel  ", 1000);

		assertEquals(1, result.size());
		ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
				ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
		verify(hotelRepository).findActiveNameContaining(eq("hotel"), eq(Hotel.Status.ACTIVE), captor.capture());
		assertEquals(50, captor.getValue().getPageSize());
	}

	@Test
	void create_validDto_savesHotelWithNormalizedNameAndAmenities() {
		HotelRequestDto dto = new HotelRequestDto();
		dto.setName("  My Hotel  ");
		dto.setAddress("addr");
		dto.setCity("C");
		dto.setCountry("X");
		dto.setCheckInTime(LocalTime.of(10, 0));
		dto.setCheckOutTime(LocalTime.of(12, 0));
		dto.setAmenityIds(Set.of(1L, 2L));
		dto.setStatus(Hotel.Status.INACTIVE);

		when(hotelRepository.existsByNameIgnoreCase("My Hotel")).thenReturn(false);

		Amenity a1 = new Amenity();
		a1.setId(1L);
		a1.setName("A");

		Amenity a2 = new Amenity();
		a2.setId(2L);
		a2.setName("B");

		when(amenityRepository.findAllById(dto.getAmenityIds())).thenReturn(asList(a1, a2));

		Hotel saved = new Hotel();
		saved.setId(10L);
		saved.setName("My Hotel");
		saved.setStatus(Hotel.Status.INACTIVE);

		when(hotelRepository.save(any())).thenReturn(saved);

		var resp = service.create(dto);

		assertNotNull(resp);
		assertEquals(10L, resp.getId());
		verify(hotelRepository).existsByNameIgnoreCase("My Hotel");
		verify(amenityRepository).findAllById(dto.getAmenityIds());
		verify(hotelRepository).save(hotelCaptor.capture());
		assertEquals("My Hotel", hotelCaptor.getValue().getName());
		assertEquals(2, hotelCaptor.getValue().getAmenities().size());
	}

	@Test
	void create_duplicateName_throwsDuplicateHotelNameException() {
		HotelRequestDto dto = new HotelRequestDto();
		dto.setName("Hotel");
		dto.setAddress("a");
		dto.setCity("c");
		dto.setCountry("ct");

		when(hotelRepository.existsByNameIgnoreCase("Hotel")).thenReturn(true);

		assertThrows(DuplicateHotelNameException.class, () -> service.create(dto));
		verify(hotelRepository).existsByNameIgnoreCase("Hotel");
		verifyNoMoreInteractions(hotelRepository);
		verifyNoInteractions(amenityRepository);
	}

	@Test
	void create_saveThrowsDataIntegrity_wrappedAsDuplicateHotelNameException() {
		HotelRequestDto dto = new HotelRequestDto();
		dto.setName("H");
		dto.setAddress("a");
		dto.setCity("c");
		dto.setCountry("ct");

		when(hotelRepository.existsByNameIgnoreCase("H")).thenReturn(false);
		when(hotelRepository.save(any())).thenThrow(new DataIntegrityViolationException("uk_violation"));

		assertThrows(DuplicateHotelNameException.class, () -> service.create(dto));
		verify(hotelRepository).save(any());
	}

	@Test
	void create_invalidCheckTimes_throwsBadRequest() {
		HotelRequestDto dto = new HotelRequestDto();
		dto.setName("n");
		dto.setAddress("a");
		dto.setCity("c");
		dto.setCountry("ct");
		dto.setCheckInTime(LocalTime.of(12, 0));
		dto.setCheckOutTime(LocalTime.of(11, 0));

		assertThrows(com.swer313.projectstep1.errors.BadRequestException.class, () -> service.create(dto));
		verifyNoInteractions(hotelRepository);
	}

	@Test
	void update_valid_updatesAndSaves() {
		Long id = 5L;
		Hotel existing = new Hotel();
		existing.setId(id);
		existing.setName("Old");

		HotelRequestDto dto = new HotelRequestDto();
		dto.setName(" New ");
		dto.setAddress("a");
		dto.setCity("c");
		dto.setCountry("ct");
		dto.setAmenityIds(Set.of(3L));

		when(hotelRepository.findById(id)).thenReturn(Optional.of(existing));
		when(hotelRepository.existsByNameIgnoreCaseAndIdNot("New", id)).thenReturn(false);

		Amenity a = new Amenity();
		a.setId(3L);
		a.setName("X");

		when(amenityRepository.findAllById(dto.getAmenityIds())).thenReturn(List.of(a));
		when(hotelRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		var resp = service.update(id, dto);

		assertNotNull(resp);
		verify(hotelRepository).findById(id);
		verify(hotelRepository).existsByNameIgnoreCaseAndIdNot("New", id);
		verify(amenityRepository).findAllById(dto.getAmenityIds());
		verify(hotelRepository).save(hotelCaptor.capture());
		assertEquals("New", hotelCaptor.getValue().getName());
		assertEquals(1, hotelCaptor.getValue().getAmenities().size());
	}

	@Test
	void update_duplicateName_throwsDuplicateHotelNameException() {
		Long id = 6L;
		Hotel existing = new Hotel();
		existing.setId(id);

		HotelRequestDto dto = new HotelRequestDto();
		dto.setName("dup");
		dto.setAddress("a");
		dto.setCity("c");
		dto.setCountry("ct");

		when(hotelRepository.findById(id)).thenReturn(Optional.of(existing));
		when(hotelRepository.existsByNameIgnoreCaseAndIdNot("dup", id)).thenReturn(true);

		assertThrows(DuplicateHotelNameException.class, () -> service.update(id, dto));
		verify(hotelRepository).existsByNameIgnoreCaseAndIdNot("dup", id);
		verify(hotelRepository, never()).save(any());
	}

	@Test
	void patch_nameProvided_andUnique_updatesName() {
		Long id = 7L;
		Hotel existing = new Hotel();
		existing.setId(id);
		existing.setName("Old");

		HotelPatchDto dto = new HotelPatchDto();
		dto.setName("  Patched  ");

		when(hotelRepository.findById(id)).thenReturn(Optional.of(existing));
		when(hotelRepository.existsByNameIgnoreCaseAndIdNot("Patched", id)).thenReturn(false);
		when(hotelRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		var resp = service.patch(id, dto);

		assertEquals("Patched", resp.getName());
		verify(hotelRepository).existsByNameIgnoreCaseAndIdNot("Patched", id);
	}

	@Test
	void patch_nameConflict_throwsDuplicateHotelNameException() {
		Long id = 8L;
		Hotel existing = new Hotel();
		existing.setId(id);

		HotelPatchDto dto = new HotelPatchDto();
		dto.setName("conf");

		when(hotelRepository.findById(id)).thenReturn(Optional.of(existing));
		when(hotelRepository.existsByNameIgnoreCaseAndIdNot("conf", id)).thenReturn(true);

		assertThrows(DuplicateHotelNameException.class, () -> service.patch(id, dto));
		verify(hotelRepository, never()).save(any());
	}

	@Test
	void uploadImages_validFiles_savesAllImagesAndReturnsDtos() {
		Long hotelId = 11L;
		Hotel hotel = new Hotel();
		hotel.setId(hotelId);

		MultipartFile f1 = mock(MultipartFile.class);
		MultipartFile f2 = mock(MultipartFile.class);
		List<MultipartFile> files = asList(f1, f2);

		when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
		when(fileStorageService.storeHotelImage(f1)).thenReturn(new StoredFileResult("f1.png", "/url/f1.png"));
		when(fileStorageService.storeHotelImage(f2)).thenReturn(new StoredFileResult("f2.png", "/url/f2.png"));
		when(hotelImageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		var dtos = service.uploadImages(hotelId, files);

		assertEquals(2, dtos.size());
		verify(fileStorageService).storeHotelImage(f1);
		verify(fileStorageService).storeHotelImage(f2);
		verify(hotelImageRepository, times(2)).save(hotelImageCaptor.capture());

		List<HotelImage> savedImages = hotelImageCaptor.getAllValues();
		assertTrue(savedImages.stream().allMatch(img -> img.getHotel().getId().equals(hotelId)));
	}

	@Test
	void uploadImages_noFiles_throwsBadRequest() {
		Long hotelId = 12L;
		when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(new Hotel()));

		assertThrows(com.swer313.projectstep1.errors.BadRequestException.class,
				() -> service.uploadImages(hotelId, emptyList()));

		verifyNoInteractions(fileStorageService);
	}

	@Test
	void uploadImages_hotelNotFound_throwsHotelNotFoundException() {
		Long hotelId = 13L;
		when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

		assertThrows(HotelNotFoundException.class,
				() -> service.uploadImages(hotelId, List.of(mock(MultipartFile.class))));

		verifyNoInteractions(fileStorageService);
	}

	@Test
	void getHotelImages_hotelExists_returnsMappedDtos() {
		Long hotelId = 20L;
		Hotel hotel = new Hotel();
		hotel.setId(hotelId);

		HotelImage img = new HotelImage();
		img.setFileName("x.png");
		img.setImageUrl("/x.png");
		img.setHotel(hotel);

		when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
		when(hotelImageRepository.findByHotelId(hotelId)).thenReturn(List.of(img));

		var dtos = service.getHotelImages(hotelId);

		assertEquals(1, dtos.size());
		assertEquals("/x.png", dtos.get(0).getImageUrl());
	}

	@Test
	void getHotelImages_hotelNotFound_throwsHotelNotFoundException() {
		Long hotelId = 21L;
		when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

		assertThrows(HotelNotFoundException.class, () -> service.getHotelImages(hotelId));
	}

	@Test
	void deleteHotelImage_imageBelongsToHotel_deletesFileAndImage() {
		Long hotelId = 30L;
		Long imageId = 40L;

		Hotel hotel = new Hotel();
		hotel.setId(hotelId);

		HotelImage img = new HotelImage();
		img.setImageUrl("/some.png");
		img.setHotel(hotel);

		when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
		when(hotelImageRepository.findById(imageId)).thenReturn(Optional.of(img));

		service.deleteHotelImage(hotelId, imageId);

		verify(fileStorageService).deleteFileByUrl("/some.png");
		verify(hotelImageRepository).delete(img);
	}

	@Test
	void deleteHotelImage_imageDoesNotBelong_throwsBadRequest() {
		Long hotelId = 31L;
		Long imageId = 41L;

		Hotel hotel = new Hotel();
		hotel.setId(hotelId);

		Hotel otherHotel = new Hotel();
		otherHotel.setId(999L);

		HotelImage img = new HotelImage();
		img.setImageUrl("/y.png");
		img.setHotel(otherHotel);

		when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
		when(hotelImageRepository.findById(imageId)).thenReturn(Optional.of(img));

		assertThrows(com.swer313.projectstep1.errors.BadRequestException.class,
				() -> service.deleteHotelImage(hotelId, imageId));

		verify(fileStorageService, never()).deleteFileByUrl(anyString());
		verify(hotelImageRepository, never()).delete(any());
	}

	@Test
	void deleteHotelImage_hotelNotFound_throwsHotelNotFoundException_andNoImageLookup() {
		Long hotelId = 32L;
		Long imageId = 42L;

		when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

		assertThrows(HotelNotFoundException.class, () -> service.deleteHotelImage(hotelId, imageId));
		verify(hotelImageRepository, never()).findById(any());
	}

	@Test
	void search_withFilters_returnsPagedDtos() {
		Hotel h = new Hotel();
		h.setId(1L);
		h.setName("S Hotel");

		var pageable = org.springframework.data.domain.PageRequest.of(0, 2);

		when(hotelRepository.findAll(
				org.mockito.ArgumentMatchers.<Specification<Hotel>>any(),
				eq(pageable)
		)).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(h), pageable, 1));

		var page = service.search(
				"q",
				"city",
				"country",
				Hotel.Status.ACTIVE,
				"amen",
				1.0,
				5.0,
				true,
				true,
				true,
				true,
				pageable
		);

		assertEquals(1, page.getTotalElements());
		assertEquals("S Hotel", page.getContent().get(0).getName());

		verify(hotelRepository).findAll(
				org.mockito.ArgumentMatchers.<Specification<Hotel>>any(),
				eq(pageable)
		);
	}

	@Test
	void getActiveById_active_returnsDto() {
		Long id = 2L;
		Hotel h = new Hotel();
		h.setId(id);
		h.setName("Active");
		h.setStatus(Hotel.Status.ACTIVE);

		when(hotelRepository.findById(id)).thenReturn(Optional.of(h));

		var dto = service.getActiveById(id);

		assertEquals("Active", dto.getName());
		verify(hotelRepository).findById(id);
	}

	@Test
	void getActiveById_notFound_throwsHotelNotFoundException() {
		when(hotelRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(HotelNotFoundException.class, () -> service.getActiveById(99L));
	}

	@Test
	void getActiveById_inactive_throwsHotelNotFoundException() {
		Long id = 3L;
		Hotel h = new Hotel();
		h.setId(id);
		h.setStatus(Hotel.Status.INACTIVE);

		when(hotelRepository.findById(id)).thenReturn(Optional.of(h));

		assertThrows(HotelNotFoundException.class, () -> service.getActiveById(id));
	}

	@Test
	void getById_found_returnsDto() {
		Long id = 4L;
		Hotel h = new Hotel();
		h.setId(id);
		h.setName("ById");

		when(hotelRepository.findById(id)).thenReturn(Optional.of(h));

		var dto = service.getById(id);

		assertEquals("ById", dto.getName());
		verify(hotelRepository).findById(id);
	}

	@Test
	void getById_notFound_throwsHotelNotFoundException() {
		when(hotelRepository.findById(100L)).thenReturn(Optional.empty());
		assertThrows(HotelNotFoundException.class, () -> service.getById(100L));
	}

	@Test
	void getActiveCities_returnsList() {
		when(hotelRepository.findDistinctActiveCities(Hotel.Status.ACTIVE))
				.thenReturn(List.of("CityA", "CityB"));

		var list = service.getActiveCities();

		assertEquals(2, list.size());
		verify(hotelRepository).findDistinctActiveCities(Hotel.Status.ACTIVE);
	}

	@Test
	void getActiveCountries_returnsList() {
		when(hotelRepository.findDistinctActiveCountries(Hotel.Status.ACTIVE))
				.thenReturn(List.of("CountryA"));

		var list = service.getActiveCountries();

		assertEquals(1, list.size());
		verify(hotelRepository).findDistinctActiveCountries(Hotel.Status.ACTIVE);
	}

	@Test
	void getDistinctCities_returnsList() {
		when(hotelRepository.findDistinctCities()).thenReturn(List.of("C1"));

		var list = service.getDistinctCities();

		assertEquals(1, list.size());
		verify(hotelRepository).findDistinctCities();
	}

	@Test
	void getDistinctCountries_returnsList() {
		when(hotelRepository.findDistinctCountries()).thenReturn(List.of("CT1", "CT2"));

		var list = service.getDistinctCountries();

		assertEquals(2, list.size());
		verify(hotelRepository).findDistinctCountries();
	}

	@Test
	void delete_existing_setsInactiveAndSaves() {
		Long id = 50L;
		Hotel existing = new Hotel();
		existing.setId(id);
		existing.setStatus(Hotel.Status.ACTIVE);

		when(hotelRepository.findById(id)).thenReturn(Optional.of(existing));
		when(hotelRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		service.delete(id);

		verify(hotelRepository).findById(id);
		verify(hotelRepository).save(hotelCaptor.capture());
		assertEquals(Hotel.Status.INACTIVE, hotelCaptor.getValue().getStatus());
	}

	@Test
	void delete_notFound_throwsHotelNotFoundException() {
		when(hotelRepository.findById(999L)).thenReturn(Optional.empty());
		assertThrows(HotelNotFoundException.class, () -> service.delete(999L));
	}
}
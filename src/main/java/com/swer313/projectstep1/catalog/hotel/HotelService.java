package com.swer313.projectstep1.catalog.hotel;

import com.swer313.projectstep1.user.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
public interface HotelService {

    Page<HotelResponseDto> search(
            String q,
            String city,
            String country,
            Hotel.Status status,
            String amenity,
            Double minRating,
            Double maxRating,
            Boolean hasImage,
            Boolean hasPhone,
            Boolean hasWebsite,
            Boolean hasEmail,
            Pageable pageable
    );

    HotelResponseDto getActiveById(Long id);

    HotelResponseDto getById(Long id);

    HotelResponseDto create(HotelRequestDto dto);

    HotelResponseDto update(Long id, HotelRequestDto dto);

    HotelResponseDto patch(Long id, HotelPatchDto dto);

    void delete(Long id);

    List<String> getActiveCities();

    List<String> getActiveCountries();

    List<String> getDistinctCities();

    List<String> getDistinctCountries();

    List<String> autocompleteNames(String q, int limit);

    List<HotelImageResponseDto> uploadImages(Long hotelId, List<MultipartFile> files);

    List<HotelImageResponseDto> getHotelImages(Long hotelId);

    void deleteHotelImage(Long hotelId, Long imageId);
    void assignManager(Long hotelId, Long userId);
    void removeManager(Long hotelId, Long userId);
    List<UserResponseDTO> getManagers(Long hotelId);

}
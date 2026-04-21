package com.swer313.projectstep1.catalog.hotel;

import com.swer313.projectstep1.catalog.amenities.Amenity;
import com.swer313.projectstep1.catalog.amenities.AmenityRepository;
import com.swer313.projectstep1.errors.BadRequestException;
import com.swer313.projectstep1.files.FileStorageService;
import com.swer313.projectstep1.files.StoredFileResult;
import com.swer313.projectstep1.user.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@Transactional
public class HotelServiceImpl implements HotelService {

    private static final int AUTOCOMPLETE_MAX = 50;

    private final HotelRepository hotelRepository;
    private final AmenityRepository amenityRepository;
    private final HotelImageRepository hotelImageRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    public HotelServiceImpl(HotelRepository hotelRepository,
                            AmenityRepository amenityRepository,
                            HotelImageRepository hotelImageRepository,
                            FileStorageService fileStorageService,
                            UserRepository userRepository) {
        this.hotelRepository = hotelRepository;
        this.amenityRepository = amenityRepository;
        this.hotelImageRepository = hotelImageRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }
    // ───────── Reads ─────────

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponseDto> search(
            String q, String city, String country, Hotel.Status status,
            String amenity, Double minRating, Double maxRating,
            Boolean hasImage, Boolean hasPhone, Boolean hasWebsite, Boolean hasEmail,
            Pageable pageable
    ) {
        var spec = HotelSpecifications.hasStatus(status)
                .and(HotelSpecifications.nameOrAddressOrDescriptionContains(q))
                .and(HotelSpecifications.cityContains(city))
                .and(HotelSpecifications.countryContains(country))
                .and(HotelSpecifications.hasAmenity(amenity))
                .and(HotelSpecifications.ratingBetween(minRating, maxRating))
                .and(HotelSpecifications.hasImage(hasImage))
                .and(HotelSpecifications.hasPhone(hasPhone))
                .and(HotelSpecifications.hasWebsite(hasWebsite))
                .and(HotelSpecifications.hasEmail(hasEmail));

        return hotelRepository.findAll(spec, pageable).map(HotelMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponseDto getActiveById(Long id) {
        Hotel h = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));
        if (h.getStatus() != Hotel.Status.ACTIVE) {
            throw new HotelNotFoundException(id);
        }
        return HotelMapper.toDto(h);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponseDto getById(Long id) {
        Hotel h = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));
        return HotelMapper.toDto(h);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getActiveCities() {
        return hotelRepository.findDistinctActiveCities(Hotel.Status.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getActiveCountries() {
        return hotelRepository.findDistinctActiveCountries(Hotel.Status.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> autocompleteNames(String q, int limit) {
        if (q == null || q.isBlank()) return List.of();

        int safeLimit = Math.min(Math.max(limit, 1), AUTOCOMPLETE_MAX);

        return hotelRepository.findActiveNameContaining(
                q.trim(),
                Hotel.Status.ACTIVE,
                PageRequest.of(0, safeLimit)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctCities() {
        return hotelRepository.findDistinctCities();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctCountries() {
        return hotelRepository.findDistinctCountries();
    }

    // ───────── Writes ─────────


    @Override
    public HotelResponseDto create(HotelRequestDto dto) {
        validateCheckInOutTimes(dto.getCheckInTime(), dto.getCheckOutTime());
        validateCoordinates(dto.getLatitude(), dto.getLongitude());

        String normalizedName = dto.getName().trim();

        if (hotelRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateHotelNameException(normalizedName);
        }

        Hotel h = HotelMapper.toEntity(dto);
        h.setName(normalizedName);
        h.setStatus(dto.getStatus() != null ? dto.getStatus() : Hotel.Status.ACTIVE);
        h.setAmenities(resolveAmenities(dto.getAmenityIds()));

        try {
            Hotel savedHotel = hotelRepository.save(h);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            if (currentUser.getRole() == UserRole.MANAGER) {
                currentUser.addManagedHotel(savedHotel);
                userRepository.save(currentUser);
            }

            return HotelMapper.toDto(savedHotel);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateHotelNameException(normalizedName);
        }
    }

    @Override
    public HotelResponseDto update(Long id, HotelRequestDto dto) {
        Hotel existing = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));

        validateCheckInOutTimes(dto.getCheckInTime(), dto.getCheckOutTime());
        validateCoordinates(dto.getLatitude(), dto.getLongitude());

        String normalizedName = dto.getName().trim();

        if (hotelRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new DuplicateHotelNameException(normalizedName);
        }

        HotelMapper.updateEntity(existing, dto);
        existing.setName(normalizedName);

        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        if (dto.getAmenityIds() != null) {
            existing.setAmenities(resolveAmenities(dto.getAmenityIds()));
        }

        try {
            return HotelMapper.toDto(hotelRepository.save(existing));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateHotelNameException(normalizedName);
        }
    }

    @Override
    public HotelResponseDto patch(Long id, HotelPatchDto dto) {
        Hotel existing = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));

        if (dto.getName() != null) {
            String normalizedName = dto.getName().trim();
            if (hotelRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
                throw new DuplicateHotelNameException(normalizedName);
            }
            existing.setName(normalizedName);
        }

        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getPhoneNumber() != null) existing.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
        if (dto.getWebsiteUrl() != null) existing.setWebsiteUrl(dto.getWebsiteUrl());
        if (dto.getRating() != null) existing.setRating(dto.getRating());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());

        return HotelMapper.toDto(hotelRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        Hotel existing = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));

        existing.setStatus(Hotel.Status.INACTIVE);
        hotelRepository.save(existing);
    }

    // ───────── Images ─────────

    @Override
    public List<HotelImageResponseDto> uploadImages(Long hotelId, List<MultipartFile> files) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one image is required.");
        }

        return files.stream().map(file -> {
            StoredFileResult stored = fileStorageService.storeHotelImage(file);

            HotelImage image = new HotelImage();
            image.setHotel(hotel);
            image.setFileName(stored.getFileName());
            image.setImageUrl(stored.getFileUrl());

            return HotelMapper.toImageDto(hotelImageRepository.save(image));
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelImageResponseDto> getHotelImages(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        return hotelImageRepository.findByHotelId(hotel.getId())
                .stream()
                .map(HotelMapper::toImageDto)
                .toList();
    }

    @Override
    public void deleteHotelImage(Long hotelId, Long imageId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        HotelImage image = hotelImageRepository.findById(imageId)
                .orElseThrow(() -> new HotelImageNotFoundException(imageId));

        if (!image.getHotel().getId().equals(hotel.getId())) {
            throw new BadRequestException("This image does not belong to the specified hotel.");
        }

        fileStorageService.deleteFileByUrl(image.getImageUrl());
        hotelImageRepository.delete(image);
    }
    @Override
    public void assignManager(Long hotelId, Long userId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getRole() != UserRole.MANAGER) {
            throw new UserNotManagerException(userId);
        }

        if (user.managesHotel(hotelId)) {
            throw new ManagerAlreadyAssignedException(userId, hotelId);
        }

        user.addManagedHotel(hotel);
        userRepository.save(user);
    }

    @Override
    public void removeManager(Long hotelId, Long userId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.managesHotel(hotelId)) {
            throw new ManagerNotAssignedException(userId, hotelId);
        }

        user.removeManagedHotel(hotel);
        userRepository.save(user);
    }

    @Override
    public List<UserResponseDTO> getManagers(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new HotelNotFoundException(hotelId);
        }

        return userRepository.findByManagedHotels_Id(hotelId)
                .stream()
                .map(UserResponseDTO::new)
                .toList();
    }
    // ───────── Helpers ─────────

    private Set<Amenity> resolveAmenities(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();

        List<Amenity> found = amenityRepository.findAllById(ids);

        if (found.size() != ids.size()) {
            throw new InvalidHotelAmenityReferenceException(ids);
        }

        return new HashSet<>(found);
    }

    private void validateCheckInOutTimes(LocalTime in, LocalTime out) {
        if (in != null && out != null && !out.isAfter(in)) {
            throw new BadRequestException("checkOut must be after checkIn.");
        }
    }

    private void validateCoordinates(Double lat, Double lon) {
        if ((lat == null) != (lon == null)) {
            throw new BadRequestException("lat & lon must be together.");
        }
    }
}
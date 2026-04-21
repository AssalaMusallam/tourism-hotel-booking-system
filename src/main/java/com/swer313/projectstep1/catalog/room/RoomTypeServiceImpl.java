package com.swer313.projectstep1.catalog.room;

import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.catalog.amenities.Amenity;
import com.swer313.projectstep1.catalog.amenities.AmenityRepository;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelNotFoundException;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.errors.ConflictException;
import com.swer313.projectstep1.files.FileStorageService;
import com.swer313.projectstep1.files.StoredFileResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.swer313.projectstep1.errors.BadRequestException;
import java.math.BigDecimal;
import java.util.*;

import static com.swer313.projectstep1.catalog.hotel.HotelMapper.toImageDto;

@Service
@Transactional
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository repository;
    private final AmenityRepository amenityRepository;
    private final RoomTypeImageRepository roomTypeImageRepository;
    private final FileStorageService fileStorageService ;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;

    public RoomTypeServiceImpl(RoomTypeRepository repository,
                               AmenityRepository amenityRepository,
                               RoomTypeImageRepository roomTypeImageRepository,
                               FileStorageService fileStorageService,
                               HotelRepository hotelRepository,
                               BookingRepository bookingRepository) {  // ← أضف هاد
        this.repository              = repository;
        this.amenityRepository       = amenityRepository;
        this.roomTypeImageRepository = roomTypeImageRepository;
        this.fileStorageService      = fileStorageService;
        this.hotelRepository         = hotelRepository;
        this.bookingRepository= bookingRepository;// ← أضف هاد
    }
    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponseDto getById(Long id) {
        RoomType roomType = repository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        if (roomType.getStatus() != RoomTypeStatus.ACTIVE) {
            throw new RoomTypeNotFoundException(id);
        }

        return RoomTypeMapper.toDto(roomType);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RoomTypeResponseDto> listAll(
            Pageable pageable,
            Long hotelId,
            String name,
            BedType bedType,
            Integer bedCountMin,
            Integer bedCountMax,
            Integer capacityMin,
            Integer capacityMax,
            Integer maxAdultsMin,
            Integer maxAdultsMax,
            Integer maxChildrenMin,
            Integer maxChildrenMax,
            BigDecimal priceMin,
            BigDecimal priceMax,
            String q
    ) {
        return listInternal(
                pageable, hotelId, name, bedType,
                bedCountMin, bedCountMax,
                capacityMin, capacityMax,
                maxAdultsMin, maxAdultsMax,
                maxChildrenMin, maxChildrenMax,
                priceMin, priceMax,
                RoomTypeStatus.ACTIVE, q
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponseDto getByIdAdmin(Long hotelId, Long id) {
        RoomType roomType = repository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        if (!Objects.equals(roomType.getHotelId(), hotelId)) {
            throw new RoomTypeNotFoundException(id);
        }

        return RoomTypeMapper.toDto(roomType);
    }

    @Override
    public RoomTypeResponseDto create(Long hotelId, RoomTypeRequestDto dto) {
        if (dto != null) {
            dto.setHotelId(hotelId);
        }

        validateBusiness(dto);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        if (repository.existsByHotel_IdAndNameIgnoreCase(hotelId, dto.getName().trim())) {
            throw new DuplicateRoomTypeException(hotelId, dto.getName().trim());
        }

        RoomType entity = RoomTypeMapper.toEntity(dto, hotel);
        if (entity.getStatus() == null) {
            entity.setStatus(RoomTypeStatus.ACTIVE);
        }

        entity.setAmenities(resolveAmenities(dto.getAmenityIds()));

        RoomType saved = repository.save(entity);
        return RoomTypeMapper.toDto(saved);
    }

    @Override
    public RoomTypeResponseDto update(Long hotelId, Long id, RoomTypeRequestDto dto) {
        if (dto != null) {
            dto.setHotelId(hotelId);
        }

        validateBusiness(dto);

        RoomType existing = repository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        if (!Objects.equals(existing.getHotelId(), hotelId)) {
            throw new RoomTypeNotFoundException(id);
        }

        String newName = dto.getName().trim();

        if (repository.existsByHotel_IdAndNameIgnoreCaseAndIdNot(hotelId, newName, id)) {
            throw new DuplicateRoomTypeException(hotelId, newName);
        }

        RoomTypeMapper.apply(existing, dto);
        existing.setAmenities(resolveAmenities(dto.getAmenityIds()));

        RoomType saved = repository.save(existing);
        return RoomTypeMapper.toDto(saved);
    }

    @Override
    public void delete(Long hotelId, Long id) {
        // 1. تحقق إن الغرفة موجودة
        RoomType existing = repository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        // 2. تحقق إن الغرفة تبع هالفندق
        if (!Objects.equals(existing.getHotelId(), hotelId)) {
            throw new RoomTypeNotFoundException(id);
        }

        // 3. تحقق إذا عندها حجوزات نشطة
        boolean hasActiveBookings = bookingRepository.existsByRoomTypeIdAndStatusIn(
                id,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

        if (hasActiveBookings) {
            throw new ConflictException(
                    "Cannot delete room type with active bookings. Deactivate it instead."
            );
        }

        // 4. احذف فعلاً
        repository.delete(existing);
    }

    @Override
    public RoomTypeResponseDto changeStatus(Long hotelId, Long id, RoomTypeStatus status) {
        if (status == null) {
            throw new BadRequestException("status is required.");
        }

        RoomType existing = repository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        if (!Objects.equals(existing.getHotelId(), hotelId)) {
            throw new RoomTypeNotFoundException(id);
        }

        existing.setStatus(status);
        return RoomTypeMapper.toDto(repository.save(existing));
    }



    @Override
    public RoomTypeResponseDto replacePolicies(Long hotelId, Long id, String policies) {
        RoomType existing = repository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        if (!Objects.equals(existing.getHotelId(), hotelId)) {
            throw new RoomTypeNotFoundException(id);
        }

        existing.setPolicies(policies);
        return RoomTypeMapper.toDto(repository.save(existing));
    }

    @Override
    public RoomTypeResponseDto clone(Long hotelId, Long id) {
        RoomType source = repository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        if (!Objects.equals(source.getHotelId(), hotelId)) {
            throw new RoomTypeNotFoundException(id);
        }

        RoomType copy = new RoomType();
        copy.setHotel(source.getHotel());
        copy.setName(source.getName() + " Copy");
        copy.setCapacity(source.getCapacity());
        copy.setBedType(source.getBedType());
        copy.setBedCount(source.getBedCount());
        copy.setMaxAdults(source.getMaxAdults());
        copy.setMaxChildren(source.getMaxChildren());
        copy.setBasePrice(source.getBasePrice());
        copy.setTotalUnits(source.getTotalUnits());
        copy.setDescription(source.getDescription());
        copy.setPolicies(source.getPolicies());

        copy.setStatus(source.getStatus());
        copy.setAmenities(new HashSet<>(source.getAmenities()));

        if (repository.existsByHotel_IdAndNameIgnoreCase(hotelId, copy.getName())) {
            copy.setName(copy.getName() + " " + System.currentTimeMillis());
        }

        return RoomTypeMapper.toDto(repository.save(copy));
    }

    @Override
    public BulkStatusResult bulkStatus(Long hotelId, List<Long> ids, RoomTypeStatus status) {
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException("ids are required.");
        }
        if (status == null) {
            throw new BadRequestException("status is required.");
        }

        List<Long> updatedIds = new ArrayList<>();
        List<Long> notFoundIds = new ArrayList<>();

        for (Long id : ids) {
            Optional<RoomType> opt = repository.findById(id);
            if (opt.isEmpty() || !Objects.equals(opt.get().getHotelId(), hotelId)) {
                notFoundIds.add(id);
                continue;
            }
            RoomType roomType = opt.get();
            roomType.setStatus(status);
            repository.save(roomType);
            updatedIds.add(id);
        }

        return new BulkStatusResult(updatedIds, notFoundIds, status);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RoomTypeResponseDto> listByHotel(
            Long hotelId,
            Pageable pageable,
            String name,
            BedType bedType,
            Integer bedCountMin,
            Integer bedCountMax,
            Integer capacityMin,
            Integer capacityMax,
            Integer maxAdultsMin,
            Integer maxAdultsMax,
            Integer maxChildrenMin,
            Integer maxChildrenMax,
            BigDecimal priceMin,
            BigDecimal priceMax,
            RoomTypeStatus status,
            String q
    ) {
        return listInternal(
                pageable, hotelId, name, bedType,
                bedCountMin, bedCountMax,
                capacityMin, capacityMax,
                maxAdultsMin, maxAdultsMax,
                maxChildrenMin, maxChildrenMax,
                priceMin, priceMax,
                status, q
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RoomTypeMinimalDto> minimal(Long hotelId, RoomTypeStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));

        PagedResponse<RoomTypeResponseDto> full = listByHotel(
                hotelId, pageable, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, status, null
        );

        List<RoomTypeMinimalDto> content = full.getContent().stream()
                .map(r -> new RoomTypeMinimalDto(r.getId(), r.getName()))
                .toList();

        return new PagedResponse<>(
                content,
                full.getPageNumber(),
                full.getPageSize(),
                full.getTotalElements(),
                full.getTotalPages(),
                full.isFirst(),
                full.isLast(),
                full.isHasNext(),
                full.isHasPrevious()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeMinimalDto> suggest(Long hotelId, String q) {
        if (q == null || q.isBlank()) {
            throw new BadRequestException("q is required.");
        }

        Pageable pageable = PageRequest.of(0, 10);

        PagedResponse<RoomTypeResponseDto> page = listByHotel(
                hotelId, pageable, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, RoomTypeStatus.ACTIVE, q
        );

        return page.getContent().stream()
                .map(r -> new RoomTypeMinimalDto(r.getId(), r.getName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return repository.existsById(id);
    }

    @Override
    public RoomTypeResponseDto replaceAmenities(Long roomTypeId, Set<Long> amenityIds) {
        RoomType roomType = repository.findById(roomTypeId)
                .orElseThrow(() -> new RoomTypeNotFoundException(roomTypeId));

        roomType.setAmenities(resolveAmenities(amenityIds));
        return RoomTypeMapper.toDto(repository.save(roomType));
    }

    @Override
    public RoomTypeResponseDto addAmenity(Long roomTypeId, Long amenityId) {
        RoomType roomType = repository.findById(roomTypeId)
                .orElseThrow(() -> new RoomTypeNotFoundException(roomTypeId));

        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(InvalidAmenityReferenceException::new);

        roomType.getAmenities().add(amenity);
        return RoomTypeMapper.toDto(repository.save(roomType));
    }

    @Override
    public RoomTypeResponseDto removeAmenity(Long roomTypeId, Long amenityId) {
        RoomType roomType = repository.findById(roomTypeId)
                .orElseThrow(() -> new RoomTypeNotFoundException(roomTypeId));

        roomType.getAmenities().removeIf(a -> Objects.equals(a.getId(), amenityId));
        return RoomTypeMapper.toDto(repository.save(roomType));
    }

    private PagedResponse<RoomTypeResponseDto> listInternal(
            Pageable pageable,
            Long hotelId,
            String name,
            BedType bedType,
            Integer bedCountMin,
            Integer bedCountMax,
            Integer capacityMin,
            Integer capacityMax,
            Integer maxAdultsMin,
            Integer maxAdultsMax,
            Integer maxChildrenMin,
            Integer maxChildrenMax,
            BigDecimal priceMin,
            BigDecimal priceMax,
            RoomTypeStatus status,
            String q
    ) {
        validateRange("bedCount", bedCountMin, bedCountMax);
        validateRange("capacity", capacityMin, capacityMax);
        validateRange("maxAdults", maxAdultsMin, maxAdultsMax);
        validateRange("maxChildren", maxChildrenMin, maxChildrenMax);
        validatePriceRange(priceMin, priceMax);

        Specification<RoomType> spec = Specification
                .where(RoomTypeSpecifications.hotelIdEq(hotelId))
                .and(RoomTypeSpecifications.nameEq(name))
                .and(RoomTypeSpecifications.bedTypeEq(bedType))
                .and(RoomTypeSpecifications.bedCountBetween(bedCountMin, bedCountMax))
                .and(RoomTypeSpecifications.capacityBetween(capacityMin, capacityMax))
                .and(RoomTypeSpecifications.maxAdultsBetween(maxAdultsMin, maxAdultsMax))
                .and(RoomTypeSpecifications.maxChildrenBetween(maxChildrenMin, maxChildrenMax))
                .and(RoomTypeSpecifications.basePriceBetween(priceMin, priceMax))
                .and(RoomTypeSpecifications.statusEq(status))
                .and(RoomTypeSpecifications.qLike(q));

        Page<RoomType> page = repository.findAll(spec, pageable);

        List<RoomTypeResponseDto> content = page.getContent()
                .stream()
                .map(RoomTypeMapper::toDto)
                .toList();

        return PagedResponse.from(page, content);
    }

    private Set<Amenity> resolveAmenities(Set<Long> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Amenity> amenities = amenityRepository.findAllById(amenityIds);

        if (amenities.size() != amenityIds.size()) {
            throw new InvalidAmenityReferenceException();
        }

        return new HashSet<>(amenities);
    }

    private void validateBusiness(RoomTypeRequestDto dto) {
        if (dto == null) throw new BadRequestException("Request body is required.");
        if (dto.getName() == null || dto.getName().isBlank()) throw new BadRequestException("name is required.");
        if (dto.getHotelId() == null) throw new BadRequestException("hotelId is required.");
        if (dto.getCapacity() < 1) throw new BadRequestException("capacity must be >= 1.");
        if (dto.getBedType() == null) throw new BadRequestException("bedType is required.");
        if (dto.getBedCount() < 1) throw new BadRequestException("bedCount must be >= 1.");
        if (dto.getMaxAdults() < 1) throw new BadRequestException("maxAdults must be >= 1.");
        if (dto.getMaxChildren() < 0) throw new BadRequestException("maxChildren must be >= 0.");
        if (dto.getBasePrice() == null || dto.getBasePrice().signum() < 0) throw new BadRequestException("basePrice must be >= 0.");
        if (dto.getTotalUnits() < 0) throw new BadRequestException("totalUnits must be >= 0.");

        int totalGuests = dto.getMaxAdults() + dto.getMaxChildren();
        if (dto.getCapacity() < totalGuests) {
            throw new BadRequestException("capacity must be >= (maxAdults + maxChildren).");
        }
    }

    private void validateRange(String field, Integer min, Integer max) {
        if (min != null && min < 0) throw new BadRequestException(field + "Min must be >= 0");
        if (max != null && max < 0) throw new BadRequestException(field + "Max must be >= 0");
        if (min != null && max != null && min > max) {
            throw new BadRequestException(field + "Min must be <= " + field + "Max");
        }
    }

    private void validatePriceRange(BigDecimal min, BigDecimal max) {
        if (min != null && min.signum() < 0) throw new BadRequestException("priceMin must be >= 0");
        if (max != null && max.signum() < 0) throw new BadRequestException("priceMax must be >= 0");
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BadRequestException("priceMin must be <= priceMax");
        }

    }
    @Override
    public List<RoomTypeImageResponseDto> uploadImages(Long roomTypeId,
                                                       List<MultipartFile> files) {
        RoomType roomType = findOrThrow(roomTypeId);

        return files.stream().map(file -> {
            StoredFileResult result = fileStorageService.storeRoomTypeImage(file);

            RoomTypeImage image = new RoomTypeImage();
            image.setImageUrl(result.getFileUrl());
            image.setFileName(result.getFileName());
            image.setRoomType(roomType);

            RoomTypeImage saved = roomTypeImageRepository.save(image);
            return toImageDto(saved);
        }).toList();
    }

    @Override
    public List<RoomTypeImageResponseDto> getRoomTypeImages(Long roomTypeId) {
        findOrThrow(roomTypeId); // تأكد إن الـ roomType موجود
        return roomTypeImageRepository.findByRoomTypeId(roomTypeId)
                .stream()
                .map(this::toImageDto)
                .toList();
    }


    @Override
    public void deleteRoomTypeImage(Long roomTypeId, Long imageId) {
        findOrThrow(roomTypeId);
        RoomTypeImage image = roomTypeImageRepository.findById(imageId)
                .orElseThrow(() -> new RoomTypeImageNotFoundException(imageId));
        fileStorageService.deleteFileByUrl(image.getImageUrl());
        roomTypeImageRepository.delete(image);
    }
    // helper
    private RoomTypeImageResponseDto toImageDto(RoomTypeImage image) {
        RoomTypeImageResponseDto dto = new RoomTypeImageResponseDto();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setFileName(image.getFileName());
        return dto;
    }
    private RoomType findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));
    }
}
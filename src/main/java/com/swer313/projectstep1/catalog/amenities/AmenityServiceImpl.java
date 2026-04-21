package com.swer313.projectstep1.catalog.amenities;
import com.swer313.projectstep1.errors.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AmenityServiceImpl implements AmenityService {
    private final AmenityRepository repository;
    private final AmenityMapper mapper;

    public AmenityServiceImpl(AmenityRepository repository, AmenityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AmenityResponseDTO> getAll(
            Pageable pageable,
            String name,
            Amenity.AmenityCategory category,
            Boolean premium,
            Boolean active,
            String q,
            Long roomTypeId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo
    ) {
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new BadRequestException("createdFrom must be before or equal to createdTo");
        }

        Specification<Amenity> spec = Specification
                .where(AmenitySpecifications.nameEq(name))
                .and(AmenitySpecifications.categoryEq(category))
                .and(AmenitySpecifications.premiumEq(premium))
                .and(AmenitySpecifications.activeEq(active))
                .and(AmenitySpecifications.qLike(q))
                .and(AmenitySpecifications.roomTypeIdEq(roomTypeId))
                .and(AmenitySpecifications.createdAtBetween(createdFrom, createdTo));

        Page<Amenity> page = repository.findAll(spec, pageable);
        List<AmenityResponseDTO> content = page.getContent()
                .stream()
                .map(mapper::toDto)
                .toList();

        return PagedResponse.from(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public AmenityResponseDTO getById(Long id) {
        Amenity amenity = repository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException(id));
        return mapper.toDto(amenity);
    }

    @Override
    public AmenityResponseDTO create(AmenityRequestDTO dto) {
        validate(dto);

        String normalizedName = dto.getName().trim();

        if (repository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateAmenityException(normalizedName);
        }

        Amenity amenity = mapper.toEntity(dto);
        amenity.setName(normalizedName);

        Amenity saved = repository.save(amenity);
        return mapper.toDto(saved);
    }

    @Override
    public AmenityResponseDTO update(Long id, AmenityRequestDTO dto) {
        validate(dto);

        Amenity existing = repository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException(id));

        String normalizedName = dto.getName().trim();
        if (repository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new DuplicateAmenityException(normalizedName);
        }

        mapper.updateEntity(existing, dto);
        existing.setName(normalizedName);

        Amenity updated = repository.save(existing);
        return mapper.toDto(updated);
    }

    @Override
    public AmenityResponseDTO activate(Long id) {
        Amenity existing = repository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException(id));

        existing.setActive(true);
        return mapper.toDto(repository.save(existing));
    }

    @Override
    public AmenityResponseDTO deactivate(Long id) {
        Amenity existing = repository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException(id));

        existing.setActive(false);
        return mapper.toDto(repository.save(existing));
    }

    @Override
    public AmenityResponseDTO restore(Long id) {
        Amenity existing = repository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException(id));

        existing.setActive(true);
        return mapper.toDto(repository.save(existing));
    }

    @Override
    public void delete(Long id) {
        Amenity existing = repository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException(id));

        existing.setActive(false);
        repository.save(existing);
    }

    @Override
    public void hardDelete(Long id) {
        Amenity existing = repository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException(id));

        existing.getRoomTypes().forEach(roomType -> roomType.getAmenities().remove(existing));
        existing.getRoomTypes().clear();

        repository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityMinimalDTO> suggest(String q, Boolean active) {
        if (q == null || q.isBlank()) return List.of();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                0, 10, org.springframework.data.domain.Sort.by("name").ascending()
        );

        Specification<Amenity> spec = Specification
                .where(AmenitySpecifications.qLike(q))
                .and(AmenitySpecifications.activeEq(active));

        return repository.findAll(spec, pageable)
                .getContent()
                .stream()
                .map(a -> new AmenityMinimalDTO(a.getId(), a.getName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AmenityMinimalDTO> minimal(Boolean active, Pageable pageable) {
        Specification<Amenity> spec = Specification.where(AmenitySpecifications.activeEq(active));

        Page<Amenity> page = repository.findAll(spec, pageable);
        List<AmenityMinimalDTO> content = page.getContent()
                .stream()
                .map(a -> new AmenityMinimalDTO(a.getId(), a.getName()))
                .toList();

        return PagedResponse.from(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> exists(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("name is required");
        }

        String normalized = name.trim();
        boolean exists = repository.existsByNameIgnoreCase(normalized);

        return Map.of(
                "name", normalized,
                "exists", exists
        );
    }

    @Override
    public Map<String, Object> bulkStatus(AmenityBulkStatusRequest body) {
        if (body == null || body.getIds() == null || body.getIds().isEmpty()) {
            throw new BadRequestException("ids are required");
        }
        if (body.getActive() == null) {
            throw new BadRequestException("active is required");
        }

        List<Long> updatedIds = body.getIds().stream().map(id -> {
            Amenity amenity = repository.findById(id)
                    .orElseThrow(() -> new AmenityNotFoundException(id));
            amenity.setActive(body.getActive());
            repository.save(amenity);
            return id;
        }).toList();

        return Map.of(
                "active", body.getActive(),
                "updatedIds", updatedIds,
                "count", updatedIds.size()
        );
    }

    private void validate(AmenityRequestDTO dto) {
        if (dto == null) throw new BadRequestException("Request body is required");
        if (dto.getName() == null || dto.getName().isBlank())
            throw new BadRequestException("Amenity name is required");
        if (dto.getDescription() == null || dto.getDescription().isBlank())
            throw new BadRequestException("Amenity description is required");
        if (dto.getCategory() == null)
            throw new BadRequestException("Amenity category is required");
    }
}
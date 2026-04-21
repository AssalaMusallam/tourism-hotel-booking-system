package com.swer313.projectstep1.catalog.amenities;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/amenities")
@Tag(name = "4. Amenities", description = "Manage amenities")
public class AmenityController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "category", "createdAt", "updatedAt"
    );

    private final AmenityService amenityService;

    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }

    @Operation(summary = "Get all amenities with filters and pagination")
    @GetMapping
    public ResponseEntity<PagedResponse<AmenityResponseDTO>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Amenity.AmenityCategory category,
            @RequestParam(required = false) Boolean premium,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) LocalDateTime createdFrom,
            @RequestParam(required = false) LocalDateTime createdTo,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);

        return ResponseEntity.ok(
                amenityService.getAll(
                        pageable,
                        name,
                        category,
                        premium,
                        active,
                        q,
                        roomTypeId,
                        createdFrom,
                        createdTo
                )
        );
    }

    @Operation(summary = "Get amenity by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AmenityResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(amenityService.getById(id));
    }

    @Operation(summary = "Create amenity")
    @ApiResponse(responseCode = "201", description = "Amenity created successfully")
    @PostMapping
    public ResponseEntity<AmenityResponseDTO> create(@Valid @RequestBody AmenityRequestDTO dto) {
        AmenityResponseDTO created = amenityService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update amenity")
    @PutMapping("/{id}")
    public ResponseEntity<AmenityResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AmenityRequestDTO dto
    ) {
        return ResponseEntity.ok(amenityService.update(id, dto));
    }

    @Operation(summary = "Soft delete amenity")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        amenityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Hard delete amenity")
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        amenityService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate amenity")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<AmenityResponseDTO> activate(@PathVariable Long id) {
        return ResponseEntity.ok(amenityService.activate(id));
    }

    @Operation(summary = "Deactivate amenity")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<AmenityResponseDTO> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(amenityService.deactivate(id));
    }

    @Operation(summary = "Restore deleted amenity")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<AmenityResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(amenityService.restore(id));
    }

    @Operation(summary = "Check if amenity exists by name")
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Object>> exists(@RequestParam String name) {
        return ResponseEntity.ok(amenityService.exists(name));
    }

    @Operation(summary = "Get minimal amenities list")
    @GetMapping("/minimal")
    public ResponseEntity<PagedResponse<AmenityMinimalDTO>> minimal(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        if (size > MAX_SIZE) size = MAX_SIZE;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(amenityService.minimal(active, pageable));
    }

    @Operation(summary = "Suggest amenities by query")
    @GetMapping("/suggest")
    public ResponseEntity<?> suggest(
            @RequestParam String q,
            @RequestParam(required = false) Boolean active
    ) {
        return ResponseEntity.ok(amenityService.suggest(q, active));
    }

    @Operation(summary = "Bulk update amenities status")
    @PatchMapping("/status")
    public ResponseEntity<Map<String, Object>> bulkStatus(@RequestBody AmenityBulkStatusRequest body) {
        return ResponseEntity.ok(amenityService.bulkStatus(body));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        if (size > MAX_SIZE) throw new IllegalArgumentException("size must be <= " + MAX_SIZE);

        return PageRequest.of(page, size, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.by(Sort.Direction.ASC, "id");

        String[] parts = sort.split(",");
        String field = parts[0].trim();

        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Invalid sort field: " + field);
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String dir = parts[1].trim().toLowerCase();
            if ("desc".equals(dir)) direction = Sort.Direction.DESC;
            else if (!"asc".equals(dir)) {
                throw new IllegalArgumentException("Invalid sort direction: " + parts[1]);
            }
        }

        return Sort.by(direction, field);
    }
}
package com.swer313.projectstep1.catalog.room;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@Validated
@Tag(name = "3. Room Types", description = "Browse and manage room types")
public class RoomTypeController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "capacity", "bedCount", "maxAdults", "maxChildren",
            "basePrice", "totalUnits", "status"
    );

    private final RoomTypeService service;

    public RoomTypeController(RoomTypeService service) {
        this.service = service;
    }

    @Operation(summary = "Replace room type amenities")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessRoomType(#id)")
    @PutMapping("/api/admin/room-types/{id}/amenities")
    public RoomTypeResponseDto replaceAmenities(
            @PathVariable Long id,
            @RequestBody ReplaceRoomAmenitiesRequest body
    ) {
        if (body == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        return service.replaceAmenities(id, body.getAmenityIds());
    }

    @Operation(summary = "Add amenity to room type")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessRoomType(#id)")
    @PatchMapping("/api/admin/room-types/{id}/amenities/{amenityId}")
    public RoomTypeResponseDto addAmenity(
            @PathVariable Long id,
            @PathVariable Long amenityId
    ) {
        return service.addAmenity(id, amenityId);
    }

    @Operation(summary = "Remove amenity from room type")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessRoomType(#id)")
    @DeleteMapping("/api/admin/room-types/{id}/amenities/{amenityId}")
    public RoomTypeResponseDto removeAmenity(
            @PathVariable Long id,
            @PathVariable Long amenityId
    ) {
        return service.removeAmenity(id, amenityId);
    }

    @Operation(summary = "Get room type by ID")
    @GetMapping("/api/room-types/{id}")
    public ResponseEntity<RoomTypeResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "List all room types")
    @GetMapping("/api/room-types")
    public ResponseEntity<PagedResponse<RoomTypeResponseDto>> listAll(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BedType bedType,
            @RequestParam(required = false) Integer bedCountMin,
            @RequestParam(required = false) Integer bedCountMax,
            @RequestParam(required = false) Integer capacityMin,
            @RequestParam(required = false) Integer capacityMax,
            @RequestParam(required = false) Integer maxAdultsMin,
            @RequestParam(required = false) Integer maxAdultsMax,
            @RequestParam(required = false) Integer maxChildrenMin,
            @RequestParam(required = false) Integer maxChildrenMax,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir) {

        Pageable pageable = buildPageable(page, size, sort, dir);
        return ResponseEntity.ok(service.listAll(
                pageable, hotelId, name, bedType,
                bedCountMin, bedCountMax,
                capacityMin, capacityMax,
                maxAdultsMin, maxAdultsMax,
                maxChildrenMin, maxChildrenMax,
                priceMin, priceMax, q
        ));
    }

    @Operation(summary = "Get room type status")
    @GetMapping("/api/room-types/{id}/status")
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("status", service.getById(id).getStatus().name()));
    }

    @Operation(summary = "List room types by hotel")
    @GetMapping("/api/hotels/{hotelId}/room-types")
    public ResponseEntity<PagedResponse<RoomTypeResponseDto>> listByHotel(
            @PathVariable Long hotelId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BedType bedType,
            @RequestParam(required = false) Integer bedCountMin,
            @RequestParam(required = false) Integer bedCountMax,
            @RequestParam(required = false) Integer capacityMin,
            @RequestParam(required = false) Integer capacityMax,
            @RequestParam(required = false) Integer maxAdultsMin,
            @RequestParam(required = false) Integer maxAdultsMax,
            @RequestParam(required = false) Integer maxChildrenMin,
            @RequestParam(required = false) Integer maxChildrenMax,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) RoomTypeStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir) {

        Pageable pageable = buildPageable(page, size, sort, dir);
        return ResponseEntity.ok(service.listByHotel(
                hotelId, pageable, name, bedType,
                bedCountMin, bedCountMax,
                capacityMin, capacityMax,
                maxAdultsMin, maxAdultsMax,
                maxChildrenMin, maxChildrenMax,
                priceMin, priceMax, status, q
        ));
    }

    @Operation(summary = "Get minimal room types list")
    @GetMapping("/api/hotels/{hotelId}/room-types/meta/minimal")
    public ResponseEntity<PagedResponse<RoomTypeService.RoomTypeMinimalDto>> minimal(
            @PathVariable Long hotelId,
            @RequestParam(required = false) RoomTypeStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) int size) {

        return ResponseEntity.ok(service.minimal(hotelId, status, page, size));
    }

    @Operation(summary = "Suggest room types by query")
    @GetMapping("/api/hotels/{hotelId}/room-types/suggest")
    public ResponseEntity<List<RoomTypeService.RoomTypeMinimalDto>> suggest(
            @PathVariable Long hotelId,
            @RequestParam @NotBlank String q) {

        return ResponseEntity.ok(service.suggest(hotelId, q));
    }

    @Operation(summary = "Admin/Manager get room type by hotel and ID")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#hotelId)")
    @GetMapping("/api/hotels/{hotelId}/room-types/{id}")
    public ResponseEntity<RoomTypeResponseDto> getByIdAdmin(
            @PathVariable Long hotelId,
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getByIdAdmin(hotelId, id));
    }

    @Operation(summary = "Create room type")
    @ApiResponse(responseCode = "201", description = "Room type created successfully")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#hotelId)")
    @PostMapping("/api/hotels/{hotelId}/room-types")
    public ResponseEntity<RoomTypeResponseDto> create(
            @PathVariable Long hotelId,
            @Valid @RequestBody RoomTypeRequestDto dto) {

        RoomTypeResponseDto created = service.create(hotelId, dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/hotels/{hotelId}/room-types/{id}")
                .buildAndExpand(hotelId, created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update room type")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#hotelId)")
    @PutMapping("/api/hotels/{hotelId}/room-types/{id}")
    public ResponseEntity<RoomTypeResponseDto> update(
            @PathVariable Long hotelId,
            @PathVariable Long id,
            @Valid @RequestBody RoomTypeRequestDto dto) {

        return ResponseEntity.ok(service.update(hotelId, id, dto));
    }

    @Operation(summary = "Delete room type")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#hotelId)")
    @DeleteMapping("/api/hotels/{hotelId}/room-types/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long hotelId,
            @PathVariable Long id) {

        service.delete(hotelId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Bulk update room types status")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#hotelId)")
    @PatchMapping("/api/hotels/{hotelId}/room-types/bulk-status")
    public ResponseEntity<RoomTypeService.BulkStatusResult> bulkStatus(
            @PathVariable Long hotelId,
            @Valid @RequestBody BulkStatusRequest request) {

        return ResponseEntity.ok(service.bulkStatus(hotelId, request.ids(), request.status()));
    }

    @Operation(summary = "Change room type status")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#hotelId)")
    @PatchMapping("/api/hotels/{hotelId}/room-types/{id}/status")
    public ResponseEntity<RoomTypeResponseDto> changeStatus(
            @PathVariable Long hotelId,
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request) {

        return ResponseEntity.ok(service.changeStatus(hotelId, id, request.status()));
    }

    @Operation(summary = "Replace room type policies")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#hotelId)")
    @PutMapping("/api/hotels/{hotelId}/room-types/{id}/policies")
    public ResponseEntity<RoomTypeResponseDto> replacePolicies(
            @PathVariable Long hotelId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return ResponseEntity.ok(service.replacePolicies(hotelId, id, body.get("policies")));
    }

    private RoomTypeRequestDto copyToRequest(RoomTypeResponseDto existing) {
        RoomTypeRequestDto dto = new RoomTypeRequestDto();
        dto.setHotelId(existing.getHotelId());
        dto.setName(existing.getName());
        dto.setCapacity(existing.getCapacity());
        dto.setBedType(existing.getBedType());
        dto.setBedCount(existing.getBedCount());
        dto.setMaxAdults(existing.getMaxAdults());
        dto.setMaxChildren(existing.getMaxChildren());
        dto.setBasePrice(existing.getBasePrice());
        dto.setTotalUnits(existing.getTotalUnits());
        dto.setDescription(existing.getDescription());
        dto.setPolicies(existing.getPolicies());
        dto.setStatus(existing.getStatus());
        dto.setAmenityIds(existing.getAmenityIds());
        return dto;
    }

    private Pageable buildPageable(int page, int size, String sort, String dir) {
        int safePage = Math.max(page, DEFAULT_PAGE);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);

        String safeSort = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "id";
        Sort.Direction direction = "desc".equalsIgnoreCase(dir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(safePage, safeSize, Sort.by(direction, safeSort));
    }

    public record BulkStatusRequest(
            @NotNull(message = "ids are required") List<Long> ids,
            @NotNull(message = "status is required") RoomTypeStatus status
    ) {}

    public record ChangeStatusRequest(
            @NotNull(message = "status is required") RoomTypeStatus status
    ) {}

    @Operation(summary = "Upload room type images")
    @ApiResponse(responseCode = "201", description = "Images uploaded successfully")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessRoomType(#id)")
    @PostMapping(value = "/api/admin/room-types/{id}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<RoomTypeImageResponseDto>> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.uploadImages(id, files));
    }

    @Operation(summary = "Get room type images")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessRoomType(#id)")
    @GetMapping("/api/admin/room-types/{id}/images")
    public ResponseEntity<List<RoomTypeImageResponseDto>> getImages(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getRoomTypeImages(id));
    }

    @Operation(summary = "Delete room type image")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessRoomType(#roomTypeId)")
    @DeleteMapping("/api/admin/room-types/{roomTypeId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long roomTypeId,
            @PathVariable Long imageId) {
        service.deleteRoomTypeImage(roomTypeId, imageId);
        return ResponseEntity.noContent().build();
    }
}
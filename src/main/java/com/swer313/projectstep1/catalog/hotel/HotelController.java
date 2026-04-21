package com.swer313.projectstep1.catalog.hotel;


import com.swer313.projectstep1.user.UserResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Validated
@Tag(name = "2. Hotels", description = "Browse and manage hotels")
public class HotelController {
    private final HotelService hotelService;
    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    // PUBLIC CATALOG  —  /api/hotels/**   ACTIVE only

    @Operation(summary = "Search active hotels")
    @GetMapping("/api/hotels")
    public ResponseEntity<PagedResponse<HotelResponseDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String amenity,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) Boolean hasImage,
            @RequestParam(required = false) Boolean hasPhone,
            @RequestParam(required = false) Boolean hasWebsite,
            @RequestParam(required = false) Boolean hasEmail,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                PagedResponse.from(
                        hotelService.search(
                                q, city, country, Hotel.Status.ACTIVE, amenity,
                                minRating, maxRating,
                                hasImage, hasPhone, hasWebsite, hasEmail,
                                pageable
                        )
                )
        );
    }

    @Operation(summary = "Get active hotel by ID")
    @GetMapping("/api/hotels/{id}")
    public ResponseEntity<HotelResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getActiveById(id));
    }

    @Operation(summary = "Get active hotel cities")
    @GetMapping("/api/hotels/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(hotelService.getActiveCities());
    }

    @Operation(summary = "Get active hotel countries")
    @GetMapping("/api/hotels/countries")
    public ResponseEntity<List<String>> getCountries() {
        return ResponseEntity.ok(hotelService.getActiveCountries());
    }

    @Operation(summary = "Autocomplete hotel names")
    @GetMapping("/api/hotels/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        return ResponseEntity.ok(hotelService.autocompleteNames(q, limit));
    }

    // ADMIN / MANAGER  —  /api/admin/hotels/**   (all statuses)

    @Operation(summary = "Admin/Manager search hotels")
    @GetMapping("/api/admin/hotels")
    public ResponseEntity<PagedResponse<HotelResponseDto>> adminSearch(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Hotel.Status status,
            @RequestParam(required = false) String amenity,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) Boolean hasImage,
            @RequestParam(required = false) Boolean hasPhone,
            @RequestParam(required = false) Boolean hasWebsite,
            @RequestParam(required = false) Boolean hasEmail,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                PagedResponse.from(
                        hotelService.search(
                                q, city, country, status, amenity,
                                minRating, maxRating,
                                hasImage, hasPhone, hasWebsite, hasEmail,
                                pageable
                        )
                )
        );
    }

    @Operation(summary = "Admin/Manager get hotel by ID")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#id)")
    @GetMapping("/api/admin/hotels/{id}")
    public ResponseEntity<HotelResponseDto> adminGetById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getById(id));
    }

    @Operation(summary = "Admin/Manager get all cities")
    @GetMapping("/api/admin/hotels/cities")
    public ResponseEntity<List<String>> adminGetCities() {
        return ResponseEntity.ok(hotelService.getDistinctCities());
    }

    @Operation(summary = "Admin/Manager get all countries")
    @GetMapping("/api/admin/hotels/countries")
    public ResponseEntity<List<String>> adminGetCountries() {
        return ResponseEntity.ok(hotelService.getDistinctCountries());
    }

    @Operation(summary = "Create hotel")
    @ApiResponse(responseCode = "201", description = "Hotel created successfully")
    @PostMapping("/api/admin/hotels")
    public ResponseEntity<HotelResponseDto> create(@Valid @RequestBody HotelRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.create(dto));
    }

    @Operation(summary = "Update hotel")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#id)")
    @PutMapping("/api/admin/hotels/{id}")
    public ResponseEntity<HotelResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody HotelRequestDto dto
    ) {
        return ResponseEntity.ok(hotelService.update(id, dto));
    }

    @Operation(summary = "Partially update hotel")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#id)")
    @PatchMapping("/api/admin/hotels/{id}")
    public ResponseEntity<HotelResponseDto> patch(
            @PathVariable Long id,
            @Valid @RequestBody HotelPatchDto dto
    ) {
        return ResponseEntity.ok(hotelService.patch(id, dto));
    }

    @Operation(summary = "Delete hotel")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#id)")
    @DeleteMapping("/api/admin/hotels/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/hotels/{hotelId}/managers/{userId}")
    public ResponseEntity<Void> assignManager(
            @PathVariable Long hotelId,
            @PathVariable Long userId) {
        hotelService.assignManager(hotelId, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/hotels/{hotelId}/managers/{userId}")
    public ResponseEntity<Void> removeManager(
            @PathVariable Long hotelId,
            @PathVariable Long userId) {
        hotelService.removeManager(hotelId, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/hotels/{hotelId}/managers")
    public ResponseEntity<List<UserResponseDTO>> getManagers(
            @PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getManagers(hotelId));
    }

    @Operation(summary = "Upload hotel images")
    @ApiResponse(responseCode = "201", description = "Images uploaded successfully")

    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#id)")
    @PostMapping(value = "/api/admin/hotels/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<HotelImageResponseDto>> uploadHotelImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hotelService.uploadImages(id, files));
    }

    @Operation(summary = "Get hotel images")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#id)")
    @GetMapping("/api/admin/hotels/{id}/images")
    public ResponseEntity<List<HotelImageResponseDto>> getHotelImages(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(hotelService.getHotelImages(id));
    }

    @Operation(summary = "Delete hotel image")
    @PreAuthorize("hasRole('ADMIN') or @hotelAccessSecurityService.canAccessHotel(#hotelId)")
    @DeleteMapping("/api/admin/hotels/{hotelId}/images/{imageId}")
    public ResponseEntity<Void> deleteHotelImage(
            @PathVariable Long hotelId,
            @PathVariable Long imageId
    ) {
        hotelService.deleteHotelImage(hotelId, imageId);
        return ResponseEntity.noContent().build();
    }
}
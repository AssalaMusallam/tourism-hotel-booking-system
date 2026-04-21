package com.swer313.projectstep1.waitinglist;
import com.swer313.projectstep1.catalog.room.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.swer313.projectstep1.security.CurrentUserService;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController
@RequestMapping("/api/waiting-list")
@Validated
@Tag(name = "Waiting List",
        description = "Join waiting list for fully booked room types")
public class WaitingListController {

    private final WaitingListService waitingListService;
    private final CurrentUserService currentUserService;

    public WaitingListController(WaitingListService waitingListService,
                                 CurrentUserService currentUserService) {
        this.waitingListService = waitingListService;
        this.currentUserService = currentUserService;
    }

    // ── Admin endpoints ───────────────────────────────────────────────────────

    /**
     * TODO: بعد إضافة Auth — تحقق إن الـ admin مسؤول عن هذا الفندق فقط.
     * حالياً الـ endpoints مفتوحة لأن Auth ما بُني بعد.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")

    @GetMapping("/admin/room-types/{roomTypeId}/count")
    @Operation(summary = "Admin: Get waiting list count for a room type")
    public ResponseEntity<java.util.Map<String, Object>> getCount(
            @PathVariable Long roomTypeId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate checkIn,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate checkOut) {

        int count = waitingListService.countWaiting(roomTypeId, checkIn, checkOut);

        return ResponseEntity.ok(java.util.Map.of(
                "roomTypeId",    roomTypeId,
                "checkIn",       checkIn,
                "checkOut",      checkOut,
                "waitingCount",  count
        ));
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")

    @GetMapping("/admin/room-types/{roomTypeId}/waiting-list")
    @Operation(summary = "Admin: Get waiting list entries for a room type (masked emails)")
    public ResponseEntity<PagedResponse<WaitingListAdminDTO>> getAdminList(
            @PathVariable Long roomTypeId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                waitingListService.getWaitingListForAdmin(roomTypeId, pageable)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<WaitingListResponseDTO> join(
            @Valid @RequestBody WaitingListRequestDTO dto) {

        dto.setGuestEmail(currentUserService.getCurrentUserEmail());

        if (dto.getGuestName() == null || dto.getGuestName().isBlank()) {
            dto.setGuestName(currentUserService.getCurrentUser().getFullName());
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(waitingListService.joinWaitingList(dto));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        waitingListService.cancelEntry(id, currentUserService.getCurrentUserEmail());
        return ResponseEntity.noContent().build();
    }

    // GET /api/waiting-list/my?email=... — تسجيلاتي
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ResponseEntity<PagedResponse<WaitingListResponseDTO>> getMyEntries(
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(
                waitingListService.getMyEntries(
                        currentUserService.getCurrentUserEmail(),
                        pageable
                )
        );
    }
}
package com.mmva.newsapp.controller.admin.news;

import com.mmva.newsapp.domain.newssourceagency.dto.core.NewsSourceAgencyRequestDto;
import com.mmva.newsapp.domain.newssourceagency.dto.core.NewsSourceAgencyResponseDto;
import com.mmva.newsapp.infrastructure.security.util.SecurityContextUtils;
import com.mmva.newsapp.domain.newssourceagency.service.core.NewsSourceAgencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;

/**
 * Admin controller for managing news source agencies.
 * 
 * <h3>Authentication Required:</h3>
 * <p>
 * Requires admin authentication and NEWS_MANAGE permission.
 * </p>
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li>CRUD operations for source agencies</li>
 * <li>Toggle active/trusted status</li>
 * <li>List with filtering options</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/source-agencies")
@RequiredArgsConstructor
@Tag(name = "News Source Agencies (Admin)", description = "Admin operations for managing news source agencies")
@SecurityRequirement(name = "bearerAuth")
public class AdminNewsSourceAgencyController {

        private final NewsSourceAgencyService sourceAgencyService;

        // ========================================
        // CRUD Operations
        // ========================================

        @Operation(summary = "Create a new source agency", description = """
                        Creates a new newsapp source agency for content attribution.

                        **Required Fields:**
                        - `agencyCode`: Unique uppercase code (e.g., "REUTERS", "AP")
                        - `agencyName`: Display name of the agency

                        **Optional Fields:**
                        - `agencyLogoUrl`: URL to agency logo
                        - `agencyWebsiteUrl`: Agency website URL
                        - `isTrusted`: Whether agency is trusted (defaults to false)
                        - `isActive`: Whether agency is active (defaults to true)
                        - `description`: Agency description
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Agency created successfully", content = @Content(schema = @Schema(implementation = NewsSourceAgencyResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "409", description = "Agency with code already exists")
        })
        @PostMapping
        @PreAuthorize("hasAuthority('NEWS_MANAGE') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<NewsSourceAgencyResponseDto>> createSourceAgency(
                        @Valid @RequestBody NewsSourceAgencyRequestDto request) {

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                request.setAdminId(adminId);
                log.info("AdminNewsSourceAgencyController: Create source agency - code={}, name={}",
                                request.getAgencyCode(), request.getAgencyName());

                NewsSourceAgencyResponseDto response = sourceAgencyService.create(request);

                log.info("AdminNewsSourceAgencyController: Source agency created - agencyId={}",
                                response.getAgencyId());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Source agency created", response));
        }

        @Operation(summary = "Get source agency by ID", description = """
                        Retrieves details of a specific source agency by its UUID.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Agency found", content = @Content(schema = @Schema(implementation = NewsSourceAgencyResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Agency not found")
        })
        @GetMapping("/{agencyId}")
        @PreAuthorize("hasAuthority('NEWS_VIEW') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<NewsSourceAgencyResponseDto>> getSourceAgencyById(
                        @Parameter(description = "Agency UUID") @PathVariable String agencyId) {

                log.debug("AdminNewsSourceAgencyController: Get source agency - agencyId={}", agencyId);

                NewsSourceAgencyResponseDto response = sourceAgencyService.getById(UUID.fromString(agencyId));
                return ResponseEntity.ok(ApiResponseDto.success("Source agency retrieved", response));
        }

        @Operation(summary = "Get source agency by code", description = """
                        Retrieves details of a specific source agency by its unique code.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Agency found", content = @Content(schema = @Schema(implementation = NewsSourceAgencyResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Agency not found")
        })
        @GetMapping("/by-code/{agencyCode}")
        @PreAuthorize("hasAuthority('NEWS_VIEW') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<NewsSourceAgencyResponseDto>> getSourceAgencyByCode(
                        @Parameter(description = "Agency code (e.g., REUTERS, AP)") @PathVariable String agencyCode) {

                log.debug("AdminNewsSourceAgencyController: Get source agency by code - code={}", agencyCode);

                NewsSourceAgencyResponseDto response = sourceAgencyService.getByCode(agencyCode);
                return ResponseEntity.ok(ApiResponseDto.success("Source agency retrieved", response));
        }

        @Operation(summary = "Update source agency", description = """
                        Updates an existing source agency.
                        All fields in the request will overwrite existing values.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Agency updated successfully", content = @Content(schema = @Schema(implementation = NewsSourceAgencyResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Agency not found"),
                        @ApiResponse(responseCode = "409", description = "Agency code already exists")
        })
        @PutMapping("/{agencyId}")
        @PreAuthorize("hasAuthority('NEWS_MANAGE') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<NewsSourceAgencyResponseDto>> updateSourceAgency(
                        @Parameter(description = "Agency UUID") @PathVariable String agencyId,
                        @Valid @RequestBody NewsSourceAgencyRequestDto request) {

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                request.setAdminId(adminId);
                log.info("AdminNewsSourceAgencyController: Update source agency - agencyId={}", agencyId);

                NewsSourceAgencyResponseDto response = sourceAgencyService.update(UUID.fromString(agencyId), request);

                log.info("AdminNewsSourceAgencyController: Source agency updated - agencyId={}", agencyId);

                return ResponseEntity.ok(ApiResponseDto.success("Source agency updated", response));
        }

        @Operation(summary = "Delete source agency (soft delete)", description = """
                        Soft deletes a source agency. The agency will no longer appear in lists
                        but can be recovered if needed.

                        **Warning:** News articles referencing this agency will retain their
                        sourceAgencyId but the relationship will return null values.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Agency deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Agency not found")
        })
        @DeleteMapping("/{agencyId}")
        @PreAuthorize("hasAuthority('NEWS_MANAGE') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<Void>> deleteSourceAgency(
                        @Parameter(description = "Agency UUID") @PathVariable String agencyId) {

                log.info("AdminNewsSourceAgencyController: Delete source agency - agencyId={}", agencyId);

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                sourceAgencyService.delete(UUID.fromString(agencyId), adminId);

                log.info("AdminNewsSourceAgencyController: Source agency deleted - agencyId={}", agencyId);

                return ResponseEntity.ok(ApiResponseDto.success("Source agency deleted", null));
        }

        // ========================================
        // List & Filter Operations
        // ========================================

        @Operation(summary = "Get all source agencies", description = """
                        Retrieves a paginated list of all source agencies with optional filtering.

                        **Filter Options:**
                        - `activeOnly`: If true, returns only active agencies
                        - `trustedOnly`: If true, returns only trusted agencies
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Agencies retrieved successfully")
        })
        @GetMapping
        @PreAuthorize("hasAuthority('NEWS_VIEW') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<Page<NewsSourceAgencyResponseDto>>> getAllSourceAgencies(
                        @PageableDefault(size = 20, sort = "agencyName", direction = Sort.Direction.ASC) Pageable pageable) {

                log.debug("AdminNewsSourceAgencyController: Get all source agencies - page={}, size={}",
                                pageable.getPageNumber(), pageable.getPageSize());

                Page<NewsSourceAgencyResponseDto> response = sourceAgencyService.getAll(pageable);

                return ResponseEntity.ok(ApiResponseDto.success("Source agencies retrieved", response));
        }

        @Operation(summary = "Get active source agencies list (for dropdowns)", description = """
                        Returns a simple list of all active source agencies for UI dropdowns.
                        No pagination, returns all active agencies.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Active agencies retrieved")
        })
        @GetMapping("/active/list")
        @PreAuthorize("hasAuthority('NEWS_VIEW') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<List<NewsSourceAgencyResponseDto>>> getActiveSourceAgenciesList() {

                log.debug("AdminNewsSourceAgencyController: Get active source agencies list");

                List<NewsSourceAgencyResponseDto> response = sourceAgencyService.getAllEnabled();
                return ResponseEntity.ok(ApiResponseDto.success("Active agencies retrieved", response));
        }

        @Operation(summary = "Get trusted source agencies list", description = """
                        Returns a simple list of all trusted source agencies.
                        No pagination, returns all trusted agencies.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Trusted agencies retrieved")
        })
        @GetMapping("/trusted/list")
        @PreAuthorize("hasAuthority('NEWS_VIEW') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<List<NewsSourceAgencyResponseDto>>> getTrustedSourceAgenciesList() {

                log.debug("AdminNewsSourceAgencyController: Get trusted source agencies list");

                List<NewsSourceAgencyResponseDto> response = sourceAgencyService.getAllTrusted();
                return ResponseEntity.ok(ApiResponseDto.success("Trusted agencies retrieved", response));
        }

        // ========================================
        // Toggle Operations
        // ========================================

        @Operation(summary = "Toggle agency active status", description = """
                        Toggles the active status of a source agency.
                        Inactive agencies will not appear in public listings.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Status toggled successfully", content = @Content(schema = @Schema(implementation = NewsSourceAgencyResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Agency not found")
        })
        @PatchMapping("/{agencyId}/toggle-active")
        @PreAuthorize("hasAuthority('NEWS_MANAGE') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<NewsSourceAgencyResponseDto>> toggleActive(
                        @Parameter(description = "Agency UUID") @PathVariable String agencyId) {

                log.info("AdminNewsSourceAgencyController: Toggle active status - agencyId={}", agencyId);

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                NewsSourceAgencyResponseDto response = sourceAgencyService.toggleActive(UUID.fromString(agencyId),
                                adminId);

                log.info("AdminNewsSourceAgencyController: Active status toggled - agencyId={}, newStatus={}",
                                agencyId, response.getIsActive());

                return ResponseEntity.ok(ApiResponseDto.success("Active status toggled", response));
        }

        @Operation(summary = "Toggle agency trusted status", description = """
                        Toggles the trusted status of a source agency.
                        Trusted agencies may receive different display treatment in the UI.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Status toggled successfully", content = @Content(schema = @Schema(implementation = NewsSourceAgencyResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Agency not found")
        })
        @PatchMapping("/{agencyId}/toggle-trusted")
        @PreAuthorize("hasAuthority('NEWS_MANAGE') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<NewsSourceAgencyResponseDto>> toggleTrusted(
                        @Parameter(description = "Agency UUID") @PathVariable String agencyId) {

                log.info("AdminNewsSourceAgencyController: Toggle trusted status - agencyId={}", agencyId);

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                NewsSourceAgencyResponseDto response = sourceAgencyService.toggleTrusted(UUID.fromString(agencyId),
                                adminId);

                log.info("AdminNewsSourceAgencyController: Trusted status toggled - agencyId={}, newStatus={}",
                                agencyId, response.getIsTrusted());

                return ResponseEntity.ok(ApiResponseDto.success("Trusted status toggled", response));
        }

        // ========================================
        // Bulk Operations
        // ========================================

        @Operation(summary = "Bulk update agency statuses", description = """
                        Updates active/trusted status for multiple agencies at once.

                        **Request Body:**
                        ```json
                        {
                          "agencyIds": ["uuid1", "uuid2"],
                          "isActive": true,
                          "isTrusted": false
                        }
                        ```

                        Only provided fields will be updated.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Agencies updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request")
        })
        @PatchMapping("/bulk-status")
        @PreAuthorize("hasAuthority('NEWS_MANAGE') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> bulkUpdateStatus(
                        @RequestBody Map<String, Object> request) {

                @SuppressWarnings("unchecked")
                List<String> agencyIds = (List<String>) request.get("agencyIds");
                Boolean isActive = (Boolean) request.get("isActive");
                Boolean isTrusted = (Boolean) request.get("isTrusted");

                log.info("AdminNewsSourceAgencyController: Bulk update status - count={}, isActive={}, isTrusted={}",
                                agencyIds != null ? agencyIds.size() : 0, isActive, isTrusted);

                int updatedCount = 0;
                if (agencyIds != null) {
                        for (String agencyId : agencyIds) {
                                try {
                                        UUID id = UUID.fromString(agencyId);
                                        NewsSourceAgencyResponseDto agency = sourceAgencyService.getById(id);

                                        // Create update request with current values
                                        NewsSourceAgencyRequestDto updateRequest = NewsSourceAgencyRequestDto.builder()
                                                        .agencyCode(agency.getAgencyCode())
                                                        .agencyName(agency.getAgencyName())
                                                        .agencyLogoUrl(agency.getAgencyLogoUrl())
                                                        .agencyWebsiteUrl(agency.getAgencyWebsiteUrl())
                                                        .isTrusted(isTrusted != null ? isTrusted
                                                                        : agency.getIsTrusted())
                                                        .isActive(isActive != null ? isActive : agency.getIsActive())
                                                        .description(agency.getDescription())
                                                        .build();

                                        sourceAgencyService.update(id, updateRequest);
                                        updatedCount++;
                                } catch (Exception e) {
                                        log.warn("AdminNewsSourceAgencyController: Failed to update agency - agencyId={}, error={}",
                                                        agencyId, e.getMessage());
                                }
                        }
                }

                log.info("AdminNewsSourceAgencyController: Bulk update completed - updatedCount={}", updatedCount);

                return ResponseEntity.ok(ApiResponseDto.success("Bulk update completed", Map.of(
                                "updatedCount", updatedCount,
                                "requestedCount", agencyIds != null ? agencyIds.size() : 0)));
        }
}

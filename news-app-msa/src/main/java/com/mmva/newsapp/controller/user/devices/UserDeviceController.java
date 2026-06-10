package com.mmva.newsapp.controller.user.devices;

import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.dto.UserDeviceBlockRequestDto;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.dto.UserDeviceRenameRequestDto;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.dto.UserDeviceResponseDto;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.mapper.UserDeviceMapper;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.model.UserDevice;
import com.mmva.newsapp.infrastructure.clientcontext.core.service.ClientContextService;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.service.UserDeviceService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.security.userdetails.AppUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * User Device Controller.
 * 
 * <p>
 * Handles all device management operations for the authenticated user:
 * </p>
 * <ul>
 * <li>Device listing (active, blocked)</li>
 * <li>Device trust management (trust, untrust)</li>
 * <li>Device security (block, unblock, remove)</li>
 * <li>Device customization (rename)</li>
 * <li>Multi-device session management (logout others)</li>
 * </ul>
 * 
 * <p>
 * Path prefix: /api/v1/me/devices
 * </p>
 */
@CrossOrigin(origins = "*", allowedHeaders = { "Range", "Accept", "Content-Type", "Authorization" }, exposedHeaders = {
                "Content-Length", "Content-Range", "Accept-Ranges" })
@RestController
@RequestMapping("/api/v1/me/devices")
@Tag(name = "User Devices", description = "Device management and security for authenticated users")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserDeviceController {

        private final UserDeviceService userDeviceService;
        private final ClientContextService clientContextService;
        private final UserDeviceMapper userDeviceMapper;

        // ==========================================
        // DEVICE LISTING OPERATIONS (1-3)
        // ==========================================

        @GetMapping
        @Operation(summary = "1. List my devices", description = "Lists all devices associated with the current user's account")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Devices retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<List<UserDeviceResponseDto>>> getMyDevices(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Device [{}]: Fetching devices", userId);

                ClientContextDto context = clientContextService.getCurrentContext();
                String currentFingerprint = context != null ? context.deviceFingerprint() : null;

                List<UserDevice> devices = userDeviceService.getActiveDevices(userId);
                List<UserDeviceResponseDto> response = userDeviceMapper.toResponseDtoList(devices, currentFingerprint);

                log.debug("Device [{}]: Retrieved {} devices", userId, response.size());
                return ResponseEntity.ok(ApiResponseDto.success("Devices retrieved successfully", response));
        }

        @GetMapping("/{deviceId}")
        @Operation(summary = "2. Get device details", description = "Gets detailed information about a specific device")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device details retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        public ResponseEntity<ApiResponseDto<UserDeviceResponseDto>> getDeviceDetails(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Device ID", required = true) @PathVariable UUID deviceId) {
                UUID userId = userDetails.getUserId();
                log.debug("Device [{}]: Fetching device details for {}", userId, deviceId);

                ClientContextDto context = clientContextService.getCurrentContext();
                String currentFingerprint = context != null ? context.deviceFingerprint() : null;

                UserDevice device = userDeviceService.getDeviceById(deviceId, userId);
                UserDeviceResponseDto response = userDeviceMapper.toResponseDto(device, currentFingerprint);

                log.debug("Device [{}]: Device details retrieved for {}", userId, deviceId);
                return ResponseEntity.ok(ApiResponseDto.success("Device details retrieved", response));
        }

        @GetMapping("/blocked")
        @Operation(summary = "3. List blocked devices", description = "Lists all blocked devices for the account")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Blocked devices retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<List<UserDeviceResponseDto>>> getBlockedDevices(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Device [{}]: Fetching blocked devices", userId);

                List<UserDevice> devices = userDeviceService.getBlockedDevices(userId);
                List<UserDeviceResponseDto> response = userDeviceMapper.toResponseDtoList(devices);

                log.debug("Device [{}]: Retrieved {} blocked devices", userId, response.size());
                return ResponseEntity.ok(ApiResponseDto.success("Blocked devices retrieved", response));
        }

        // ==========================================
        // DEVICE TRUST OPERATIONS (4-5)
        // ==========================================

        @PostMapping("/{deviceId}/trust")
        @Operation(summary = "4. Trust a device", description = "Marks a device as trusted, skipping future security alerts for this device")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device trusted successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        public ResponseEntity<ApiResponseDto<UserDeviceResponseDto>> trustDevice(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Device ID to trust", required = true) @PathVariable UUID deviceId) {
                UUID userId = userDetails.getUserId();
                log.info("Device [{}]: Trusting device {}", userId, deviceId);

                UserDevice device = userDeviceService.trustDevice(userId, deviceId);
                UserDeviceResponseDto response = userDeviceMapper.toResponseDto(device);

                log.info("Device [{}]: Device {} trusted successfully", userId, deviceId);
                return ResponseEntity.ok(ApiResponseDto.success("Device trusted successfully", response));
        }

        @DeleteMapping("/{deviceId}/trust")
        @Operation(summary = "5. Untrust a device", description = "Removes trust from a device, enabling security alerts again")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device untrusted successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        public ResponseEntity<ApiResponseDto<UserDeviceResponseDto>> untrustDevice(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Device ID to untrust", required = true) @PathVariable UUID deviceId) {
                UUID userId = userDetails.getUserId();
                log.info("Device [{}]: Removing trust from device {}", userId, deviceId);

                UserDevice device = userDeviceService.untrustDevice(userId, deviceId);
                UserDeviceResponseDto response = userDeviceMapper.toResponseDto(device);

                log.info("Device [{}]: Device {} untrusted successfully", userId, deviceId);
                return ResponseEntity.ok(ApiResponseDto.success("Device untrusted successfully", response));
        }

        // ==========================================
        // DEVICE SECURITY OPERATIONS (6-8)
        // ==========================================

        @PostMapping("/{deviceId}/block")
        @Operation(summary = "6. Block a device", description = "Blocks a device from accessing the account (e.g., stolen/lost device)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device blocked successfully"),
                        @ApiResponse(responseCode = "400", description = "Cannot block current device"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        public ResponseEntity<ApiResponseDto<UserDeviceResponseDto>> blockDevice(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Device ID to block", required = true) @PathVariable UUID deviceId,
                        @Valid @RequestBody UserDeviceBlockRequestDto request) {
                UUID userId = userDetails.getUserId();
                log.warn("Device [{}]: Blocking device {} - reason: {}", userId, deviceId, request.getReason());

                // Prevent blocking current device
                ClientContextDto context = clientContextService.getCurrentContext();
                UserDevice targetDevice = userDeviceService.getDeviceById(deviceId, userId);
                if (context != null && context.deviceFingerprint() != null
                                && context.deviceFingerprint().equals(targetDevice.getDeviceFingerprint())) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponseDto.error("Cannot block the device you are currently using"));
                }

                UserDevice device = userDeviceService.blockDevice(userId, deviceId, request.getReason());
                UserDeviceResponseDto response = userDeviceMapper.toResponseDto(device);

                log.warn("Device [{}]: Device {} blocked successfully", userId, deviceId);
                return ResponseEntity.ok(ApiResponseDto.success("Device blocked successfully", response));
        }

        @DeleteMapping("/{deviceId}/block")
        @Operation(summary = "7. Unblock a device", description = "Unblocks a previously blocked device")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device unblocked successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        public ResponseEntity<ApiResponseDto<UserDeviceResponseDto>> unblockDevice(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Device ID to unblock", required = true) @PathVariable UUID deviceId) {
                UUID userId = userDetails.getUserId();
                log.info("Device [{}]: Unblocking device {}", userId, deviceId);

                UserDevice device = userDeviceService.unblockDevice(userId, deviceId);
                UserDeviceResponseDto response = userDeviceMapper.toResponseDto(device);

                log.info("Device [{}]: Device {} unblocked successfully", userId, deviceId);
                return ResponseEntity.ok(ApiResponseDto.success("Device unblocked successfully", response));
        }

        @DeleteMapping("/{deviceId}")
        @Operation(summary = "8. Remove a device", description = "Removes a device from the account (soft delete)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device removed successfully"),
                        @ApiResponse(responseCode = "400", description = "Cannot remove current device"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> removeDevice(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Device ID to remove", required = true) @PathVariable UUID deviceId) {
                UUID userId = userDetails.getUserId();
                log.info("Device [{}]: Removing device {}", userId, deviceId);

                // Prevent removing current device
                ClientContextDto context = clientContextService.getCurrentContext();
                UserDevice targetDevice = userDeviceService.getDeviceById(deviceId, userId);
                if (context != null && context.deviceFingerprint() != null
                                && context.deviceFingerprint().equals(targetDevice.getDeviceFingerprint())) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponseDto.error("Cannot remove the device you are currently using"));
                }

                userDeviceService.removeDevice(userId, deviceId);

                log.info("Device [{}]: Device {} removed successfully", userId, deviceId);
                return ResponseEntity.ok(ApiResponseDto.success("Device removed successfully", null));
        }

        // ==========================================
        // MULTI-DEVICE SESSION OPERATIONS (9)
        // ==========================================

        @PostMapping("/logout-others")
        @Operation(summary = "9. Logout from other devices", description = "Logs out from all devices except the current one")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Logged out from other devices"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Integer>> logoutOtherDevices(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.warn("Device [{}]: Logging out from all other devices", userId);

                ClientContextDto context = clientContextService.getCurrentContext();
                String currentFingerprint = context != null ? context.deviceFingerprint() : null;

                if (currentFingerprint == null) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponseDto.error("Cannot identify current device"));
                }

                int count = userDeviceService.logoutOtherDevices(userId, currentFingerprint);

                log.warn("Device [{}]: Logged out from {} other devices", userId, count);
                return ResponseEntity.ok(ApiResponseDto.success(
                                "Logged out from " + count + " other device(s)", count));
        }

        // ==========================================
        // DEVICE CUSTOMIZATION OPERATIONS (10)
        // ==========================================

        @PatchMapping("/{deviceId}/name")
        @Operation(summary = "10. Rename a device", description = "Sets a custom name for a device")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device renamed successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        public ResponseEntity<ApiResponseDto<UserDeviceResponseDto>> renameDevice(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Device ID to rename", required = true) @PathVariable UUID deviceId,
                        @Valid @RequestBody UserDeviceRenameRequestDto request) {
                UUID userId = userDetails.getUserId();
                log.info("Device [{}]: Renaming device {} to '{}'", userId, deviceId, request.getDeviceName());

                UserDevice device = userDeviceService.renameDevice(userId, deviceId, request.getDeviceName());
                UserDeviceResponseDto response = userDeviceMapper.toResponseDto(device);

                log.info("Device [{}]: Device {} renamed successfully", userId, deviceId);
                return ResponseEntity.ok(ApiResponseDto.success("Device renamed successfully", response));
        }
}

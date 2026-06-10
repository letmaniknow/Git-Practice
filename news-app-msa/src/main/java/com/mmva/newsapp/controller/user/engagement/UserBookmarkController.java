package com.mmva.newsapp.controller.user.engagement;

import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkRequestDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkResponseDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkUpdateDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.service.NewsBookmarkService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.security.userdetails.AppUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * User Bookmark Controller.
 * 
 * <p>
 * Handles all bookmark operations for the authenticated user:
 * </p>
 * <ul>
 * <li>Bookmark management (add, update, remove)</li>
 * <li>Folder management (create, rename, delete)</li>
 * <li>Bookmark queries (list, check status)</li>
 * </ul>
 * 
 * <p>
 * Path prefix: /api/v1/me/bookmarks
 * </p>
 */
@CrossOrigin(origins = "*", allowedHeaders = { "Range", "Accept", "Content-Type", "Authorization" }, exposedHeaders = {
                "Content-Length", "Content-Range", "Accept-Ranges" })
@RestController
@RequestMapping("/api/v1/me/bookmarks")
@Tag(name = "User Bookmarks", description = "Bookmark and folder management for authenticated users")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserBookmarkController {

        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_SIZE = "10";
        private static final int MAX_PAGE_SIZE = 100;

        private final NewsBookmarkService bookmarkService;

        // ==========================================
        // BOOKMARK OPERATIONS (1-6)
        // ==========================================

        @GetMapping
        @Operation(summary = "1. Get my bookmarks", description = "Retrieves all bookmarks for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookmarks retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<List<NewsBookmarkResponseDto>>> getMyBookmarks(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Bookmark [{}]: Fetching all bookmarks", userId);
                List<NewsBookmarkResponseDto> bookmarks = bookmarkService.getBookmarksByUserId(userId);
                log.debug("Bookmark [{}]: Retrieved {} bookmarks", userId, bookmarks.size());
                return ResponseEntity.ok(ApiResponseDto.success("Bookmarks retrieved successfully", bookmarks));
        }

        @GetMapping("/paged")
        @Operation(summary = "2. Get my bookmarks (paged)", description = "Retrieves paginated bookmarks for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Paged bookmarks retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsBookmarkResponseDto>>> getMyBookmarksPaged(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID userId = userDetails.getUserId();
                log.debug("Bookmark [{}]: Fetching paged bookmarks - page: {}, size: {}", userId, page, size);
                Pageable pageable = PageRequest.of(page, size);
                Page<NewsBookmarkResponseDto> bookmarks = bookmarkService.getAllBookmarks(pageable);
                log.debug("Bookmark [{}]: Retrieved {} bookmarks on page {}", userId, bookmarks.getNumberOfElements(),
                                page);
                return ResponseEntity.ok(ApiResponseDto.success("Paged bookmarks retrieved successfully", bookmarks));
        }

        @PostMapping
        @Operation(summary = "3. Add a bookmark", description = "Adds a news article to the user's bookmarks")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Bookmark added"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "409", description = "Bookmark already exists")
        })
        public ResponseEntity<ApiResponseDto<NewsBookmarkResponseDto>> addBookmark(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Valid @RequestBody NewsBookmarkRequestDto dto) {
                UUID userId = userDetails.getUserId();
                log.info("Bookmark [{}]: Adding bookmark for news: {}", userId, dto.getNewsBookmarksNewsId());
                dto.setNewsBookmarksUserId(userId);
                NewsBookmarkResponseDto response = bookmarkService.addBookmark(dto);
                log.info("Bookmark [{}]: Bookmark added for news: {}", userId, dto.getNewsBookmarksNewsId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Bookmark added successfully", response));
        }

        @PutMapping("/{bookmarkId}")
        @Operation(summary = "4. Update a bookmark", description = "Updates a bookmark (e.g., change folder)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookmark updated"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Bookmark not found")
        })
        public ResponseEntity<ApiResponseDto<NewsBookmarkResponseDto>> updateMyBookmark(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Bookmark ID to update", required = true) @PathVariable UUID bookmarkId,
                        @Valid @RequestBody NewsBookmarkUpdateDto dto) {
                UUID userId = userDetails.getUserId();
                log.info("Bookmark [{}]: Updating bookmark: {}", userId, bookmarkId);
                NewsBookmarkResponseDto response = bookmarkService.updateBookmark(bookmarkId, dto);
                log.info("Bookmark [{}]: Bookmark updated: {}", userId, bookmarkId);
                return ResponseEntity.ok(ApiResponseDto.success("Bookmark updated successfully", response));
        }

        @DeleteMapping("/{newsId}")
        @Operation(summary = "5. Remove a bookmark", description = "Removes a news article from the user's bookmarks")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookmark removed"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Bookmark not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> removeMyBookmark(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "News ID to unbookmark", required = true) @PathVariable UUID newsId) {
                UUID userId = userDetails.getUserId();
                log.info("Bookmark [{}]: Removing bookmark for news: {}", userId, newsId);
                bookmarkService.removeBookmarkByUser(newsId, userId);
                log.info("Bookmark [{}]: Bookmark removed for news: {}", userId, newsId);
                return ResponseEntity.ok(ApiResponseDto.success("Bookmark removed successfully", null));
        }

        @GetMapping("/check/{newsId}")
        @Operation(summary = "6. Check if news is bookmarked", description = "Checks if a news article is bookmarked by the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookmark status retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Boolean>> isNewsBookmarked(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "News ID to check", required = true) @PathVariable UUID newsId) {
                UUID userId = userDetails.getUserId();
                log.debug("Bookmark [{}]: Checking bookmark status for news: {}", userId, newsId);
                boolean bookmarked = bookmarkService.isBookmarked(userId, newsId);
                log.debug("Bookmark [{}]: News {} bookmarked: {}", userId, newsId, bookmarked);
                return ResponseEntity.ok(ApiResponseDto.success("Bookmark status retrieved", bookmarked));
        }

        // ==========================================
        // FOLDER OPERATIONS (7-11)
        // ==========================================

        @GetMapping("/folder/{folderName}")
        @Operation(summary = "7. Get bookmarks by folder", description = "Retrieves bookmarks in a specific folder")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookmarks retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<List<NewsBookmarkResponseDto>>> getMyBookmarksByFolder(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Folder name", required = true) @PathVariable String folderName) {
                UUID userId = userDetails.getUserId();
                log.debug("Bookmark [{}]: Fetching bookmarks in folder: {}", userId, folderName);
                List<NewsBookmarkResponseDto> bookmarks = bookmarkService.getBookmarksByUserIdAndFolder(userId,
                                folderName);
                log.debug("Bookmark [{}]: Retrieved {} bookmarks from folder: {}", userId, bookmarks.size(),
                                folderName);
                return ResponseEntity.ok(ApiResponseDto.success("Bookmarks retrieved successfully", bookmarks));
        }

        @GetMapping("/folders")
        @Operation(summary = "8. Get my bookmark folders", description = "Lists all bookmark folders for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Folders retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<List<String>>> getMyBookmarkFolders(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Bookmark [{}]: Fetching bookmark folders", userId);
                List<String> folders = bookmarkService.getFoldersByUserId(userId);
                log.debug("Bookmark [{}]: Retrieved {} folders", userId, folders.size());
                return ResponseEntity.ok(ApiResponseDto.success("Folders retrieved successfully", folders));
        }

        @PostMapping("/folders")
        @Operation(summary = "9. Create a bookmark folder", description = "Creates a new bookmark folder for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Folder created"),
                        @ApiResponse(responseCode = "400", description = "Invalid folder name"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "409", description = "Folder already exists")
        })
        public ResponseEntity<ApiResponseDto<String>> createBookmarkFolder(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Name of the folder to create", required = true) @RequestParam String folderName) {
                UUID userId = userDetails.getUserId();
                log.info("Bookmark [{}]: Creating bookmark folder: {}", userId, folderName);
                String createdFolder = bookmarkService.createFolderForUser(userId, folderName);
                log.info("Bookmark [{}]: Bookmark folder created: {}", userId, createdFolder);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Folder created successfully", createdFolder));
        }

        @PutMapping("/folders/rename")
        @Operation(summary = "10. Rename a bookmark folder", description = "Renames an existing bookmark folder for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Folder renamed"),
                        @ApiResponse(responseCode = "400", description = "Invalid folder name"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Folder not found")
        })
        public ResponseEntity<ApiResponseDto<String>> renameBookmarkFolder(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Current folder name", required = true) @RequestParam String oldFolderName,
                        @Parameter(description = "New folder name", required = true) @RequestParam String newFolderName) {
                UUID userId = userDetails.getUserId();
                log.info("Bookmark [{}]: Renaming folder '{}' to '{}'", userId, oldFolderName, newFolderName);
                String renamedFolder = bookmarkService.renameFolderForUser(userId, oldFolderName, newFolderName);
                log.info("Bookmark [{}]: Folder renamed from '{}' to '{}'", userId, oldFolderName, newFolderName);
                return ResponseEntity.ok(ApiResponseDto.success("Folder renamed successfully", renamedFolder));
        }

        @DeleteMapping("/folders")
        @Operation(summary = "11. Delete a bookmark folder", description = "Deletes a bookmark folder and all bookmarks in it")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Folder deleted"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Folder not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> deleteBookmarkFolder(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Folder name to delete", required = true) @RequestParam String folderName) {
                UUID userId = userDetails.getUserId();
                log.info("Bookmark [{}]: Deleting bookmark folder: {}", userId, folderName);
                bookmarkService.deleteFolderForUser(userId, folderName);
                log.info("Bookmark [{}]: Bookmark folder deleted: {}", userId, folderName);
                return ResponseEntity.ok(ApiResponseDto.success("Folder deleted successfully", null));
        }
}

package com.mmva.newsapp.domain.newscategory.dto.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for news category audit log responses.
 * Used to return audit history via API endpoints.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsCategoryAuditLogDto {

    @Schema(description = "Unique identifier of the audit log entry", example = "1")
    private Long id;

    @Schema(description = "UUID of the category this log belongs to", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID categoryId;

    @Schema(description = "Action performed (CREATE, UPDATE, DELETE, RESTORE)", example = "UPDATE")
    private String action;

    @Schema(description = "Human-readable description of the action", example = "Category updated")
    private String actionDescription;

    @Schema(description = "Additional details about the action", example = "Category name changed from 'Sports' to 'Sports & Games'")
    private String details;

    @Schema(description = "UUID of the admin who performed the action", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID createdBy;

    @Schema(description = "Timestamp when the action occurred (ISO 8601)", example = "2025-12-14T12:00:00Z")
    private String createdAt;
}

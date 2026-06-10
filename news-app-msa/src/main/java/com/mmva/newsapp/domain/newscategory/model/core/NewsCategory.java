package com.mmva.newsapp.domain.newscategory.model.core;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.domain.newscategory.enums.NewsCategoryStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * News newscategory entity for organizing newsapp articles.
 * 
 * <p>
 * Table: news_categories
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * <p>
 * Supports bilingual newscategory names (English/Spanish) with SEO-friendly
 * slugs.
 * </p>
 * 
 * <p>
 * Soft-delete filtering is handled via {@code SoftDeleteSpec} in repository
 * queries.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "news_categories", indexes = {
        @Index(name = "idx_news_categories_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_news_categories_slug", columnList = "news_categories_slug")
})
public class NewsCategory extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "news_categories_id")
    private UUID newsCategoriesId;

    // ========================================
    // URL & Identification
    // ========================================

    /**
     * Auto-generated from nameEn via SlugUtils.
     * Non-unique since nameEn is unique → slug will naturally be unique.
     * Kept as index for fast URL lookups.
     */
    @Column(name = "news_categories_slug", nullable = false, length = 255)
    private String newsCategoriesSlug;

    // ========================================
    // Category Names (Bilingual)
    // ========================================

    @Column(name = "news_categories_name_en", nullable = false, unique = true, length = 255)
    private String newsCategoriesNameEn;

    @Column(name = "news_categories_name_es", nullable = false, unique = true, length = 255)
    private String newsCategoriesNameEs;

    // ========================================
    // Description
    // ========================================

    @Column(name = "news_categories_description")
    private String newsCategoriesDescription;

    // ========================================
    // Status
    // ========================================

    @Column(name = "news_categories_status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NewsCategoryStatus status = NewsCategoryStatus.ACTIVE;
}

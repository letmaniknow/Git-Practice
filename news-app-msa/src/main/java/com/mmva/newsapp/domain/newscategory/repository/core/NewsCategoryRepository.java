package com.mmva.newsapp.domain.newscategory.repository.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mmva.newsapp.domain.newscategory.model.core.NewsCategory;

import java.util.UUID;

@Repository
public interface NewsCategoryRepository
        extends JpaRepository<NewsCategory, UUID>, JpaSpecificationExecutor<NewsCategory> {

    /**
     * Check if slug exists (includes soft-deleted).
     */
    boolean existsByNewsCategoriesSlug(String newsCategoriesSlug);

    /**
     * Check if active (non-deleted) newscategory exists with given slug.
     * Use for uniqueness validation.
     */
    @Query("SELECT COUNT(c) > 0 FROM NewsCategory c WHERE c.deletedAt IS NULL AND c.newsCategoriesSlug = :slug")
    boolean existsActiveBySlug(@Param("slug") String slug);
}

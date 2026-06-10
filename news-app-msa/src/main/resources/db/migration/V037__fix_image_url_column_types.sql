-- ============================================================
-- V037: Fix Image URL Column Types
-- ============================================================
-- This migration fixes the column types for image URL fields
-- to match JPA entity expectations (VARCHAR(255) instead of NVARCHAR(500)).
--
-- The V036 migration created columns as NVARCHAR(500), but JPA expects
-- VARCHAR(255) for String fields without @Lob. This migration alters
-- the existing columns to the correct type.
--
-- Note: Indexes must be dropped and recreated due to SQL Server limitations.
-- ============================================================

-- Drop existing indexes before altering columns
DROP INDEX IF EXISTS idx_news_image_small_url ON news;
DROP INDEX IF EXISTS idx_news_image_medium_url ON news;
DROP INDEX IF EXISTS idx_news_image_large_url ON news;
GO

-- Alter column types to match JPA expectations
ALTER TABLE news ALTER COLUMN news_image_small_url VARCHAR(255) NULL;
ALTER TABLE news ALTER COLUMN news_image_medium_url VARCHAR(255) NULL;
ALTER TABLE news ALTER COLUMN news_image_large_url VARCHAR(255) NULL;
GO

-- Recreate indexes after altering columns
CREATE INDEX idx_news_image_small_url ON news (news_image_small_url);
CREATE INDEX idx_news_image_medium_url ON news (news_image_medium_url);
CREATE INDEX idx_news_image_large_url ON news (news_image_large_url);
GO

-- Update extended properties for documentation
EXEC sp_updateextendedproperty
    @name = N'MS_Description',
    @value = N'URL for small optimized images (512x256) - ideal for mobile compact views',
    @level0type = N'Schema', @level0name = 'dbo',
    @level1type = N'Table',  @level1name = 'news',
    @level2type = N'Column', @level2name = 'news_image_small_url';
GO

EXEC sp_updateextendedproperty
    @name = N'MS_Description',
    @value = N'URL for medium optimized images (1024x512) - standard web display size',
    @level0type = N'Schema', @level0name = 'dbo',
    @level1type = N'Table',  @level1name = 'news',
    @level2type = N'Column', @level2name = 'news_image_medium_url';
GO

EXEC sp_updateextendedproperty
    @name = N'MS_Description',
    @value = N'URL for large high-resolution images (2048x1024) - full quality display',
    @level0type = N'Schema', @level0name = 'dbo',
    @level1type = N'Table',  @level1name = 'news',
    @level2type = N'Column', @level2name = 'news_image_large_url';
GO
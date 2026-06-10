-- ============================================================
-- MANUAL V038: Rename News Image Columns and Remove Avatar
-- ============================================================
-- This is a MANUAL migration script for renaming image URL columns
-- and removing the avatar column from the news table.
--
-- Execute this script manually in production databases.
-- DO NOT use with Flyway - this is for manual execution only.
--
-- Changes:
-- - news_image_small_url → news_image_card_url (400x225 - 16:9 card images)
-- - news_image_medium_url → news_image_hero_url (800x450 - 16:9 hero images)
-- - DROP news_image_large_url (avatar functionality moved to users domain)
--
-- Note: Indexes must be dropped and recreated due to column renames.
-- ============================================================

-- Drop existing indexes before renaming columns
DROP INDEX IF EXISTS idx_news_image_small_url ON news;
DROP INDEX IF EXISTS idx_news_image_medium_url ON news;
DROP INDEX IF EXISTS idx_news_image_large_url ON news;
GO

-- Rename columns to more descriptive names
IF EXISTS (SELECT 1 FROM sys.columns c JOIN sys.tables t ON c.object_id = t.object_id WHERE t.name = 'news' AND c.name = 'news_image_small_url')
BEGIN
    EXEC sp_rename 'news.news_image_small_url', 'news_image_card_url', 'COLUMN';
END

IF EXISTS (SELECT 1 FROM sys.columns c JOIN sys.tables t ON c.object_id = t.object_id WHERE t.name = 'news' AND c.name = 'news_image_medium_url')
BEGIN
    EXEC sp_rename 'news.news_image_medium_url', 'news_image_hero_url', 'COLUMN';
END
GO

-- Drop all constraints and dependencies on news_image_large_url before dropping the column
DECLARE @constraint_name NVARCHAR(256)
DECLARE @sql NVARCHAR(MAX)

-- Drop foreign key constraints
DECLARE constraint_cursor CURSOR FOR
SELECT CONSTRAINT_NAME
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_NAME = 'news'
AND CONSTRAINT_TYPE = 'FOREIGN KEY'

OPEN constraint_cursor
FETCH NEXT FROM constraint_cursor INTO @constraint_name

WHILE @@FETCH_STATUS = 0
BEGIN
    -- Check if constraint references the column we're dropping
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
        WHERE CONSTRAINT_NAME = @constraint_name
        AND COLUMN_NAME = 'news_image_large_url'
    )
    BEGIN
        SET @sql = 'ALTER TABLE news DROP CONSTRAINT ' + QUOTENAME(@constraint_name)
        EXEC sp_executesql @sql
        PRINT 'Dropped constraint: ' + @constraint_name
    END

    FETCH NEXT FROM constraint_cursor INTO @constraint_name
END

CLOSE constraint_cursor
DEALLOCATE constraint_cursor

-- Drop default constraints
DECLARE default_cursor CURSOR FOR
SELECT dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON dc.parent_column_id = c.column_id
JOIN sys.tables t ON dc.parent_object_id = t.object_id
WHERE t.name = 'news'
AND c.name = 'news_image_large_url'

OPEN default_cursor
FETCH NEXT FROM default_cursor INTO @constraint_name

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @sql = 'ALTER TABLE news DROP CONSTRAINT ' + QUOTENAME(@constraint_name)
    EXEC sp_executesql @sql
    PRINT 'Dropped default constraint: ' + @constraint_name

    FETCH NEXT FROM default_cursor INTO @constraint_name
END

CLOSE default_cursor
DEALLOCATE default_cursor

-- Drop check constraints
DECLARE check_cursor CURSOR FOR
SELECT CONSTRAINT_NAME
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_NAME = 'news'
AND CONSTRAINT_TYPE = 'CHECK'

OPEN check_cursor
FETCH NEXT FROM check_cursor INTO @constraint_name

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @sql = 'ALTER TABLE news DROP CONSTRAINT ' + QUOTENAME(@constraint_name)
    EXEC sp_executesql @sql
    PRINT 'Dropped check constraint: ' + @constraint_name

    FETCH NEXT FROM check_cursor INTO @constraint_name
END

CLOSE check_cursor
DEALLOCATE check_cursor

-- Now drop the avatar column (no longer needed for news)
IF EXISTS (SELECT 1 FROM sys.columns c JOIN sys.tables t ON c.object_id = t.object_id WHERE t.name = 'news' AND c.name = 'news_image_large_url')
BEGIN
    ALTER TABLE news DROP COLUMN news_image_large_url;
END
GO

-- Recreate indexes with new column names
DROP INDEX IF EXISTS idx_news_image_card_url ON news;
DROP INDEX IF EXISTS idx_news_image_hero_url ON news;
CREATE INDEX idx_news_image_card_url ON news (news_image_card_url);
CREATE INDEX idx_news_image_hero_url ON news (news_image_hero_url);
GO

-- Update extended properties for documentation
EXEC sp_updateextendedproperty
    @name = N'MS_Description',
    @value = N'URL for card optimized images (400x225 - 16:9) - mobile compact views',
    @level0type = N'Schema', @level0name = 'dbo',
    @level1type = N'Table',  @level1name = 'news',
    @level2type = N'Column', @level2name = 'news_image_card_url';
GO

EXEC sp_updateextendedproperty
    @name = N'MS_Description',
    @value = N'URL for hero optimized images (800x450 - 16:9) - web display size',
    @level0type = N'Schema', @level0name = 'dbo',
    @level1type = N'Table',  @level1name = 'news',
    @level2type = N'Column', @level2name = 'news_image_hero_url';
GO
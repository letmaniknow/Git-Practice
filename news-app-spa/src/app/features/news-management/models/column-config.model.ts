  /**
   * IMPORTANT: Only canonical/current field names from the backend/data model are used in this file.
   * Legacy/alternate field support is intentionally omitted for clarity and maintainability.
   * When adding new columns, update both this config and the grid template, and provide name resolution if needed.
   */
  // GROUP 9: AUDIT - PUBLISHED BY (OPTIONAL)
  // Place this inside DEFAULT_COLUMN_DEFINITIONS array after createdBy
/**
 * Column Configuration Model
 * 
 * Defines the columns available in the news management data table
 * and tracks user preferences for visibility and ordering.
 */

export interface ColumnDefinition {
  /** Unique identifier for the column */
  id: string;

  /** Display label in the UI */
  label: string;

  /** Property path in the NewsItem object (supports nested props: 'news.title') */
  propertyPath: string;

  /** Whether this column is visible by default */
  defaultVisible: boolean;

  /** Whether user can hide this column */
  hideable: boolean;

  /** Column width (if applicable) */
  width?: string;

  /** Column data type for formatting */
  dataType?: 'string' | 'date' | 'number' | 'boolean' | 'enum';

  /** For enum types, the enum values map */
  enumMap?: { [key: string]: string };

  /** Column importance: 'critical' (always show), 'important', 'optional' */
  importance?: 'critical' | 'important' | 'optional';

  /** Custom sort order (1-based, null = not sortable) */
  sortOrder?: number;

  /** Icon to display in column header */
  icon?: string;

  /** Tooltip for column header */
  tooltip?: string;

  /** Whether to enable text truncation */
  truncateText?: boolean;
}

export interface ColumnPreferences {
  /** Columns currently visible */
  visibleColumnIds: string[];

  /** Order of visible columns */
  columnOrder: string[];

  /** Last modified timestamp */
  lastModified: Date;

  /** User ID who modified (optional, for multi-user scenarios) */
  userId?: string;

  /** Version for migration purposes */
  version: number;
}

/**
 * Default column configurations for News Management
 * 
 * MONETIZATION-FIRST STRATEGY (Revenue > Editorial):
 * GROUP 1: Selection & Identity (Checkbox, Thumbnail, Title, Category)
 * GROUP 2: Status & Publishing (Status, Published, Scheduled, Created By)
 * GROUP 3: Monetization (Sponsored 📢, Premium 🔐) - CRITICAL FOR REVENUE
 * GROUP 4: Engagement & Operations (Views, Actions)
 * GROUP 5: Optional/Hidden (Featured, Active, Likes, Comments, etc)
 * 
 * DEFAULT VISIBLE (11 columns): Checkbox, Thumbnail, Title, Category, Status,
 * Published, Scheduled, Created By, Sponsored, Premium, Views, Actions
 */
export const DEFAULT_COLUMN_DEFINITIONS: ColumnDefinition[] = [
  // UI-only: Checkbox for selection
  {
    id: 'checkbox',
    label: '',
    propertyPath: '',
    defaultVisible: true,
    hideable: false,
    width: '44px',
    dataType: 'boolean',
    importance: 'critical',
    sortOrder: 0,
    icon: '',
    tooltip: 'Select row',
  },
  // Thumbnail (newsThumbnailUrl)
  {
    id: 'thumbnail',
    label: 'Thumbnail',
    propertyPath: 'newsThumbnailUrl',
    defaultVisible: true,
    hideable: true,
    width: '60px',
    dataType: 'string',
    importance: 'important',
    sortOrder: 1,
    icon: 'image',
    tooltip: 'Article thumbnail preview (60x60px)',
  },
  // Title (newsTitleEn)
  {
    id: 'newsTitleEn',
    label: 'Title',
    propertyPath: 'newsTitleEn',
    defaultVisible: true,
    hideable: false,
    width: 'minmax(220px, 2fr)',
    dataType: 'string',
    importance: 'critical',
    sortOrder: 2,
    icon: 'title',
    tooltip: 'News title (English)',
    truncateText: true,
  },
  // Category (newsNewsCategoryId)
  {
    id: 'newsNewsCategoryId',
    label: 'Category',
    propertyPath: 'newsNewsCategoryId',
    defaultVisible: true,
    hideable: true,
    width: 'minmax(120px, 1fr)',
    dataType: 'string',
    importance: 'important',
    sortOrder: 3,
    icon: 'category',
    tooltip: 'News category / section',
  },
  // Status (newsWorkflowStatus)
  {
    id: 'newsWorkflowStatus',
    label: 'Status',
    propertyPath: 'newsWorkflowStatus',
    defaultVisible: true,
    hideable: true,
    width: 'minmax(110px, 1fr)',
    dataType: 'enum',
    importance: 'important',
    sortOrder: 4,
    icon: 'published_with_changes',
    tooltip: 'Publication workflow status',
    enumMap: {
      DRAFT: 'Draft',
      PUBLISHED: 'Published',
      SCHEDULED: 'Scheduled',
      ARCHIVED: 'Archived',
    },
  },

  // ===================== AUDIT FIELDS GROUP =====================
  {
    id: 'createdBy',
    label: 'Created By',
    propertyPath: 'createdBy',
    defaultVisible: true,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'string',
    importance: 'important',
    sortOrder: 7,
    icon: 'person',
    tooltip: 'Created By (audit: createdBy)',
  },
  {
    id: 'newsCreatedAt',
    label: 'Created At',
    propertyPath: 'createdAt',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(120px, 1fr)',
    dataType: 'date',
    importance: 'optional',
    sortOrder: 70,
    icon: 'add_circle',
    tooltip: 'News creation date (audit trail)',
  },
  {
    id: 'newsUpdatedBy',
    label: 'Updated By',
    propertyPath: 'newsUpdatedBy',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'string',
    importance: 'optional',
    sortOrder: 72,
    icon: 'person',
    tooltip: 'User who last updated (audit)',
  },
  {
    id: 'updatedAt',
    label: 'Updated At',
    propertyPath: 'updatedAt',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(120px, 1fr)',
    dataType: 'date',
    importance: 'optional',
    sortOrder: 71,
    icon: 'update',
    tooltip: 'Last update date (audit trail)',
  },
  {
    id: 'newsPublishedBy',
    label: 'Published By',
    propertyPath: 'newsPublishedBy',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'string',
    importance: 'optional',
    sortOrder: 62,
    icon: 'person',
    tooltip: 'User who published (audit)',
  },
  {
    id: 'newsPublishedAt',
    label: 'Published At',
    propertyPath: 'newsPublishedAt',
    defaultVisible: true,
    hideable: true,
    width: 'minmax(120px, 1fr)',
    dataType: 'date',
    importance: 'important',
    sortOrder: 5,
    icon: 'calendar_today',
    tooltip: 'Publication date',
  },
  {
    id: 'newsScheduledBy',
    label: 'Scheduled By',
    propertyPath: 'newsScheduledBy',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'string',
    importance: 'optional',
    sortOrder: 73,
    icon: 'person',
    tooltip: 'User who scheduled (audit)',
  },
  {
    id: 'newsScheduledPublishAt',
    label: 'Scheduled At',
    propertyPath: 'newsScheduledPublishAt',
    defaultVisible: true,
    hideable: true,
    width: 'minmax(120px, 1fr)',
    dataType: 'date',
    importance: 'important',
    sortOrder: 6,
    icon: 'schedule_send',
    tooltip: 'Scheduled publish date',
  },
  {
    id: 'newsDeletedBy',
    label: 'Deleted By',
    propertyPath: 'newsDeletedBy',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'string',
    importance: 'optional',
    sortOrder: 61,
    icon: 'person_off',
    tooltip: 'User who deleted (audit)',
  },
  {
    id: 'newsDeletedAt',
    label: 'Deleted At',
    propertyPath: 'newsDeletedAt',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(120px, 1fr)',
    dataType: 'date',
    importance: 'optional',
    sortOrder: 60,
    icon: 'delete',
    tooltip: 'Soft delete timestamp (audit)',
  },
  // =================== END AUDIT FIELDS GROUP ===================
  // ...existing code...
  {
    id: 'featured',
    label: 'Featured',
    propertyPath: 'newsIsFeatured',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(70px, 1fr)',
    dataType: 'boolean',
    importance: 'optional',
    sortOrder: 20,
    icon: 'star',
    tooltip: 'Featured news indicator (editorial - optional)',
  },


  // GROUP 3: MONETIZATION (REVENUE CRITICAL!)
  {
    id: 'sponsored',
    label: 'Sponsored',
    propertyPath: 'newsIsSponsored',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(70px, 1fr)',
    dataType: 'boolean',
    importance: 'important',
    sortOrder: 8,
    icon: 'local_offer',
    tooltip: 'Sponsored content (brand partnership)',
  },
  {
    id: 'premium',
    label: 'Premium',
    propertyPath: 'newsIsPremium',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(70px, 1fr)',
    dataType: 'boolean',
    importance: 'important',
    sortOrder: 9,
    icon: 'lock',
    tooltip: 'Premium subscription content (paywall)',
  },

  // GROUP 4: ENGAGEMENT METRICS
  {
    id: 'views',
    label: 'Views',
    propertyPath: 'newsViewCount',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(70px, 1fr)',
    dataType: 'number',
    importance: 'important',
    sortOrder: 10,
    icon: 'visibility',
    tooltip: 'Total news views (engagement proof)',
  },
  {
    id: 'likes',
    label: 'Likes',
    propertyPath: 'newsLikeCount',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(70px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 30,
    icon: 'thumb_up',
    tooltip: 'User likes count',
  },
  {
    id: 'comments',
    label: 'Comments',
    propertyPath: 'newsCommentCount',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(90px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 31,
    icon: 'comment',
    tooltip: 'User comments count',
  },
  {
    id: 'shares',
    label: 'Shares',
    propertyPath: 'newsShareCount',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(80px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 32,
    icon: 'share',
    tooltip: 'Times news shared',
  },
  {
    id: 'bookmarks',
    label: 'Bookmarks',
    propertyPath: 'newsBookmarkCount',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 33,
    icon: 'bookmark',
    tooltip: 'Bookmark count',
  },

  // GROUP 5: CONTENT METADATA (OPTIONAL)
  {
    id: 'newsWordCount',
    label: 'Words',
    propertyPath: 'newsWordCount',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 40,
    icon: 'format_size',
    tooltip: 'Word count',
  },
  {
    id: 'newsReadTimeMinutes',
    label: 'Read Time',
    propertyPath: 'newsReadTimeMinutes',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 41,
    icon: 'schedule',
    tooltip: 'Reading time (minutes)',
  },
  {
    id: 'newsCharacterCount',
    label: 'Characters',
    propertyPath: 'newsCharacterCount',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 42,
    icon: 'format_size',
    tooltip: 'Character count (content length)',
  },
  {
    id: 'newsReadabilityScore',
    label: 'Readability',
    propertyPath: 'newsReadabilityScore',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 43,
    icon: 'assessment',
    tooltip: 'Readability score (content quality)',
  },

  // GROUP 6: EDITORIAL CONTROL (OPTIONAL)
  {
    id: 'newsUrgencyLevel',
    label: 'Urgency',
    propertyPath: 'newsUrgencyLevel',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'enum',
    importance: 'optional',
    sortOrder: 50,
    icon: 'priority_high',
    tooltip: 'Editorial urgency level',
    enumMap: {
      LOW: 'Low',
      MEDIUM: 'Medium',
      HIGH: 'High',
      CRITICAL: 'Critical',
    },
  },
  {
    id: 'newsPriority',
    label: 'Priority',
    propertyPath: 'newsPriority',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(80px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 51,
    icon: 'trending_up',
    tooltip: 'Editorial priority ranking',
  },
  {
    id: 'newsSourceAgencyName',
    label: 'Source Agency',
    propertyPath: 'newsSourceAgencyName',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(110px, 1fr)',
    dataType: 'string',
    importance: 'optional',
    sortOrder: 52,
    icon: 'source',
    tooltip: 'Content source agency',
  },

  // GROUP 7: SERIES & VERSIONING (OPTIONAL)

  // GROUP 8: AUDIT - SOFT DELETE (OPTIONAL)
  {
    id: 'newsDeletedAt',
    label: 'Deleted At',
    propertyPath: 'newsDeletedAt',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(120px, 1fr)',
    dataType: 'date',
    importance: 'optional',
    sortOrder: 60,
    icon: 'delete',
    tooltip: 'Soft delete timestamp (audit)',
  },
  {
    id: 'newsDeletedBy',
    label: 'Deleted By',
    propertyPath: 'newsDeletedBy',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'string',
    importance: 'optional',
    sortOrder: 61,
    icon: 'person_off',
    tooltip: 'User who deleted (audit)',
  },
  {
    id: 'newsSeriesId',
    label: 'Series',
    propertyPath: 'newsSeriesId',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'string',
    importance: 'optional',
    sortOrder: 53,
    icon: 'collections',
    tooltip: 'Series ID (multi-part stories)',
  },
  {
    id: 'newsSeriesOrder',
    label: 'Series Order',
    propertyPath: 'newsSeriesOrder',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 54,
    icon: 'format_list_numbered',
    tooltip: 'Order within series',
  },
  {
    id: 'newsVersion',
    label: 'Version',
    propertyPath: 'newsVersion',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(100px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 55,
    icon: 'history',
    tooltip: 'Content version number',
  },

  // GROUP 7: STATUS INDICATORS (OPTIONAL)
  {
    id: 'newsIsActive',
    label: 'Active',
    propertyPath: 'newsIsActive',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(70px, 1fr)',
    dataType: 'boolean',
    importance: 'optional',
    sortOrder: 60,
    icon: 'visibility',
    tooltip: 'Article active/inactive status',
  },
  {
    id: 'newsIsBreaking',
    label: 'Breaking',
    propertyPath: 'newsIsBreaking',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(70px, 1fr)',
    dataType: 'boolean',
    importance: 'optional',
    sortOrder: 61,
    icon: 'warning',
    tooltip: 'Breaking news indicator',
  },

  // GROUP 8: SOCIAL ENGAGEMENT (OPTIONAL)
  {
    id: 'newsReplyCount',
    label: 'Replies',
    propertyPath: 'newsReplyCount',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(90px, 1fr)',
    dataType: 'number',
    importance: 'optional',
    sortOrder: 62,
    icon: 'reply',
    tooltip: 'Reply count (social engagement)',
  },

  // GROUP 9: GEOGRAPHIC DATA (OPTIONAL)
  {
    id: 'newsCountryCode',
    label: 'Country',
    propertyPath: 'newsCountryCode',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(90px, 1fr)',
    dataType: 'string',
    importance: 'optional',
    sortOrder: 63,
    icon: 'flag',
    tooltip: 'Country code (geographic targeting)',
  },
  {
    id: 'newsRegion',
    label: 'Region',
    propertyPath: 'newsRegion',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(90px, 1fr)',
    dataType: 'string',
    importance: 'optional',
    sortOrder: 64,
    icon: 'location_on',
    tooltip: 'Region/State (geographic targeting)',
  },
  {
    id: 'newsCity',
    label: 'City',
    propertyPath: 'newsCity',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(90px, 1fr)',
    dataType: 'string',
    importance: 'optional',
    sortOrder: 65,
    icon: 'location_city',
    tooltip: 'City (geographic targeting)',
  },
  {
    id: 'newsCreatedAt',
    label: 'Created At',
    propertyPath: 'createdAt', // Use backend canonical field name
    defaultVisible: false,
    hideable: true,
    width: 'minmax(120px, 1fr)',
    dataType: 'date',
    importance: 'optional',
    sortOrder: 70,
    icon: 'add_circle',
    tooltip: 'News creation date (audit trail)',
  },
  {
    id: 'updatedAt',
    label: 'Updated At',
    propertyPath: 'updatedAt', // Use backend canonical field name
    defaultVisible: false,
    hideable: true,
    width: 'minmax(120px, 1fr)',
    dataType: 'date',
    importance: 'optional',
    sortOrder: 71,
    icon: 'update',
    tooltip: 'Last update date (audit trail)',
  },

  // GROUP 8: STATUS BADGES (OPTIONAL)
  {
    id: 'badges',
    label: 'Badges',
    propertyPath: '',
    defaultVisible: false,
    hideable: true,
    width: 'minmax(90px, 1fr)',
    importance: 'optional',
    sortOrder: 80,
    icon: 'star_rate',
    tooltip: 'Status badges and indicators',
  },

  // GROUP 9: OPERATIONS (Always Last)
  {
    id: 'actions',
    label: 'Actions',
    propertyPath: '',
    defaultVisible: true,
    hideable: false,
    width: 'var(--table-col-actions-width)',
    importance: 'critical',
    sortOrder: 99,
    icon: 'more_vert',
    tooltip: 'Edit, delete, or view news',
  },
];

/**
 * Get default column preferences
 */
export function getDefaultColumnPreferences(): ColumnPreferences {
  // Professional default order
  const defaultOrder = [
    'checkbox',
    'thumbnail',
    'title',
    'category',
    'status',
    'scheduledPublishAt',
    'publishedAt',
    'createdBy',
    'actions',
  ];
  return {
    visibleColumnIds: defaultOrder,
    columnOrder: defaultOrder,
    lastModified: new Date(),
    version: 1,
  };
}

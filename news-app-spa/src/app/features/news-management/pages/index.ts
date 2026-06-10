/**
 * NEWS MANAGEMENT PAGES - Clear Architecture Overview
 * 
 * This directory contains the two main pages for news management:
 * 1. Card/Grid List (Public Browsing) - for readers browsing and reading articles
 * 2. List Management (Admin Dashboard) - for admins managing and moderating articles
 * 
 * ROUTING:
 * - `/news`      → NewsBrowsePageComponent (Browse news in card/grid view)
 * - `/news/admin` → NewsManagementListPageComponent (Admin list management)
 */

// PUBLIC BROWSING PAGE (Card View)
export { NewsBrowsePageComponent } from './news-browse-page/news-browse-page.component';

// ADMIN MANAGEMENT PAGE (List View)
export { NewsManagementListPageComponent } from './news-table-list-page/news-table-list-page.component';

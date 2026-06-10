import { Component, Input, Output, EventEmitter, Inject, Optional, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatMenuModule } from '@angular/material/menu';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { NewsItem } from '../../models/news-item.model';
import { NewsFormService } from '../../services/news-form.service';
import { VideoControlService } from '../../services/video-control.service';

@Component({
  selector: 'app-news-card',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MatCardModule, MatIconModule, MatButtonModule, MatDividerModule, MatMenuModule, MatCheckboxModule],
  templateUrl: './news-card.component.html',
  styleUrl: './news-card.component.css'
})
export class NewsCardComponent implements AfterViewInit, OnDestroy {
  @Input() showActions = true;
  @Input() compact = false;
  @Input() news!: NewsItem;
  @Input() selected = false;
  @Output() edit = new EventEmitter<NewsItem>();
  @Output() delete = new EventEmitter<NewsItem>();
  @Output() togglePublish = new EventEmitter<NewsItem>();
  @Output() view = new EventEmitter<NewsItem>();
  @Output() selectionChange = new EventEmitter<{news: NewsItem, selected: boolean}>();

  @ViewChild('videoElement') videoElement?: ElementRef<HTMLVideoElement>;

  adminSectionExpanded = false;
  previewExpanded = false;

  private mediaUrlCache = new Map<string, string>();
  private blobUrls = new Map<string, string>();
  private intersectionObserver?: IntersectionObserver;
  
  // SMART VIDEO PRELOAD STATE
  // Architecture: Control preload by adding/removing <source> element
  // Why: HTML5 preload attribute read once at init - changing it later has no effect
  // Solution: Dynamically render <source> only when video is visible
  videoHasSrc = false; // Start without source → No preloading
  isVideoVisible = false;

  constructor(
    private elementRef: ElementRef,
    @Optional() private newsService?: NewsFormService,
    @Optional() private videoControlService?: VideoControlService
  ) {}

  ngAfterViewInit(): void {
    this.initializeVideoIntersectionObserver();
  }

  private initializeVideoIntersectionObserver(): void {
    if (!this.news.newsMediaFileName || !this.isSupportedVideoFormat(this.news.newsMediaFileName)) {
      return;
    }

    /**
     * INTERSECTION OBSERVER ARCHITECTURE
     * 
     * Goal: Control video preloading based on viewport visibility
     * 
     * Mechanism:
     * - When video enters viewport → Add <source> element → Browser preloads
     * - When video leaves viewport → Remove <source> element → Browser stops loading
     * 
     * Why not just change preload attribute?
     * - HTML5 preload attribute read ONCE at element initialization
     * - Dynamically changing it has NO EFFECT on browser behavior
     * - Must control preloading by managing <source> element presence
     * 
     * This pattern used by: YouTube, Netflix, Vimeo, TikTok
     */
    const observerOptions: IntersectionObserverInit = {
      root: null,
      rootMargin: '50px',
      threshold: 0.25
    };

    this.intersectionObserver = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && !this.isVideoVisible) {
          // Video entered viewport
          // → Set videoHasSrc = true → <source> added to DOM → Browser starts preload
          this.isVideoVisible = true;
          this.videoHasSrc = true;
          console.log('[Video Preload] ✓ Visible → Added <source> → Preload started', {
            filename: this.news.newsMediaFileName,
            visibility: 'IN_VIEWPORT'
          });
        } else if (!entry.isIntersecting && this.isVideoVisible) {
          // Video left viewport
          // → Set videoHasSrc = false → <source> removed from DOM → Browser stops preload
          this.isVideoVisible = false;
          this.videoHasSrc = false;
          console.log('[Video Preload] ✓ Hidden → Removed <source> → Preload stopped', {
            filename: this.news.newsMediaFileName,
            visibility: 'OUT_OF_VIEWPORT'
          });
        }
      });
    }, observerOptions);

    const mediaContainer = this.elementRef.nativeElement.querySelector('.news-media');
    if (mediaContainer) {
      this.intersectionObserver.observe(mediaContainer);
    }
  }

  ngOnDestroy(): void {
    if (this.intersectionObserver) {
      this.intersectionObserver.disconnect();
    }
    this.mediaUrlCache.clear();
    this.blobUrls.forEach(url => URL.revokeObjectURL(url));
    this.blobUrls.clear();
  }

  onEdit(): void {
    this.edit.emit(this.news);
  }

  onDelete(): void {
    this.delete.emit(this.news);
  }

  onTogglePublish(): void {
    this.togglePublish.emit(this.news);
  }

  onView(): void {
    this.view.emit(this.news);
  }

  toggleAdminSection(): void {
    this.adminSectionExpanded = !this.adminSectionExpanded;
  }

  togglePreview(): void {
    this.previewExpanded = !this.previewExpanded;
  }

  toggleSelection(): void {
    this.selected = !this.selected;
    this.selectionChange.emit({news: this.news, selected: this.selected});
  }

  // ==================== ENGAGEMENT METRICS ====================
  getEngagementMetrics(): {likes: number, comments: number, shares: number, views: number} {
    const contentLength = this.news.newsContentEn?.length || 0;
    return {
      likes: Math.floor(contentLength / 5),
      comments: Math.floor(contentLength / 20),
      shares: Math.floor(contentLength / 50),
      views: Math.floor(contentLength / 2) + 100
    };
  }

  // ==================== INDUSTRY-STANDARD ADMIN HELPERS ====================
  getReadingTime(): number {
    const content = this.news.newsContentEn || '';
    const wordsPerMinute = 200;
    const words = content.trim().split(/\s+/).length;
    return Math.max(1, Math.ceil(words / wordsPerMinute));
  }

  getStatusInfo(): {color: string, icon: string, text: string, isUrgent: boolean} {
    const status = this.news.newsWorkflowStatus || 'Draft';
    const isScheduled = !!this.news.newsScheduledPublishAt;
    const isUrgent = isScheduled || status === 'Draft';
    const statusMap: {[key: string]: {color: string, icon: string, text: string}} = {
      'Published': { color: 'accent', icon: 'check_circle', text: 'Published' },
      'Draft': { color: 'warn', icon: 'edit', text: 'Draft' },
      'Scheduled': { color: 'primary', icon: 'schedule', text: 'Scheduled' }
    };
    const baseStatus = statusMap[status] || { color: 'warn', icon: 'help', text: status };
    return { ...baseStatus, isUrgent };
  }

  isScheduled(): boolean {
    return !!this.news.newsScheduledPublishAt;
  }

  formatRelativeTime(dateString: string | Date): string {
    try {
      const date = typeof dateString === 'string' ? new Date(dateString) : dateString;
      const now = new Date();
      const diffMs = now.getTime() - date.getTime();
      const diffMins = Math.floor(diffMs / 60000);
      const diffHours = Math.floor(diffMins / 60);
      const diffDays = Math.floor(diffHours / 24);
      if (diffMins < 1) return 'just now';
      if (diffMins < 60) return `${diffMins}m ago`;
      if (diffHours < 24) return `${diffHours}h ago`;
      if (diffDays < 7) return `${diffDays}d ago`;
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    } catch {
      return 'unknown';
    }
  }

  getLanguageFlag(): string {
    const hasEn = !!this.news.newsTitleEn;
    const hasEs = !!this.news.newsTitleEs;
    if (hasEn && hasEs) return '🌍';
    if (hasEs) return '🇪🇸';
    return '🇬🇧';
  }

  getStatusClass(): string {
    return `status-${this.news.newsWorkflowStatus?.toLowerCase() || 'draft'}`;
  }

  getStatusText(): string {
    return this.news.newsWorkflowStatus || 'Draft';
  }

  formatDate(date?: Date | string): string {
    if (!date) return 'Unknown date';
    
    try {
      const dateObj = typeof date === 'string' ? new Date(date) : date;
      
      if (isNaN(dateObj.getTime())) {
        return 'Invalid date';
      }
      
      return dateObj.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
      });
    } catch (error) {
      console.error('Error formatting date:', error);
      return 'Invalid date';
    }
  }

  getTruncatedContent(maxLength: number = 150): string {
    const content = this.news.newsContentEn;
    return content.length > maxLength 
      ? content.substring(0, maxLength) + '...'
      : content;
  }

  // Media handling methods
  getMediaUrl(fileName: string): string {
    if (!fileName || !this.newsService) return '';
    const cached = this.mediaUrlCache.get(fileName);
    if (cached) return cached;
    const url = this.newsService.getMediaFileUrl(fileName);
    this.mediaUrlCache.set(fileName, url);
    return url;
  }

  getDirectVideoUrl(fileName: string): string {
    if (!fileName || !this.newsService) return '';
    return this.newsService.getMediaFileUrl(fileName);
  }

  isImage(filename: string): boolean {
    if (!filename) return false;
    const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg'];
    const extension = filename.toLowerCase().substring(filename.lastIndexOf('.'));
    return imageExtensions.includes(extension);
  }

  isVideo(filename: string): boolean {
    if (!filename) return false;
    const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm', '.mkv', '.m4v'];
    const extension = filename.toLowerCase().substring(filename.lastIndexOf('.'));
    return videoExtensions.includes(extension);
  }

  isSupportedVideoFormat(filename: string): boolean {
    if (!filename) return false;
    const supportedExtensions = ['.mp4', '.webm', '.ogg'];
    const extension = filename.toLowerCase().substring(filename.lastIndexOf('.'));
    return supportedExtensions.includes(extension);
  }

  isUnsupportedVideoFormat(filename: string): boolean {
    return this.isVideo(filename) && !this.isSupportedVideoFormat(filename);
  }

  getFileExtension(filename: string): string {
    if (!filename) return '';
    return filename.substring(filename.lastIndexOf('.') + 1);
  }

  getVideoMimeType(filename: string): string {
    if (!filename) return 'video/mp4';
    
    const extension = filename.toLowerCase().substring(filename.lastIndexOf('.'));
    const mimeTypes: { [key: string]: string } = {
      '.mp4': 'video/mp4',
      '.webm': 'video/webm',
      '.ogg': 'video/ogg',
      '.avi': 'video/x-msvideo',
      '.mov': 'video/quicktime',
      '.wmv': 'video/x-ms-wmv',
      '.flv': 'video/x-flv',
      '.mkv': 'video/x-matroska',
      '.m4v': 'video/mp4'
    };
    
    return mimeTypes[extension] || 'video/mp4';
  }

  onImageError(event: any): void {
    const target = event.target as HTMLElement;
    const parent = target.parentElement;
    if (parent) {
      parent.style.display = 'none';
    }
  }

  onMediaError(event: any): void {
    console.error('Media failed to load:', event);
    const target = event.target as HTMLElement;
    if (target && target.tagName.toLowerCase() === 'img') {
      target.style.display = 'none';
    }
  }

  onVideoPlay(event: any): void {
    if (this.videoControlService) {
      this.videoControlService.playVideo(event.target as HTMLVideoElement);
    }
  }

  onVideoLoadStart(event: any): void {
    console.log('Video load started:', event.target.src);
  }

  onVideoCanPlay(event: any): void {
    console.log('Video can play:', event.target.src);
  }

  onVideoError(event: any): void {
    console.error('Video failed to load:', event.target.src);
    console.error('Video error:', event.target.error);
  }
}
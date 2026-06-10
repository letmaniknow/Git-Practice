import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { Subject, takeUntil, switchMap } from 'rxjs';

import { NewsFormService } from '../../services/news-form.service';
import { NewsItem } from '../../models/news-item.model';
import { ConfirmationDialogComponent } from '../../components/confirmation-dialog/confirmation-dialog.component';
import { VideoControlService } from '../../services/video-control.service';

@Component({
  selector: 'app-news-detail-page',
  standalone: true,
  imports: [CommonModule, RouterModule, ConfirmationDialogComponent],
  templateUrl: './news-detail-page.component.html',
  styleUrl: './news-detail-page.component.css'
})
export class NewsDetailPageComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private mediaUrlCache = new Map<string, string>();
  private blobUrls = new Map<string, string>(); // Store blob URLs for cleanup
  
  news: NewsItem | null = null;
  isLoading = false;
  error: string | null = null;
  newsId: string | null = null;
  
  // Dialog state
  showDeleteDialog = false;

  constructor(
    private newsService: NewsFormService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private videoControlService: VideoControlService
  ) {}

  ngOnInit(): void {
    this.loadNewsFromRoute();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    
    // Clear URL cache and revoke blob URLs to prevent memory leaks
    this.mediaUrlCache.clear();
    this.blobUrls.forEach(url => URL.revokeObjectURL(url));
    this.blobUrls.clear();
  }

  private loadNewsFromRoute(): void {
    this.route.paramMap.pipe(
      takeUntil(this.destroy$),
      switchMap(params => {
        this.newsId = params.get('id');
        if (!this.newsId) {
          this.router.navigate(['/news']);
          throw new Error('No news ID provided');
        }
        this.isLoading = true;
        
        // Clear media URL cache when loading news
        this.mediaUrlCache.clear();
        // Clear blob URLs
        this.blobUrls.forEach(url => URL.revokeObjectURL(url));
        this.blobUrls.clear();
        
        return this.newsService.getNewsById(this.newsId);
      })
    ).subscribe({
      next: (news) => {
        this.isLoading = false;
        if (news) {
          console.log('Successfully loaded news:', news);
          this.news = news;
          
          // Debug media file information
          const mediaFile = this.getMediaFileName();
          console.log('Media file:', mediaFile);
          if (mediaFile) {
            console.log('Is video:', this.isVideo(mediaFile));
            console.log('Is supported video:', this.isSupportedVideoFormat(mediaFile));
            console.log('Is image:', this.isImage(mediaFile));
          }
        } else {
          this.router.navigate(['/news']);
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.error = error.message;
        console.error('Failed to load news:', error);
      }
    });
  }

  onEdit(): void {
    if (this.newsId) {
      this.router.navigate(['/news', this.newsId, 'edit']);
    }
  }

  onDelete(): void {
    this.showDeleteDialog = true;
  }

  confirmDelete(): void {
    if (this.newsId) {
      this.newsService.deleteNews(this.newsId).subscribe({
        next: () => {
          this.router.navigate(['/news']);
        },
        error: (error: any) => {
          console.error('Failed to delete news:', error);
          this.showDeleteDialog = false;
        }
      });
    }
  }

  cancelDelete(): void {
    this.showDeleteDialog = false;
  }

  onTogglePublish(): void {
    // This functionality would need to be implemented based on your backend
    // since isPublished is not part of the current News interface
    console.log('Publish/unpublish functionality not implemented in current interface');
  }

  onBack(): void {
    this.router.navigate(['/news']);
  }

  formatDate(date: Date | string): string {
    if (!date) return 'Unknown date';
    
    try {
      const dateObj = typeof date === 'string' ? new Date(date) : date;
      
      // Check if the date is valid
      if (isNaN(dateObj.getTime())) {
        return 'Invalid date';
      }
      
      // Industry standard: DD-MMM-YYYY HH:mm (24-hour format)
      return dateObj.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      });
    } catch (error) {
      console.error('Error formatting date:', error);
      return 'Invalid date';
    }
  }

  shareNews(): void {
    const shareUrl = `${window.location.origin}${this.router.url}`;
    if (navigator.share && this.news) {
      navigator.share({
        title: this.news.newsTitleEn,
        text: this.news.newsContentEn.substring(0, 200) + '...',
        url: shareUrl
      }).catch(err => console.log('Error sharing:', err));
    } else {
      // Fallback to copying URL to clipboard
      navigator.clipboard.writeText(shareUrl).then(() => {
        // Could show a toast notification here
        console.log('URL copied to clipboard');
      });
    }
  }

  onImageError(event: any): void {
    const target = event.target as HTMLElement;
    const parent = target.parentElement;
    if (parent) {
      parent.style.display = 'none';
    }
  }

  // Media handling methods
  getMediaFileName(): string {
    return this.news?.newsMediaFileName || '';
  }

  getMediaUrl(fileName: string): string {
    if (!fileName) return '';
    
    // Check if URL is already cached to prevent ExpressionChangedAfterItHasBeenCheckedError
    if (this.mediaUrlCache.has(fileName)) {
      return this.mediaUrlCache.get(fileName)!;
    }
    
    // For images, use direct URL
    if (this.isImage(fileName)) {
      const url = this.newsService.getMediaFileUrl(fileName);
      console.log('Generated image URL for', fileName, ':', url);
      this.mediaUrlCache.set(fileName, url);
      return url;
    }
    
    // For videos, create blob URL asynchronously and return empty initially
    if (this.isVideo(fileName)) {
      // Set empty initially, blob URL will be created asynchronously
      this.mediaUrlCache.set(fileName, '');
      
      // Create blob URL asynchronously
      this.createVideoBlobUrl(fileName);
      
      return '';
    }
    
    // Fallback to direct URL
    const url = this.newsService.getMediaFileUrl(fileName);
    this.mediaUrlCache.set(fileName, url);
    return url;
  }

  // Create blob URL for video files to enable proper streaming
  private createVideoBlobUrl(fileName: string): void {
    if (!fileName || !this.isVideo(fileName)) return;
    
    console.log('Creating blob URL for video:', fileName);
    
    this.newsService.getMediaFileBlob(fileName).subscribe({
      next: (blob: any) => {
        console.log('Successfully received blob for video:', fileName, 'Size:', blob.size, 'Type:', blob.type);
        const blobUrl = URL.createObjectURL(blob);
        console.log('Created blob URL for video:', fileName, blobUrl);
        
        // Update the cached URL
        this.mediaUrlCache.set(fileName, blobUrl);
        this.blobUrls.set(fileName, blobUrl);
        
        // Trigger change detection to update the view
        this.cdr.detectChanges();
        console.log('Video blob URL ready and view updated for:', fileName);
      },
      error: (error: any) => {
        console.error('Failed to create blob URL for video:', fileName, error);
        // Fallback to direct URL
        const fallbackUrl = this.newsService.getMediaFileUrl(fileName);
        console.log('Using fallback URL for video:', fileName, fallbackUrl);
        this.mediaUrlCache.set(fileName, fallbackUrl);
        this.cdr.detectChanges();
      }
    });
  }

  // Check if video blob URL is ready
  isVideoReady(fileName: string): boolean {
    if (!fileName || !this.isVideo(fileName)) return false;
    
    const cachedUrl = this.mediaUrlCache.get(fileName);
    return cachedUrl !== undefined && 
           cachedUrl !== '' && 
           cachedUrl.startsWith('blob:');
  }

  // Get direct video URL for fallback
  getDirectVideoUrl(fileName: string): string {
    if (!fileName) return '';
    return this.newsService.getMediaFileUrl(fileName);
  }

  isImage(fileName: string): boolean {
    if (!fileName) return false;
    const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg'];
    const extension = fileName.toLowerCase().substring(fileName.lastIndexOf('.'));
    return imageExtensions.includes(extension);
  }

  isVideo(fileName: string): boolean {
    if (!fileName) return false;
    const videoExtensions = ['.mp4', '.webm', '.ogg', '.mov', '.avi', '.wmv', '.flv', '.mkv'];
    const extension = fileName.toLowerCase().substring(fileName.lastIndexOf('.'));
    return videoExtensions.includes(extension);
  }

  // Check if video format is supported by HTML5 video element
  isSupportedVideoFormat(fileName: string): boolean {
    if (!fileName) return false;
    const supportedExtensions = ['.mp4', '.webm', '.ogg'];
    const extension = fileName.toLowerCase().substring(fileName.lastIndexOf('.'));
    return supportedExtensions.includes(extension);
  }

  // Check if it's a video but not supported for streaming (like AVI)
  isUnsupportedVideoFormat(fileName: string): boolean {
    return this.isVideo(fileName) && !this.isSupportedVideoFormat(fileName);
  }

  // Get file extension for display
  getFileExtension(fileName: string): string {
    if (!fileName) return '';
    return fileName.substring(fileName.lastIndexOf('.') + 1);
  }

  getVideoMimeType(fileName: string): string {
    if (!fileName) return 'video/mp4';
    const extension = fileName.toLowerCase().substring(fileName.lastIndexOf('.'));
    
    const mimeMap: { [key: string]: string } = {
      '.mp4': 'video/mp4',
      '.webm': 'video/webm',
      '.ogg': 'video/ogg',
      '.mov': 'video/quicktime',
      '.avi': 'video/x-msvideo',
      '.wmv': 'video/x-ms-wmv',
      '.flv': 'video/x-flv',
      '.mkv': 'video/x-matroska'
    };
    
    return mimeMap[extension] || 'video/mp4';
  }

  // Video playback control - ensures only one video plays at a time
  onVideoPlay(event: any): void {
    const currentVideo = event.target as HTMLVideoElement;
    console.log('Video started playing:', currentVideo.src);
    
    // Use the video control service to manage playback
    // This will automatically pause any other playing videos
    this.videoControlService.playVideo(currentVideo);
  }

  // Pause all videos except the currently playing one
  private pauseAllOtherVideos(currentVideo: HTMLVideoElement): void {
    // Since the service already handles pausing other videos in playVideo(),
    // this method is no longer needed as the logic is centralized
    // But we'll keep it for backward compatibility
    this.videoControlService.pauseAllVideos();
  }

  onVideoLoad(event: any): void {
    console.log('Video loaded successfully:', event.target.src);
    console.log('Video duration:', event.target.duration);
    console.log('Video ready state:', event.target.readyState);
  }

  onVideoLoadStart(event: any): void {
    console.log('Video load started:', event.target.src);
  }

  onVideoCanPlay(event: any): void {
    console.log('Video can play:', event.target.src);
    console.log('Video buffered:', event.target.buffered.length);
  }

  onVideoError(event: any): void {
    console.error('Video failed to load:', event.target.src);
    console.error('Video error:', event.target.error);
    console.error('Network state:', event.target.networkState);
    console.error('Error code:', event.target.error?.code);
    console.error('Error message:', event.target.error?.message);
    
    // Try to test the URL directly
    this.testMediaUrl(event.target.src);
  }

  // Test if media URL is accessible
  testMediaUrl(url: string): void {
    fetch(url, { method: 'HEAD' })
      .then(response => {
        console.log('Media URL test - Status:', response.status);
        console.log('Media URL test - Headers:', response.headers);
        console.log('Media URL test - Content-Type:', response.headers.get('content-type'));
      })
      .catch(error => {
        console.error('Media URL test failed:', error);
      });
  }

  getDeleteMessage(): string {
    const title = this.news?.newsTitleEn || 'this news';
    return `Are you sure you want to delete "${title}"? This action cannot be undone.`;
  }
}
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class VideoControlService {
  private currentlyPlaying: HTMLVideoElement | null = null;

  // Play a video and pause all others
  playVideo(video: HTMLVideoElement): void {
    // Pause the currently playing video if there is one
    if (this.currentlyPlaying && this.currentlyPlaying !== video) {
      console.log('Pausing currently playing video:', this.currentlyPlaying.src);
      this.currentlyPlaying.pause();
    }
    
    // Set the new video as currently playing
    this.currentlyPlaying = video;
    console.log('New video started playing:', video.src);
  }

  // Pause all videos
  pauseAllVideos(): void {
    const allVideos = document.querySelectorAll('video');
    allVideos.forEach((video: HTMLVideoElement) => {
      if (!video.paused) {
        console.log('Pausing video:', video.src);
        video.pause();
      }
    });
    this.currentlyPlaying = null;
  }

  // Get currently playing video
  getCurrentlyPlaying(): HTMLVideoElement | null {
    return this.currentlyPlaying;
  }

  // Clear the currently playing video reference
  clearCurrentlyPlaying(): void {
    this.currentlyPlaying = null;
  }
}
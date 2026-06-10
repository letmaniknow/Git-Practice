import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpResponse,
  HttpInterceptor,
} from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay, tap } from 'rxjs/operators';

@Injectable()
export class MockDashboardInterceptor implements HttpInterceptor {
  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    // Mock: GET /api/v1/admin/dashboard/stats
    if (request.url.includes('/api/v1/admin/dashboard/stats') && request.method === 'GET') {
      const mockStats = {
        totalArticles: 156,
        publishedThisMonth: 42,
        draftCount: 18,
        scheduledCount: 7,
        archivedCount: 89,
        systemHealth: 'healthy' as const,
      };
      console.log('✅ Mock interceptor: Serving dashboard stats');
      return of(new HttpResponse({ status: 200, body: mockStats })).pipe(
        delay(800),
        tap(response => console.log('📊 Stats response:', response.body))
      );
    }

    // Mock: GET /api/v1/admin/dashboard/recent-activity
    if (
      request.url.includes('/api/v1/admin/dashboard/recent-activity') &&
      request.method === 'GET'
    ) {
      const mockActivities = [
        {
          id: '1',
          articleId: 'art-001',
          articleTitle: 'Breaking News: Major Tech Announcement',
          actionType: 'PUBLISHED',
          performedBy: 'Editor Admin',
          timestamp: new Date(Date.now() - 5 * 60000),
        },
        {
          id: '2',
          articleId: 'art-002',
          articleTitle: 'Market Analysis Q1 2026',
          actionType: 'CREATED',
          performedBy: 'Writer User',
          timestamp: new Date(Date.now() - 15 * 60000),
        },
        {
          id: '3',
          articleId: 'art-003',
          articleTitle: 'Opinion: Future of Digital News',
          actionType: 'UPDATED',
          performedBy: 'Editor Admin',
          timestamp: new Date(Date.now() - 45 * 60000),
        },
        {
          id: '4',
          articleId: 'art-004',
          articleTitle: 'Sports Update: Championship Winners',
          actionType: 'PUBLISHED',
          performedBy: 'Sports Editor',
          timestamp: new Date(Date.now() - 2 * 3600000),
        },
        {
          id: '5',
          articleId: 'art-005',
          articleTitle: 'Tech Review: New Smartphone',
          actionType: 'ARCHIVED',
          performedBy: 'Editor Admin',
          timestamp: new Date(Date.now() - 24 * 3600000),
        },
      ];
      console.log('✅ Mock interceptor: Serving recent activities');
      return of(new HttpResponse({ status: 200, body: mockActivities })).pipe(
        delay(800),
        tap(response => console.log('📰 Activities response:', response.body))
      );
    }

    // Pass through to the next interceptor/handler
    return next.handle(request);
  }
}


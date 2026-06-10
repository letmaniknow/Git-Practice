import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { DashboardNewsService } from '../news/services/dashboard-news.service';
import * as AdminDashboardActions from './admin-dashboard.actions';

@Injectable()
export class AdminDashboardEffects {
  // ✅ Load Dashboard Data Effect (NEWS)
  loadDashboardData$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AdminDashboardActions.loadDashboardData),
      tap(() => console.log('🔄 Effects: loadDashboardData action received')),
      switchMap(() =>
        this.dashboardService.getStats().pipe(
          tap((stats) => console.log('📊 Effects: Stats loaded:', stats)),
          switchMap((stats) =>
            this.dashboardService.getRecentActivity().pipe(
              tap((activities) => console.log('📰 Effects: Activities loaded:', activities)),
              map((recentActivity) => {
                console.log('✅ Effects: Dispatching success action');
                return AdminDashboardActions.loadDashboardDataSuccess({ stats, recentActivity });
              })
            )
          ),
          catchError((error) => {
            console.error('❌ Effects: Error loading dashboard:', error);
            return of(
              AdminDashboardActions.loadDashboardDataFailure({
                error: error.message || 'Failed to load dashboard data',
              })
            );
          })
        )
      )
    )
  );

  constructor(
    private actions$: Actions,
    private dashboardService: DashboardNewsService
  ) {}
}

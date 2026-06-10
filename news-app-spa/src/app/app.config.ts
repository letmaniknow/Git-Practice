import { routes } from './app.routes';
import { ApplicationConfig, provideZoneChangeDetection, importProvidersFrom } from '@angular/core';
import { provideRouter, RouterModule } from '@angular/router';
import { provideHttpClient, withInterceptors, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpClientModule } from '@angular/common/http';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { GlobalErrorInterceptor } from './core/interceptors/global-error.interceptor';
// ✅ Removed mock interceptor - now using real backend API
// import { MockDashboardInterceptor } from './core/interceptors/mock-dashboard.interceptor';
import { LoginLayoutComponent } from './layouts/login-layout/login-layout.component';
import { LayoutComponent } from './layouts/main-layout/layout.component';
import { provideAnimations } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';
import { FooterComponent } from './layouts/main-layout/footer/main-footer.component';
import { HeaderComponent } from './layouts/main-layout/header/main-header.component';
import { SidebarComponent } from './layouts/main-layout/sidebar/main-sidebar.component';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { newsReducer } from '@store/news/news.reducer';
import { newsSchedulerReducer } from './features/news-management/store/news-scheduler.reducer';
import { NewsSchedulerEffects } from './features/news-management/store/news-scheduler.effects';
import { adminDashboardReducer } from './features/dashboard-management/store/admin-dashboard.reducer';
import { ThemeService } from './core/services/theme.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    // ✅ Removed mock interceptor - now using real backend API
    // {
    //   provide: HTTP_INTERCEPTORS,
    //   useClass: MockDashboardInterceptor,
    //   multi: true
    // },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: GlobalErrorInterceptor,
      multi: true
    },
    provideAnimations(),
    provideStore({ news: newsReducer, newsScheduler: newsSchedulerReducer, adminDashboard: adminDashboardReducer }),
    provideEffects([NewsSchedulerEffects]),
    ThemeService,  // 🎨 Initialize theme service on app startup
    importProvidersFrom(
      CommonModule,
      RouterModule,
      HttpClientModule
    )
  ]
};

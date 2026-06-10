# Dashboard Architecture Guide

## Overview

The News Dashboard is built with a **feature-first, horizontally scalable architecture**. This document serves as the blueprint for implementing Phases 2-5 following the same professional patterns.

---

## Project Structure

### Folder Organization

```
src/app/features/dashboard-management/
├── shared/
│   ├── components/            # Reusable components (all phases)
│   │   ├── dashboard-quick-actions/
│   │   └── dashboard-stats-card/
│   ├── services/              # Reusable services
│   │   ├── dashboard-base.service.ts       # Abstract base (implement in each phase)
│   │   ├── dashboard-action-handler.service.ts
│   │   └── index.ts           # Barrel export
│   ├── models/                # Shared interfaces & types
│   │   ├── dashboard-base.model.ts         # Base interfaces
│   │   ├── dashboard-quick-actions.model.ts
│   │   └── index.ts           # Barrel export
│   ├── constants/
│   │   └── index.ts           # Barrel export (feature-specific constants in phase folders)
│   └── README.md              # Shared component documentation
│
├── news/                      # Phase 1: News Dashboard (Active)
│   ├── pages/
│   │   └── dashboard-news-page.component.ts
│   ├── components/
│   │   └── dashboard-news-activity/
│   ├── services/
│   │   └── dashboard-news.service.ts      (extends DashboardBaseService)
│   ├── models/
│   │   └── dashboard-news.model.ts
│   ├── constants/
│   │   ├── dashboard-news-api.constant.ts
│   │   └── dashboard-quick-actions.constant.ts
│   └── README.md
│
├── admin-user/                # Phase 2: Admin User Management (Placeholder)
│   ├── pages/
│   │   └── dashboard-admin-user-page.component.ts
│   ├── components/
│   │   └── dashboard-admin-user-activity/
│   ├── services/
│   │   └── dashboard-admin-user.service.ts
│   ├── models/
│   │   └── dashboard-admin-user.model.ts
│   └── constants/
│       └── dashboard-admin-user-api.constant.ts
│
├── category/                  # Phase 3: Category Management (Placeholder)
├── recycle-bin/               # Phase 4: Recycle Bin (Placeholder)
├── settings/                  # Phase 5: Settings (Placeholder)
└── store/                     # (Optional) Centralized state management
```

---

## Key Architectural Principles

### 1. **Feature-First Organization**

- Each phase is completely self-contained in its own folder
- All phase-specific code stays in `{phase}/` directory
- Shared code goes in `shared/` directory
- Easy to add/remove/modify phases without affecting others

### 2. **Inheritance & Contracts**

- All phase services **extend `DashboardBaseService`**
- Forces implementation of required methods:
  - `getStats(): Observable<any>`
  - `getRecentActivity(page, size): Observable<any[]>`
  - `onRefresh(): void`
  - `getPageInfo(): Observable<PageResponse>`
- Ensures consistency across all phases

### 3. **Reusable Components**

**Shared (in `shared/components/`):**

- `DashboardQuickActionsComponent` - Used by all phases
- `DashboardStatsCardComponent` - Generic stat display

**Phase-Specific (in `{phase}/components/`):**

- `DashboardNewsActivityComponent` - Displays news audit logs
- `DashboardAdminUserActivityComponent` - Displays user activity logs
- `DashboardCategoryActivityComponent` - Displays category changes
- (Each phase has its own activity component following the same pattern)

### 4. **Centralized Routing**

- `DashboardActionHandlerService` handles all quick action navigation
- Single source of truth for routing logic
- Easy to update all phases at once

### 5. **Barrel Exports (Index Files)**

- `shared/components/index.ts` - Export all shared components
- `shared/models/index.ts` - Export all shared models
- `shared/services/index.ts` - Export all shared services
- `shared/constants/index.ts` - Export all shared constants
- **Benefit:** Cleaner imports across the codebase

---

## Implementation Checklist for New Phases

### Phase 2: Admin User Management

#### 1. Create Folder Structure

```bash
mkdir -p src/app/features/dashboard-management/admin-user/{pages,components,services,models,constants}
```

#### 2. Create Models (`admin-user/models/dashboard-admin-user.model.ts`)

```typescript
import { BaseDashboardStats, BaseAuditLog } from '../../shared/models';

// Extend base interfaces with phase-specific fields
export interface AdminUserStats extends BaseDashboardStats {
  totalAdminUsers: number;
  activeAdminUsers: number;
  ...
}

export interface AdminUserAuditLog extends BaseAuditLog {
  adminUserId: string;
  ...
}
```

#### 3. Create Service (`admin-user/services/dashboard-admin-user.service.ts`)

```typescript
import { DashboardBaseService } from "../../shared/services";

@Injectable({ providedIn: "root" })
export class DashboardAdminUserService extends DashboardBaseService {
  // Implement required abstract methods
  override getStats(): Observable<AdminUserStats> {}
  override getRecentActivity(page, size): Observable<AdminUserAuditLog[]> {}
  override onRefresh(): void {}
  override getPageInfo(): Observable<PageResponse | null> {}
}
```

#### 4. Create API Constants (`admin-user/constants/dashboard-admin-user-api.constant.ts`)

```typescript
export const DASHBOARD_ADMIN_USER_API = {
  STATS: "/api/v1/admin/dashboard/admin-users/stats",
  RECENT_ACTIVITY: "/api/v1/admin/dashboard/admin-users/activity",
};
```

#### 5. Create Activity Component (`admin-user/components/dashboard-admin-user-activity/`)

Follow the same pattern as `DashboardNewsActivityComponent`

#### 6. Create Page Component (`admin-user/pages/dashboard-admin-user-page.component.ts`)

```typescript
import { DashboardBaseService } from "../../shared/services";
import { DashboardStatsCardComponent, DashboardQuickActionsComponent } from "../../shared/components";
import { DASHBOARD_QUICK_ACTIONS } from "../constants/dashboard-quick-actions.constant";

export class DashboardAdminUserPageComponent implements OnInit, OnDestroy {
  readonly actions: QuickAction[] = DASHBOARD_QUICK_ACTIONS;

  // Same pattern as news-page component
  stats$!: Observable<AdminUserStats>;
  recentActivity$!: Observable<AdminUserAuditLog[]>;
  // ...
}
```

#### 7. Update Routing

Add route to main app routing module

#### 8. Update DashboardActionHandlerService

If new feature needs custom routing logic

---

## Naming Conventions

### Folder Names

- Use kebab-case: `dashboard-news`, `dashboard-admin-user`
- Feature-first: `{dashboard-feature-name}`

### File Names

- Components: `dashboard-{feature}-{type}.component.ts`
  - Example: `dashboard-news-page.component.ts`, `dashboard-admin-user-activity.component.ts`
- Services: `dashboard-{feature}.service.ts`
  - Example: `dashboard-news.service.ts`
- Models: `dashboard-{feature}.model.ts`
  - Example: `dashboard-admin-user.model.ts`
- Constants: `dashboard-{feature}-api.constant.ts`
  - Example: `dashboard-category-api.constant.ts`

### Class Names

- Components: `Dashboard{Feature}{Type}Component`
  - Example: `DashboardNewsPageComponent`, `DashboardAdminUserActivityComponent`
- Services: `Dashboard{Feature}Service`
  - Example: `DashboardNewsService`, `DashboardCategoryService`
- Selectors: `app-dashboard-{feature}-{type}`
  - Example: `app-dashboard-news-page`, `app-dashboard-admin-user-activity`

---

## Best Practices

### 1. Memory Leak Prevention

```typescript
private readonly destroy$ = new Subject<void>();

ngOnInit() {
  this.observable$.pipe(
    takeUntil(this.destroy$)
  ).subscribe();
}

ngOnDestroy() {
  this.destroy$.next();
  this.destroy$.complete();
}
```

### 2. Change Detection

```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush  // Always use this
})
```

### 3. RxJS State Management

```typescript
private stats$ = new BehaviorSubject<Stats | null>(null);
public stats$ = this.statsSubject$.asObservable();

// In service methods
this.statsSubject$.next(newData);  // Update state
return this.stats$;                 // Return observable
```

### 4. Error Handling

```typescript
return this.http.get(...).pipe(
  tap(response => console.log('Success:', response)),
  map(response => response.data),
  catchError(error => {
    console.error('Error:', error);
    this.errorSubject$.next('User-friendly message');
    return throwError(() => error);
  }),
  finalize(() => this.loadingSubject$.next(false))
);
```

---

## Testing Strategy

### Unit Tests

- Test each service method independently
- Mock HttpClient
- Test component logic with change detection

### Integration Tests

- Test service + component together
- Test routing logic
- Test action handlers

### E2E Tests

- Test full dashboard workflows
- Test navigation between phases

---

## Future Considerations

1. **State Management:** Consider NgRx or Akita if phases become complex
2. **Smart Components:** Page components coordinate between services and view
3. **Presentation Components:** Reusable UI components in shared
4. **Effects:** Use RxJS operators for side effects
5. **Caching:** Implement caching strategy for stats/activities

---

## Troubleshooting

### "Cannot find module" Errors

- Check barrel export files exist: `shared/components/index.ts`, etc.
- Verify import paths: `import { Component } from 'shared/components'`

### Service Not Injected

- Ensure service extends `DashboardBaseService`
- Ensure `@Injectable({ providedIn: 'root' })`
- Verify DI token used in component

### Activity Feed Not Updating

- Check `takeUntil(this.destroy$)` is used
- Verify BehaviorSubject `.next()` is called
- Check HTTP endpoint is correct

---

## Support & Questions

For questions about the dashboard architecture, refer to:

- Component documentation: `shared/README.md`
- Service documentation: see JSDoc in `dashboard-base.service.ts`
- Example implementation: `news/` folder (Phase 1)

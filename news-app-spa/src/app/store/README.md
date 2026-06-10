# 🏪 Store (NgRx State Management)

This folder contains the application's **NgRx store** for centralized state management.

## 📋 What is NgRx?

NgRx is the Angular implementation of Redux pattern. It provides:

- **Centralized state** - Single source of truth for app data
- **Predictable mutations** - All state changes via actions + reducers
- **Time-travel debugging** - Can replay actions to understand state changes
- **Performance** - Memoized selectors prevent unnecessary re-renders
- **Testability** - Pure functions are easy to unit test

## 📁 Folder Structure

```
store/
├── README.md                 (you are here)
├── app.state.ts             (root state interface - combines all features)
├── index.ts                 (barrel exports - centralized imports)
└── news/                    (feature-specific folder)
    ├── news.state.ts        (NewsState interface: selectedIds, filters, pagination)
    ├── news.actions.ts      (all possible events: SelectNews, ClearSelection, etc.)
    ├── news.reducer.ts      (pure functions handling state mutations)
    └── news.selectors.ts    (memoized queries: selectSelectedCount, selectIsItemSelected, etc.)
```

## 🎯 How to Use

### Import from Store

```typescript
// ✅ GOOD - Using @store path alias (clean imports)
import { selectSelectedCount, newsReducer } from "@store";

// ❌ AVOID - Using relative paths (hard to maintain)
import { selectSelectedCount } from "../../../store/news/selectors";
```

### Dispatch Actions

```typescript
// In component
import { selectNewsListState } from '@store';

constructor(private store: Store) {}

selectNews(id: string) {
  this.store.dispatch(selectNewsItem({ id }));
}
```

### Subscribe to Selectors

```typescript
// In component template
<div *ngIf="(selectedCount$ | async) > 0">
  {{ selectedCount$ | async }} items selected
</div>

// In component class
selectedCount$ = this.store.select(selectSelectedCount);
```

## 📝 File Purposes

| File                | Purpose            | Example                                                        |
| ------------------- | ------------------ | -------------------------------------------------------------- |
| `news.state.ts`     | Define state shape | `selectedIds: string[]`, `filters: { status?, language? }`     |
| `news.actions.ts`   | Define events      | `selectNewsItem`, `clearSelection`, `setStatusFilter`          |
| `news.reducer.ts`   | Handle mutations   | Toggle selection, update filters, change pagination            |
| `news.selectors.ts` | Query state        | Get selected count, check if item selected, get active filters |

## 🔄 Data Flow (Redux Pattern)

```
Component dispatches action
    ↓
Action → Reducer
    ↓
State updated
    ↓
Selectors recompute (memoized)
    ↓
Components subscribed to selectors re-render (if needed)
```

**Example:**

```typescript
// 1. Component dispatches action
this.store.dispatch(selectNewsItem({ id: '123' }));

// 2. Reducer receives action
on(selectNewsItem, (state, { id }) => ({
  ...state,
  selectedIds: [...state.selectedIds, id]
}))

// 3. Selectors recompute
selectSelectedCount → now returns 1

// 4. Component subscribed to selector updates
{{ selectedCount$ | async }} // displays "1"
```

## ➕ Adding a New Feature (e.g., Ads)

1. Create folder structure:

```
store/ads/
├── ads.state.ts
├── ads.actions.ts
├── ads.reducer.ts
└── ads.selectors.ts
```

2. Define state in `ads.state.ts`:

```typescript
export interface AdsState {
  selectedIds: string[];
  filters: { campaignId?: string };
  // ... other properties
}
```

3. Register in `app.state.ts`:

```typescript
export interface AppState {
  news: NewsState;
  ads: AdsState; // ← Add this
}
```

4. Register in `app.config.ts`:

```typescript
provideStore({
  news: newsReducer,
  ads: adsReducer, // ← Add this
});
```

5. Export from `index.ts`:

```typescript
export * from "./ads/ads.state";
export * from "./ads/ads.actions";
export * from "./ads/ads.reducer";
export * from "./ads/ads.selectors";
```

## 🚀 Best Practices

1. **Keep selectors memoized** - Always use `createSelector` in selectors file
2. **Keep reducers pure** - No HTTP calls, no side effects (use Effects for that)
3. **One action per event** - `selectNewsItem`, `deselectNewsItem` (not `toggleSelection`)
4. **Use strong typing** - Full TypeScript support prevents bugs
5. **Test selectors & reducers** - Easy to unit test (pure functions)

## 📚 Learning Resources

- [NgRx Official Docs](https://ngrx.io/)
- [Redux Pattern](https://redux.js.org/)
- Redux DevTools (Chrome extension) - Inspect state changes in real-time

## 🔧 Path Alias Setup

The project is configured with `@store` path alias in `tsconfig.json`:

```json
{
  "compilerOptions": {
    "paths": {
      "@store": ["src/app/store/index.ts"],
      "@store/*": ["src/app/store/*"]
    }
  }
}
```

This allows clean imports:

```typescript
// Instead of:
import { selectSelectedCount } from "../../../store/news/news.selectors";

// Write:
import { selectSelectedCount } from "@store";
```

---

**Questions?** Check the individual files or reach out to the frontend team.

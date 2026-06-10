# News List Table Grid Architecture Migration Plan

## Goals

- Migrate from native `<table>` to a CSS Grid-based layout for the news list table
- Achieve true flexible columns, horizontal scrolling, and future-proof customization
- Preserve all current features (sorting, actions, column customization, accessibility, etc.)

## Steps

1. **Scaffold grid-based markup:**
   - Replace `<table>`, `<thead>`, `<tbody>`, `<tr>`, `<th>`, `<td>` with `<div>`s using CSS Grid
   - Use a parent `.news-grid-table` container with `display: grid` for header and rows
2. **Define grid columns:**
   - Use `grid-template-columns` for all columns
   - Make the Title column `1fr` (flexible), others `min-content` or `max-content`
3. **Migrate header and row rendering:**
   - Render header and each row as a grid
   - Use `*ngIf` for column visibility
   - Use ARIA roles for accessibility
4. **Move sorting, actions, and tooltips:**
   - All interactive features preserved
5. **Horizontal scroll:**
   - `.table-wrapper` gets `overflow-x: auto`
   - Grid min-width set to total min column widths
6. **Column customization:**
   - Show/hide columns by toggling grid-template-columns and header/cell visibility
7. **Responsive:**
   - Use media queries for mobile/tablet/desktop
8. **Accessibility:**
   - Use ARIA roles and keyboard navigation
9. **Testing:**
   - Validate all features, visual alignment, and scroll

## Impact

- All table CSS and markup will be refactored
- No breaking changes to business logic
- All features preserved and improved

---

**Next: Scaffold grid-based markup and update CSS.**

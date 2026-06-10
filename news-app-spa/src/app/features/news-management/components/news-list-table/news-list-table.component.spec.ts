import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminTableComponent } from './admin-table.component';

describe('AdminTableComponent', () => {
  let component: AdminTableComponent;
  let fixture: ComponentFixture<AdminTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminTableComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display empty state when no articles', () => {
    expect(component.articles.length).toBe(0);
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('.empty-state')).toBeTruthy();
  });

  it('should sort by column', () => {
    component.sortBy('title');
    expect(component.sortColumn).toBe('title');
    expect(component.sortDirection).toBe('desc');
  });

  it('should toggle sort direction on same column click', () => {
    component.sortBy('title');
    expect(component.sortDirection).toBe('desc');
    
    component.sortBy('title');
    expect(component.sortDirection).toBe('asc');
  });
});

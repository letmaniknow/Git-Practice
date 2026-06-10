
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { debounceTime, Subject } from 'rxjs';


export interface SearchBarConfig {
  placeholder?: string;
  fields: Array<{
    key: string;
    label: string;
    type: 'text' | 'select' | 'date' | 'autocomplete';
    options?: Array<{ label: string; value: any }>;
    autocompleteFn?: (query: string) => Promise<Array<{ label: string; value: any }>>;
  }>;
  showSearchButton?: boolean;
  showResetButton?: boolean;
}

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.css']
})
export class SearchBarComponent {
  @Input() config!: SearchBarConfig;
  @Input() model: any = {};
  @Output() search = new EventEmitter<any>();
  @Output() reset = new EventEmitter<void>();

  // For debouncing autocomplete
  private autocompleteSubjects: { [key: string]: Subject<string> } = {};
  // Store autocomplete state per field (suggestions, dropdown)
  private autocompleteState = new WeakMap<any, { suggestions: any[]; showDropdown: boolean }>();

  onAutocompleteInput(field: any, value: string) {
    if (!field.autocompleteFn) return;
    if (!this.autocompleteSubjects[field.key]) {
      this.autocompleteSubjects[field.key] = new Subject<string>();
      this.autocompleteSubjects[field.key].pipe(debounceTime(200)).subscribe((query) => {
        field.autocompleteFn(query || '').then((results: any[]) => {
          this.getAutocompleteState(field).suggestions = results;
          this.getAutocompleteState(field).showDropdown = true;
        });
      });
    }
    this.autocompleteSubjects[field.key].next(value);
  }

  onAutocompleteFocus(field: any, value: string) {
    const state = this.getAutocompleteState(field);
    if (state.suggestions?.length) {
      state.showDropdown = true;
    } else if (field.autocompleteFn) {
      this.onAutocompleteInput(field, value);
    }
  }

  onAutocompleteBlur(field: any) {
    setTimeout(() => this.getAutocompleteState(field).showDropdown = false, 200); // Delay to allow click
  }

  selectAutocompleteOption(field: any, option: any) {
    this.model[field.key] = option.value;
    this.getAutocompleteState(field).showDropdown = false;
    if (field.onSelect) field.onSelect(option);
  }

  getAutocompleteState(field: any) {
    let state = this.autocompleteState.get(field);
    if (!state) {
      state = { suggestions: [], showDropdown: false };
      this.autocompleteState.set(field, state);
    }
    return state;
  }

  onSearch() {
    this.search.emit(this.model);
  }

  onReset() {
    this.model = {};
    this.reset.emit();
  }
}

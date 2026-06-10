import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export type ErrorType = 'error' | 'warning' | 'info';
export interface AppError {
  message: string;
  type?: ErrorType;
  title?: string;
}

@Injectable({ providedIn: 'root' })
export class ErrorService {
  private errorSubject = new Subject<AppError | null>();
  error$ = this.errorSubject.asObservable();

  show(message: string, type: ErrorType = 'error', title?: string) {
    this.errorSubject.next({ message, type, title });
  }

  clear() {
    this.errorSubject.next(null);
  }
}
